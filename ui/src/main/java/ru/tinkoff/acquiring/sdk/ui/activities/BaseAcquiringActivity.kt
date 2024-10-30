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

package ru.tinkoff.acquiring.sdk.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringSdkException
import ru.tinkoff.acquiring.sdk.localization.ASDKString
import ru.tinkoff.acquiring.sdk.models.LoadState
import ru.tinkoff.acquiring.sdk.models.LoadedState
import ru.tinkoff.acquiring.sdk.models.LoadingState
import ru.tinkoff.acquiring.sdk.models.options.screen.BaseAcquiringOptions
import ru.tinkoff.acquiring.sdk.models.result.AsdkResult
import ru.tinkoff.acquiring.sdk.models.result.CardResult
import ru.tinkoff.acquiring.sdk.models.result.PaymentResult
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_CARD_ID
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_CARD_PAN
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_ERROR
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_PAYMENT_ID
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_REBILL_ID
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.RESULT_ERROR
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsSubmitV2Delegate
import ru.tinkoff.acquiring.sdk.utils.applyThemeMode
import ru.tinkoff.acquiring.sdk.utils.showErrorDialog
import ru.tinkoff.acquiring.sdk.viewmodel.ViewModelProviderFactory
import ru.tinkoff.acquiring.sdk.viewmodel.YandexPaymentViewModel
import kotlin.reflect.KClass

/**
 * @author Mariya Chernyadieva
 */
internal open class BaseAcquiringActivity : AppCompatActivity() {

    protected lateinit var options: BaseAcquiringOptions
    protected var progressBar: ProgressBar? = null
    protected var content: View? = null
    private var errorView: View? = null

    private lateinit var sdk: AcquiringSdk

    companion object {

        const val EXTRA_OPTIONS = "options"

        @Throws(AcquiringSdkException::class)
        fun createIntent(context: Context, options: BaseAcquiringOptions, cls: KClass<*>): Intent {
            options.validateRequiredFields()

            val intent = Intent(context, cls.java)
            intent.putExtras(Bundle().apply {
                putParcelable(EXTRA_OPTIONS, options)
            })
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.extras?.let { extras ->
            options = extras.getParcelable(EXTRA_OPTIONS)!!
            sdk = AcquiringSdk(options.terminalKey, options.publicKey)
        }
        initTheme()
        initViews()
    }

    protected open fun handleLoadState(loadState: LoadState) {
        progressBar = findViewById(R.id.acq_progressbar)
        content = findViewById(R.id.acq_content)
        errorView = findViewById(R.id.acq_error_ll_container)
        when (loadState) {
            is LoadingState -> {
                progressBar?.visibility = View.VISIBLE
                content?.visibility = View.INVISIBLE
            }
            is LoadedState -> {
                progressBar?.visibility = View.GONE
                if (errorView?.visibility == View.GONE) {
                    content?.visibility = View.VISIBLE
                }
            }
        }
    }

    protected open fun showErrorScreen(message: String, buttonText: String? = null, onButtonClick: (() -> Unit)? = null) {
        val errorView = findViewById<View>(R.id.acq_error_ll_container)
        val messageTextView = errorView?.findViewById<TextView>(R.id.acq_error_tv_message)
        val button = errorView?.findViewById<Button>(R.id.acq_error_btn_try_again)
        button?.text = buttonText ?: getString(ASDKString.acq_common_message_try_again)

        content = findViewById(R.id.acq_content)
        content?.visibility = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> View.GONE
            else -> View.INVISIBLE
        }

        errorView?.visibility = View.VISIBLE
        messageTextView?.text = message

        if (onButtonClick == null) {
            button?.visibility = View.GONE
        } else {
            button?.setOnClickListener {
                onButtonClick.invoke()
            }
        }
    }

    protected open fun initViews(fullScreenMode: Boolean = false) {

    }

    private fun initTheme() {
        applyThemeMode(options.features.darkThemeMode)
    }

    protected fun showErrorDialog(
        @StringRes title: Int,
        @StringRes message: Int?,
        @StringRes buttonText: Int,
        onButtonClick: (() -> Unit)? = null
    ) {
        (this as Context).showErrorDialog(title, message, buttonText, onButtonClick)
    }

    protected fun showErrorDialog(
        title: String,
        message: String?,
        buttonText: String,
        onButtonClick: (() -> Unit)? = null
    ) {
        (this as Context).showErrorDialog(title, message, buttonText, onButtonClick)
    }

    protected fun hideErrorScreen() {
        val errorView = findViewById<View>(R.id.acq_error_ll_container)
        content = findViewById(R.id.acq_content)
        content?.visibility = View.VISIBLE
        errorView.visibility = View.GONE
    }

    protected fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.acq_activity_fl_container, fragment)
                .commit()
    }

    protected fun provideViewModel(clazz: Class<out ViewModel>): ViewModel {
        return ViewModelProvider(this, ViewModelProviderFactory(application, sdk))[clazz]
    }

    protected fun provideYandexViewModelFactory()
    = YandexPaymentViewModel.factory(application, sdk)

    protected fun provideThreeDsSubmitV2Delegate() = ThreeDsSubmitV2Delegate(sdk)

    protected open fun setSuccessResult(result: AsdkResult) {
        val intent = Intent()

        when (result) {
            is PaymentResult -> {
                intent.putExtra(EXTRA_PAYMENT_ID, result.paymentId)
                intent.putExtra(EXTRA_CARD_ID, result.cardId)
                intent.putExtra(EXTRA_REBILL_ID, result.rebillId)
            }
            is CardResult -> {
                intent.putExtra(EXTRA_CARD_ID, result.cardId)
                intent.putExtra(EXTRA_CARD_PAN, result.panSuffix)
            }
        }

        setResult(Activity.RESULT_OK, intent)
    }

    protected open fun setErrorResult(throwable: Throwable, paymentId: Long? = null) {
        val intent = Intent()
        intent.putExtra(EXTRA_ERROR, throwable)
        intent.putExtra(EXTRA_PAYMENT_ID, paymentId)
        setResult(RESULT_ERROR, intent)
    }

    open fun finishWithSuccess(result: AsdkResult) {
        setSuccessResult(result)
        finish()
    }

    open fun finishWithError(throwable: Throwable, paymentId: Long? = null) {
        setErrorResult(throwable, paymentId)
        finish()
    }

    fun finishWithCancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
