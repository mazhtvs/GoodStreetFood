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
import android.content.Intent
import android.os.Bundle
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.models.ErrorScreenState
import ru.tinkoff.acquiring.sdk.models.FinishWithErrorScreenState
import ru.tinkoff.acquiring.sdk.models.Screen
import ru.tinkoff.acquiring.sdk.models.ScreenState
import ru.tinkoff.acquiring.sdk.models.SingleEvent
import ru.tinkoff.acquiring.sdk.models.ThreeDsScreen
import ru.tinkoff.acquiring.sdk.models.options.screen.AttachCardOptions
import ru.tinkoff.acquiring.sdk.models.result.CardResult
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_CARD_ID
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_CARD_PAN
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsHelper
import ru.tinkoff.acquiring.sdk.ui.fragments.AttachCardFragment
import ru.tinkoff.acquiring.sdk.viewmodel.AttachCardViewModel

/**
 * @author Mariya Chernyadieva
 */
internal class AttachCardActivity : TransparentActivity() {

    private lateinit var attachCardViewModel: AttachCardViewModel
    private lateinit var attachCardOptions: AttachCardOptions

    protected val threeDsBrowserBasedLauncher =
        registerForActivityResult(ThreeDsLauncher.Contract) {
            if (it is ThreeDsLauncher.Result.Success) {
                finishWithSuccess(it.result)
            } else if (it is ThreeDsLauncher.Result.Error) {
                finishWithError(it.error, it.paymentId)
            } else {
                setResult(Activity.RESULT_CANCELED)
                closeActivity()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachCardOptions = options as AttachCardOptions
        setContentView(R.layout.acq_activity_attach_card)

        attachCardViewModel = provideViewModel(AttachCardViewModel::class.java) as AttachCardViewModel

        initToolbar()
        observeLiveData()

        if (savedInstanceState == null) {
            showFragment(AttachCardFragment())
        }
    }

    private fun initToolbar() {
        setSupportActionBar(findViewById(R.id.acq_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setTitle(R.string.acq_cardlist_addcard_title)
    }

    private fun observeLiveData() {
        with(attachCardViewModel) {
            attachCardResultLiveData.observe(this@AttachCardActivity) { finishWithSuccess(it) }
            screenStateLiveData.observe(this@AttachCardActivity) { handleScreenState(it) }
            screenChangeEventLiveData.observe(this@AttachCardActivity) { handleScreenChangeEvent(it) }
        }
    }

    private fun finishWithSuccess(result: CardResult) {
        val intent = Intent()
        intent.putExtra(EXTRA_CARD_ID, result.cardId)
        intent.putExtra(EXTRA_CARD_PAN, result.panSuffix)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun handleScreenState(screenState: ScreenState) {
        when (screenState) {
            is FinishWithErrorScreenState -> finishWithError(screenState.error)
            is ErrorScreenState -> {
                showErrorDialog(
                    getString(R.string.acq_attach_card_error),
                    screenState.message,
                    getString(R.string.acq_generic_alert_access)
                ) {
                    attachCardViewModel.onErrorButtonClick()
                }
            }
            else -> Unit
        }
    }

    private fun handleScreenChangeEvent(screenChangeEvent: SingleEvent<Screen>) {
        screenChangeEvent.getValueIfNotHandled()?.let { screen ->
            when (screen) {
                is ThreeDsScreen -> attachCardViewModel.coroutine.launchOnMain {
                    try {
                        ThreeDsHelper.Launch(
                            activity =this@AttachCardActivity,
                            options = options,
                            threeDsData = screen.data,
                            appBasedTransaction = screen.transaction,
                            panSuffix = screen.panSuffix,
                            browserBasedLauncher = threeDsBrowserBasedLauncher
                        )
                    } catch (e: Throwable) {
                        finishWithError(e)
                    }
                }
                else -> Unit
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
