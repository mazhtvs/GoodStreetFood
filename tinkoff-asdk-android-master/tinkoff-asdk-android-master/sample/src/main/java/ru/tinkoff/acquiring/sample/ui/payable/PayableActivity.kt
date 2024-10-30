/*
 * Copyright © 2020 Tinkoff Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ru.tinkoff.acquiring.sample.ui.payable

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import kotlinx.coroutines.Dispatchers
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.acquiring.sample.SampleApplication
import ru.tinkoff.acquiring.sample.ui.PaymentResultActivity
import ru.tinkoff.acquiring.sample.ui.payable.delegates.CardsForRecurrent
import ru.tinkoff.acquiring.sample.ui.payable.delegates.CardsForRecurrentDelegate
import ru.tinkoff.acquiring.sample.ui.payable.delegates.MainFormPayment
import ru.tinkoff.acquiring.sample.ui.payable.delegates.MainFormPaymentDelegate
import ru.tinkoff.acquiring.sample.ui.payable.delegates.MirPayPayment
import ru.tinkoff.acquiring.sample.ui.payable.delegates.MirPayPaymentDelegate
import ru.tinkoff.acquiring.sample.ui.payable.delegates.RecurrentParentPayment
import ru.tinkoff.acquiring.sample.ui.payable.delegates.RecurrentParentPaymentDelegate
import ru.tinkoff.acquiring.sample.ui.payable.delegates.RecurrentPayment
import ru.tinkoff.acquiring.sample.ui.payable.delegates.RecurrentPaymentDelegate
import ru.tinkoff.acquiring.sample.ui.payable.delegates.SbpPayPayment
import ru.tinkoff.acquiring.sample.ui.payable.delegates.SbpPayPaymentDelegate
import ru.tinkoff.acquiring.sample.ui.payable.delegates.TpayPayment
import ru.tinkoff.acquiring.sample.ui.payable.delegates.TpayPaymentDelegate
import ru.tinkoff.acquiring.sample.utils.CombInitDelegate
import ru.tinkoff.acquiring.sample.utils.ReceiptFactory
import ru.tinkoff.acquiring.sample.utils.SettingsSdkManager
import ru.tinkoff.acquiring.sample.utils.TerminalsManager
import ru.tinkoff.acquiring.sdk.AcquiringSdk.Companion.log
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringSdkTimeoutException
import ru.tinkoff.acquiring.sdk.models.FfdVersion
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.SavedCardsOptions
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_ERROR
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_PAYMENT_ID
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.RESULT_ERROR
import ru.tinkoff.acquiring.sdk.ui.activities.QrCodeLauncher
import ru.tinkoff.acquiring.sdk.ui.activities.YandexPaymentLauncher
import ru.tinkoff.acquiring.sdk.utils.Money
import ru.tinkoff.acquiring.sdk.utils.getLongOrNull
import ru.tinkoff.acquiring.yandexpay.AcqYandexPayResult
import ru.tinkoff.acquiring.yandexpay.YandexButtonFragment
import ru.tinkoff.acquiring.yandexpay.addYandexResultListener
import ru.tinkoff.acquiring.yandexpay.createYandexPayButtonFragment
import ru.tinkoff.acquiring.yandexpay.models.YandexPayData
import ru.tinkoff.acquiring.yandexpay.models.enableYandexPay
import ru.tinkoff.acquiring.yandexpay.models.mapYandexPayData
import java.util.Random
import kotlin.math.abs

/**
 * @author Mariya Chernyadieva
 */
@SuppressLint("Registered")
open class PayableActivity : AppCompatActivity(),
    TpayPaymentDelegate by TpayPayment(),
    MirPayPaymentDelegate by MirPayPayment(),
    SbpPayPaymentDelegate by SbpPayPayment(),
    MainFormPaymentDelegate by MainFormPayment(),
    RecurrentPaymentDelegate by RecurrentPayment(),
    CardsForRecurrentDelegate by CardsForRecurrent(),
    RecurrentParentPaymentDelegate by RecurrentParentPayment() {

    protected var totalPrice: Money = Money()
    protected var title: String = ""
    protected var description: String = ""
    protected val receiptFactory = ReceiptFactory.Impl(FfdVersion.VERSION1_05)
    lateinit var settings: SettingsSdkManager

    private lateinit var progressDialog: AlertDialog
    private var errorDialog: AlertDialog? = null
    private var isProgressShowing = false
    private var isErrorShowing = false
    val tinkoffAcquiring = SampleApplication.tinkoffAcquiring
    private val orderId: String
        get() = abs(Random().nextInt()).toString()
    private var acqFragment: YandexButtonFragment? = null
    val combInitDelegate: CombInitDelegate = CombInitDelegate(tinkoffAcquiring.sdk, Dispatchers.IO)

    private val qrCode = this.registerForActivityResult(QrCodeLauncher.Contract) {
        when (it) {
            QrCodeLauncher.Result.Success -> onSuccessPayment()
            QrCodeLauncher.Result.Cancelled -> Toast.makeText(
                this,
                R.string.payment_cancelled,
                Toast.LENGTH_SHORT
            ).show()

            is QrCodeLauncher.Result.Error -> commonErrorHandler(it.error, it.paymentId)
        }
    }

    protected val yandexPay = this.registerForActivityResult(YandexPaymentLauncher.Contract) {
        when (it) {
            YandexPaymentLauncher.Result.Success -> {
                acqFragment?.options = createPaymentOptions()
            }

            YandexPaymentLauncher.Result.Cancelled -> Toast.makeText(this, R.string.payment_cancelled, Toast.LENGTH_SHORT)
                .show()

            is YandexPaymentLauncher.Result.Error -> {
                commonErrorHandler(it.error, it.paymentId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            totalPrice = it.getSerializable(STATE_PAYMENT_AMOUNT) as Money
            isProgressShowing = it.getBoolean(STATE_LOADING_SHOW)
            isErrorShowing = it.getBoolean(STATE_ERROR_SHOW)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        settings = SettingsSdkManager(this)

        initDialogs()
        initSbpPayPayment(this)
        initTpayPaymentDelegate(this)
        initMirPayPaymentDelegate(this)
        initMainFormPaymentDelegate(this)
        initRecurrentPaymentDelegate(this)
        initCardsForRecurrentDelegate(this)
        initRecurrentParentPaymentDelegate(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
        if (errorDialog != null && errorDialog!!.isShowing) {
            errorDialog!!.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PAYMENT_REQUEST_CODE -> handlePaymentResult(resultCode, data)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putSerializable(STATE_PAYMENT_AMOUNT, totalPrice)
            putBoolean(STATE_LOADING_SHOW, isProgressShowing)
            putBoolean(STATE_ERROR_SHOW, isErrorShowing)
        }
        acqFragment?.let {
            supportFragmentManager.putFragment(outState, YANDEX_PAY_FRAGMENT_KEY, it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    protected open fun onSuccessPayment() {
        PaymentResultActivity.start(this, totalPrice)
    }

    protected fun initPayment() {
        if (settings.isRecurrentPayment) {
            launchCardsScreen()
        } else {
            launchMainFormPayment()
        }
    }

    protected fun openDynamicQrScreen() {
        qrCode.launch(
            QrCodeLauncher.Params(
                tinkoffAcquiring = tinkoffAcquiring,
                paymentOptions = createPaymentOptions()
            )
        )
    }

    protected fun setupYandexPay(theme: Int? = null, savedInstanceState: Bundle?) {
        if (!settings.yandexPayEnabled) return

        val yandexPayButtonContainer = findViewById<View>(R.id.btn_yandex_container)

        tinkoffAcquiring.checkTerminalInfo({ terminalInfo ->
            val yandexPayData = terminalInfo?.mapYandexPayData() ?: return@checkTerminalInfo

            yandexPayButtonContainer.isVisible = terminalInfo.enableYandexPay()
            val paymentOptions = createPaymentOptions().apply {
                val session = TerminalsManager.init(this@PayableActivity).selectedTerminal
                this.setTerminalParams(
                    terminalKey = session.terminalKey, publicKey = session.publicKey
                )
            }

            val yaFragment = createYandexButtonFragment(savedInstanceState, paymentOptions, yandexPayData, theme)

            if (supportFragmentManager.isDestroyed.not()) {
                supportFragmentManager.commit { replace(yandexPayButtonContainer.id, yaFragment) }
            }

            acqFragment = yaFragment

        }, {
            yandexPayButtonContainer.visibility = View.GONE
            showErrorDialog()
        })
    }

    fun createPaymentOptions(isParentRecurrent: Boolean = false): PaymentOptions {
        val sessionParams = TerminalsManager.selectedTerminal

        return PaymentOptions()
            .setOptions {
                orderOptions {
                    orderId = this@PayableActivity.orderId
                    amount = totalPrice
                    title = this@PayableActivity.title
                    receipt = receiptFactory.getReceipt()
                    description = this@PayableActivity.description
                    recurrentPayment = isParentRecurrent
                    successURL = "asdk://tinkoff.ru/payment/success?orderId=${this@PayableActivity.orderId}"
                    failURL = "asdk://tinkoff.ru/payment/error?orderId=${this@PayableActivity.orderId}"
                    additionalData = mutableMapOf(
                        "test_additional_data_key_1" to "test_additional_data_value_2",
                        "test_additional_data_key_2" to "test_additional_data_value_2"
                    )
                }
                customerOptions {
                    customerKey = sessionParams.customerKey
                    checkType = settings.checkType
                    email = sessionParams.customerEmail
                }
                featuresOptions {
                    useSecureKeyboard = settings.isCustomKeyboardEnabled
                    cameraCardScannerContract = settings.cameraScannerContract
                    darkThemeMode = settings.resolveDarkThemeMode()
                    duplicateEmailToReceipt = true
                    showPaymentNotifications = settings.showPaymentNotifications
                }
                setTerminalParams(
                    sessionParams.terminalKey,
                    sessionParams.publicKey
                )
            }
    }

    /**
     * @param isRecurrent Признак выполнения рекуррентного платежа
     */
    fun createSavedCardOptions(isRecurrent: Boolean): SavedCardsOptions {
        val settings = SettingsSdkManager(this)
        val params = TerminalsManager.selectedTerminal

        return SampleApplication.tinkoffAcquiring.savedCardsOptions {
            mode = SavedCardsOptions.Mode.PAYMENT
            withArrowBack = true
            showOnlyRecurrentCards = isRecurrent
            allowNewCard = !isRecurrent
            customerOptions {
                customerKey = params.customerKey
                checkType = settings.checkType
                email = params.customerEmail
            }
            featuresOptions {
                useSecureKeyboard = settings.isCustomKeyboardEnabled
                cameraCardScannerContract = settings.cameraScannerContract
                darkThemeMode = settings.resolveDarkThemeMode()
                showPaymentNotifications = settings.showPaymentNotifications
            }
        }
    }

    protected fun handlePaymentResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> onSuccessPayment()
            RESULT_CANCELED -> Toast.makeText(this, R.string.payment_cancelled, Toast.LENGTH_SHORT).show()
            RESULT_ERROR -> {
                commonErrorHandler(data)
            }
        }
    }

    fun showErrorDialog() {
        errorDialog = AlertDialog.Builder(this).apply {
            setTitle(R.string.error_title)
            setMessage(getString(R.string.error_message))
            setNeutralButton("OK") { dialog, _ ->
                dialog.dismiss()
                isErrorShowing = false
            }
        }.show()
        isErrorShowing = true
    }

    private fun initDialogs() {
        progressDialog = AlertDialog.Builder(this).apply {
            setCancelable(false)
            setView(layoutInflater.inflate(R.layout.loading_view, null))
        }.create()

        if (isProgressShowing) {
            showProgressDialog()
        }
        if (isErrorShowing) {
            showErrorDialog()
        }
    }

    open fun createYandexButtonFragment(savedInstanceState: Bundle?,
                                           paymentOptions: PaymentOptions,
                                           yandexPayData: YandexPayData,
                                           theme: Int?) : YandexButtonFragment {
        return savedInstanceState?.let {
            try {
                (supportFragmentManager.getFragment(savedInstanceState, YANDEX_PAY_FRAGMENT_KEY) as? YandexButtonFragment)?.also {
                    tinkoffAcquiring.addYandexResultListener(
                        fragment = it,
                        onYandexErrorCallback = { showErrorDialog() },
                        onYandexCancelCallback = {
                            Toast.makeText(this, R.string.payment_cancelled, Toast.LENGTH_SHORT).show()
                        },
                        onYandexSuccessCallback = ::handleYandexSuccessCallback
                    )
                }
            } catch (i: IllegalStateException) {
                null
            }
        } ?: tinkoffAcquiring.createYandexPayButtonFragment(
            activity = this,
            yandexPayData = yandexPayData,
            options = paymentOptions,
            themeId = theme,
            onYandexErrorCallback = { showErrorDialog() },
            onYandexCancelCallback = {
                Toast.makeText(this, R.string.payment_cancelled, Toast.LENGTH_SHORT).show()
            },
            onYandexSuccessCallback = ::handleYandexSuccessCallback
        )
    }

    private fun handleYandexSuccessCallback(success: AcqYandexPayResult.Success) {
        yandexPay.launch(
            YandexPaymentLauncher.Params(
                tinkoffAcquiring = tinkoffAcquiring,
                options = success.paymentOptions,
                yandexPayToken = success.token,
            )
        )
    }

    fun showProgressDialog() {
        progressDialog.show()
        isProgressShowing = true
    }

    fun hideProgressDialog() {
        progressDialog.dismiss()
        isProgressShowing = false
    }

    private fun getErrorFromIntent(data: Intent?): Throwable? {
        return (data?.getSerializableExtra(EXTRA_ERROR) as? Throwable)
    }

    private fun Throwable.logIfTimeout() {
        (this as? AcquiringSdkTimeoutException)?.run {
            if (paymentId != null) {
                log("paymentId : $paymentId")
            }
            if (status != null) {
                log("status : $status")
            }
        }
    }

    private fun commonErrorHandler(data: Intent?) {
        val error = getErrorFromIntent(data)
        val paymentIdFromIntent = data?.getLongOrNull(EXTRA_PAYMENT_ID)
        commonErrorHandler(error, paymentIdFromIntent)
    }

    private fun commonErrorHandler(error: Throwable?, paymentIdFromIntent: Long?) {
        val message = configureToastMessage(error, paymentIdFromIntent)
        log("toast message: $message")
        error?.printStackTrace()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun configureToastMessage(error: Throwable?, paymentId: Long?): String {
        val acqSdkTimeout  = error as? AcquiringSdkTimeoutException
        val payment = paymentId ?: acqSdkTimeout?.paymentId
        val status = acqSdkTimeout?.status
        return buildString {
            append(getString(R.string.payment_failed))
            append(" ")
            payment?.let { append("paymentId: $it") }
            append(" ")
            status?.let { append("status: $it") }
        }
    }

    companion object {

        const val PAYMENT_REQUEST_CODE = 1

        private const val STATE_PAYMENT_AMOUNT = "payment_amount"
        private const val STATE_LOADING_SHOW = "loading_show"
        private const val STATE_ERROR_SHOW = "error_show"

        const val YANDEX_PAY_FRAGMENT_KEY = "yandex_fragment_key"
    }
}
