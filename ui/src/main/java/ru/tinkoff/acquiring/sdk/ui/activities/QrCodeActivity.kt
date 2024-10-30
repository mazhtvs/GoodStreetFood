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

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import ru.tinkoff.acquiring.sdk.models.ErrorScreenState
import ru.tinkoff.acquiring.sdk.models.FinishWithErrorScreenState
import ru.tinkoff.acquiring.sdk.models.ScreenState
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.result.PaymentResult
import ru.tinkoff.acquiring.sdk.ui.fragments.DynamicQrFragment
import ru.tinkoff.acquiring.sdk.ui.fragments.StaticQrFragment
import ru.tinkoff.acquiring.sdk.viewmodel.QrViewModel

internal class QrCodeActivity : TransparentActivity() {

    private lateinit var viewModel: QrViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = provideViewModel(QrViewModel::class.java) as QrViewModel
        viewModel.setPaymentOptions(options as? PaymentOptions)
        observeLiveData()
    }

    private fun observeLiveData() {
        viewModel.run {
            screenStateLiveData.observe(this@QrCodeActivity) { handleScreenState(it) }
            paymentResultLiveData.observe(this@QrCodeActivity) { handlePaymentResult(it) }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.qrScreeStateFlow.collectLatest {
                showTargetFragment(it.isStaticQr)
            }
        }
    }

    private fun showTargetFragment(isStaticQrPayment: Boolean) {
        val targetFragment = if (isStaticQrPayment) StaticQrFragment() else DynamicQrFragment()
        showFragment(targetFragment)
    }

    private fun handlePaymentResult(paymentId: Long) {
        finishWithSuccess(PaymentResult(paymentId))
    }

    private fun handleScreenState(screenState: ScreenState) {
        when (screenState) {
            is ErrorScreenState -> {
                showErrorScreen(screenState.message) {
                    hideErrorScreen()
                    viewModel.onErrorButtonClick()
                }
            }

            is FinishWithErrorScreenState -> finishWithError(screenState.error)
            else -> Unit
        }
    }
}
