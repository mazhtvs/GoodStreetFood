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

package ru.tinkoff.acquiring.sdk.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringApiException
import ru.tinkoff.acquiring.sdk.exceptions.NetworkException
import ru.tinkoff.acquiring.sdk.localization.ASDKString
import ru.tinkoff.acquiring.sdk.models.ErrorScreenState
import ru.tinkoff.acquiring.sdk.models.FinishWithErrorScreenState
import ru.tinkoff.acquiring.sdk.models.LoadState
import ru.tinkoff.acquiring.sdk.models.LoadedState
import ru.tinkoff.acquiring.sdk.models.Screen
import ru.tinkoff.acquiring.sdk.models.ScreenState
import ru.tinkoff.acquiring.sdk.models.SingleEvent
import ru.tinkoff.acquiring.sdk.network.AcquiringApi
import ru.tinkoff.acquiring.sdk.utils.CoroutineManager

/**
 * @author Mariya Chernyadieva
 */
internal open class BaseAcquiringViewModel(
    application: Application,
    val sdk: AcquiringSdk
) : AndroidViewModel(application) {

    val coroutine = CoroutineManager(exceptionHandler = { handleException(it) })
    private val loadState: MutableLiveData<LoadState> = MutableLiveData()
    private val screenState: MutableLiveData<ScreenState> = MutableLiveData()
    private val screenChangeEvent: MutableLiveData<SingleEvent<Screen>> = MutableLiveData()

    val screenChangeEventLiveData: LiveData<SingleEvent<Screen>> = screenChangeEvent
    val screenStateLiveData: LiveData<ScreenState> = screenState
    val loadStateLiveData: LiveData<LoadState> = loadState

    val context: Context get() = getApplication<Application>().applicationContext

    override fun onCleared() {
        super.onCleared()
        coroutine.cancelAll()
    }

    fun handleException(throwable: Throwable, paymentId: Long? = null) {
        loadState.value = LoadedState
        when (throwable) {
            is NetworkException -> changeScreenState(
                ErrorScreenState(context.getString(ASDKString.acq_pay_dialog_error_network))
            )

            is AcquiringApiException -> {
                val errorCode = throwable.response?.errorCode
                if (errorCode != null && (AcquiringApi.errorCodesFallback.contains(errorCode) ||
                            AcquiringApi.errorCodesForUserShowing.contains(errorCode))
                ) {
                    changeScreenState(ErrorScreenState(resolveErrorMessage(throwable)))
                } else changeScreenState(FinishWithErrorScreenState(throwable, paymentId))
            }

            else -> changeScreenState(FinishWithErrorScreenState(throwable, paymentId))
        }
    }

    protected fun changeLoadState(state: LoadState) {
        loadState.value = state
    }

    protected fun changeScreen(newScreenState: Screen) {
        screenChangeEvent.value = SingleEvent(newScreenState)
    }

    protected fun changeScreenState(newScreenState: ScreenState) {
        screenState.value = newScreenState
    }

    private fun resolveErrorMessage(apiException: AcquiringApiException): String {
        val fallbackMessage = context.getString(ASDKString.acq_pay_dialog_error_fallback_message)
        val errorCode = apiException.response?.errorCode
        return if (errorCode != null && AcquiringApi.errorCodesForUserShowing.contains(errorCode)) {
            apiException.response?.message ?: fallbackMessage
        } else {
            fallbackMessage
        }
    }
}
