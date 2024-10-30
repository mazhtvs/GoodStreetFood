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
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.launch
import ru.tinkoff.acquiring.sdk.models.AsdkState
import ru.tinkoff.acquiring.sdk.models.DefaultState
import ru.tinkoff.acquiring.sdk.models.ErrorScreenState
import ru.tinkoff.acquiring.sdk.models.FinishWithErrorScreenState
import ru.tinkoff.acquiring.sdk.models.LoadState
import ru.tinkoff.acquiring.sdk.models.LoadingState
import ru.tinkoff.acquiring.sdk.models.Screen
import ru.tinkoff.acquiring.sdk.models.ScreenState
import ru.tinkoff.acquiring.sdk.models.SingleEvent
import ru.tinkoff.acquiring.sdk.models.ThreeDsScreen
import ru.tinkoff.acquiring.sdk.models.YandexPayState
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.result.AsdkResult
import ru.tinkoff.acquiring.sdk.redesign.dialog.PaymentLCEDialogFragment
import ru.tinkoff.acquiring.sdk.redesign.dialog.showDialog
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsHelper
import ru.tinkoff.acquiring.sdk.viewmodel.YandexPaymentViewModel

/**
 * @author Ivan Golovachev
 */
internal class YandexPaymentActivity : TransparentActivity() {

    private lateinit var paymentViewModel: YandexPaymentViewModel
    private lateinit var paymentOptions: PaymentOptions
    private var asdkState: AsdkState = DefaultState
    private var paymentLCEDialogFragment: PaymentLCEDialogFragment =
        PaymentLCEDialogFragment.create(false)

    private val threeDsBrowserBasedLauncher =
        registerForActivityResult(ThreeDsLauncher.Contract) { result ->
            if (result is ThreeDsLauncher.Result.Success) {
                handleSuccessResult(result.result)
            } else if (result is ThreeDsLauncher.Result.Error) {
                handleErrorResult(result.error)
            } else {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

    private fun handleErrorResult(error: Throwable) {
        if (paymentOptions.features.showPaymentNotifications) {
            getStateDialog {
                it.failure {
                    finishWithError(error)
                }
            }
        } else {
            finishWithError(error)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        paymentOptions = options as PaymentOptions
        asdkState = paymentOptions.asdkState

        bottomContainer?.isVisible = false

        paymentViewModel = provideYandexViewModelFactory().create(
            YandexPaymentViewModel::class.java,
            CreationExtras.Empty
        )
        observeLiveData()

        if (savedInstanceState == null) {
            (asdkState as? YandexPayState)?.let {
                paymentViewModel.startYandexPayPayment(paymentOptions, it.yandexToken, it.paymentId)
            }
        }

        paymentViewModel.checkoutAsdkState(asdkState)
    }

    override fun handleLoadState(loadState: LoadState) {
        super.handleLoadState(loadState)
        when (loadState) {
            is LoadingState -> {
                getStateDialog { it.loading() }
            }
            else -> Unit
        }
    }

    private fun observeLiveData() {
        with(paymentViewModel) {
            loadStateLiveData.observe(this@YandexPaymentActivity, ::handleLoadState)
            screenStateLiveData.observe(this@YandexPaymentActivity, ::handleScreenState)
            screenChangeEventLiveData.observe(this@YandexPaymentActivity, ::handleScreenChangeEvent)
            paymentResultLiveData.observe(this@YandexPaymentActivity, ::handleSuccessResult)
        }
    }

    private fun handleSuccessResult(result: AsdkResult) {
        if (paymentOptions.features.showPaymentNotifications) {
            getStateDialog { f ->
                f.success { finishWithSuccess(result) }
            }
        } else {
            finishWithSuccess(result)
        }
    }

    private fun handleScreenChangeEvent(screenChangeEvent: SingleEvent<Screen>) {
        screenChangeEvent.getValueIfNotHandled()?.let { screen ->
            when (screen) {
                is ThreeDsScreen -> try {
                    lifecycleScope.launch {
                        ThreeDsHelper.Launch(
                            activity = this@YandexPaymentActivity,
                            options = options,
                            threeDsData = screen.data,
                            browserBasedLauncher = threeDsBrowserBasedLauncher
                        )
                    }
                } catch (e: Throwable) {
                    handleErrorResult(e)
                }
                else -> Unit
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        paymentViewModel.onDismissDialog()
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleScreenState(screenState: ScreenState) {
        when (screenState) {
            is FinishWithErrorScreenState -> getStateDialog {
                it.failure {
                    paymentViewModel.onDismissDialog()
                    finishWithError(screenState.error)
                }
            }
            is ErrorScreenState -> getStateDialog {
                it.failure {
                    paymentViewModel.onDismissDialog()
                    finishWithError(IllegalStateException(screenState.message))
                }
            }
            else -> Unit
        }
    }

    private fun getStateDialog(block: PaymentLCEDialogFragment.OnViewCreated? = null) {
        if (paymentLCEDialogFragment.isAdded.not()) {
            paymentLCEDialogFragment.onViewCreated = block
            showDialog(paymentLCEDialogFragment)
        } else {
            block?.invoke(paymentLCEDialogFragment)
        }
    }
}
