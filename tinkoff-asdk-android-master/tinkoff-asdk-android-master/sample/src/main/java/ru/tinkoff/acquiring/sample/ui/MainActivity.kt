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

package ru.tinkoff.acquiring.sample.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.acquiring.sample.SampleApplication
import ru.tinkoff.acquiring.sample.adapters.BooksListAdapter
import ru.tinkoff.acquiring.sample.models.Book
import ru.tinkoff.acquiring.sample.models.BooksRegistry
import ru.tinkoff.acquiring.sample.ui.environment.AcqEnvironmentDialog
import ru.tinkoff.acquiring.sample.ui.toggles.TogglesActivity
import ru.tinkoff.acquiring.sample.utils.SettingsSdkManager
import ru.tinkoff.acquiring.sample.utils.TerminalsManager
import ru.tinkoff.acquiring.sample.utils.toast
import ru.tinkoff.acquiring.sdk.models.options.FeaturesOptions
import ru.tinkoff.acquiring.sdk.models.result.CardResult
import ru.tinkoff.acquiring.sdk.redesign.cards.attach.AttachCardLauncher
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ChooseCardLauncher
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.RESULT_ERROR
import ru.tinkoff.acquiring.sdk.ui.activities.QrCodeLauncher
import ru.tinkoff.acquiring.sdk.ui.activities.ThreeDsLauncher

/**
 * @author Mariya Chernyadieva
 */
class MainActivity : AppCompatActivity(), BooksListAdapter.BookDetailsClickListener {

    private lateinit var listViewBooks: ListView
    private lateinit var adapter: BooksListAdapter
    private lateinit var settings: SettingsSdkManager

    private val attachCard = registerForActivityResult(AttachCardLauncher.Contract) { result ->
        when (result) {
            is AttachCardLauncher.Success -> PaymentResultActivity.start(this, result.cardId)
            is AttachCardLauncher.Error -> toast(result.error.message ?: getString(R.string.attachment_failed))
            is AttachCardLauncher.Canceled -> toast(R.string.attachment_cancelled)
        }
    }

    private val savedCards = registerForActivityResult(ChooseCardLauncher.Contract) { result ->
        when (result) {
            is ChooseCardLauncher.CardSelected -> Unit
            is ChooseCardLauncher.Error -> toast(result.error.message ?: getString(R.string.error_title))
            else -> Unit
        }
    }

    private val qrCode = this.registerForActivityResult(QrCodeLauncher.Contract) { result ->
        if (result is QrCodeLauncher.Result.Error) {
            Toast.makeText(this, R.string.payment_failed, Toast.LENGTH_SHORT).show()
        }
    }

    internal val threeDsBrowserBasedLauncher =
        registerForActivityResult(ThreeDsLauncher.Contract) {
            when (it) {
                is ThreeDsLauncher.Result.Success -> {
                    val result = it.result as CardResult
                    toast("Attach success card: ${result.panSuffix ?: result.cardId}")
                }

                ThreeDsLauncher.Result.Cancelled -> toast("Attach canceled")
                is ThreeDsLauncher.Result.Error -> {
                    val error = it.error
                    error.printStackTrace()
                    toast("Attach failure: ${error.message}")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        settings = SettingsSdkManager(this)

        val booksRegistry = BooksRegistry()
        adapter = BooksListAdapter(this, booksRegistry.getBooks(this), this)
        initViews()

        processIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let(::processIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        if (!settings.isFpsEnabled) {
            menu.findItem(R.id.menu_action_static_qr).isVisible = false
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_action_cart -> {
                CartActivity.start(this)
                true
            }
            R.id.menu_action_attach_card -> {
                openAttachCardScreen()
                true
            }
            R.id.menu_action_attach_card_manually -> {
                AttachCardManuallyDialogFragment().show(supportFragmentManager,
                    AttachCardManuallyDialogFragment.TAG)
                true
            }
            R.id.menu_action_change_email -> {
                ChangeEmailDialog().show(supportFragmentManager,
                    ChangeEmailDialog.TAG)
                true
            }
            R.id.menu_action_saved_cards -> {
                openSavedCardsScreen()
                true
            }
            R.id.terminals -> {
                startActivity(Intent(this, TerminalsActivity::class.java))
                true
            }
            R.id.menu_action_about -> {
                AboutActivity.start(this)
                true
            }
            R.id.menu_action_environment -> {
                AcqEnvironmentDialog().show(supportFragmentManager, AttachCardManuallyDialogFragment.TAG)
                true
            }
            R.id.menu_action_static_qr -> {
                openStaticQrScreen()
                true
            }
            R.id.menu_action_settings -> {
                SettingsActivity.start(this)
                true
            }
            R.id.menu_action_environment -> {
                AcqEnvironmentDialog().show(supportFragmentManager, AcqEnvironmentDialog.TAG)
                true
            }
            R.id.menu_action_toggles -> {
                startActivity(Intent(this, TogglesActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            NOTIFICATION_PAYMENT_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK -> {
                        Toast.makeText(this,
                                R.string.notification_payment_success,
                                Toast.LENGTH_SHORT).show()
                    }
                    RESULT_CANCELED -> Toast.makeText(this,
                            R.string.payment_cancelled,
                            Toast.LENGTH_SHORT).show()
                    RESULT_ERROR -> Toast.makeText(this,
                            R.string.payment_failed,
                            Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBookDetailsClicked(book: Book) {
        DetailsActivity.start(this, book)
    }

    private fun initViews() {
        listViewBooks = findViewById(R.id.lv_books)
        listViewBooks.adapter = adapter
    }

    private fun openAttachCardScreen() {
        val settings = SettingsSdkManager(this)
        val params = TerminalsManager.selectedTerminal

        val options = SampleApplication.tinkoffAcquiring.attachCardOptions {
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

        attachCard.launch(options)
    }

    private fun openStaticQrScreen() {
        val options = FeaturesOptions().apply {
            darkThemeMode = settings.resolveDarkThemeMode()
        }
        qrCode.launch(QrCodeLauncher.Params(
            tinkoffAcquiring = SampleApplication.tinkoffAcquiring,
            featureOptions = options,
        ))
    }

    private fun openSavedCardsScreen() {
        val settings = SettingsSdkManager(this)
        val params = TerminalsManager.selectedTerminal

        val savedCardsOptions = SampleApplication.tinkoffAcquiring.savedCardsOptions {
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
        savedCards.launch(ChooseCardLauncher.StartData(savedCardsOptions))
    }

    private fun processIntent(intent: Intent) {
        val data = intent.data ?: return

        toast("Opened with deeplink: $data", Toast.LENGTH_LONG)
    }

    companion object {
        const val NOTIFICATION_PAYMENT_REQUEST_CODE = 14
        const val THREE_DS_REQUEST_CODE = 15
    }
}
