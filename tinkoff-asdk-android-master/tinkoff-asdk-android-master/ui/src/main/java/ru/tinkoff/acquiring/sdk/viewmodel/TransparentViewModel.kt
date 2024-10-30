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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.models.LoadedState
import ru.tinkoff.acquiring.sdk.models.LoadingState
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsDelegate
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsState

internal class TransparentViewModel(
    application: Application,
    sdk: AcquiringSdk
) : BaseAcquiringViewModel(application, sdk),
    ThreeDsDelegate by ThreeDsDelegate.Impl(sdk) {

    private val threeDSState: MutableLiveData<ThreeDsState> = MutableLiveData()

    val threeDsStateLiveData: LiveData<ThreeDsState> = threeDSState

    init {
        initDelegate(coroutine)
        threeDsState
            .onEach(::collect3dsState)
            .launchIn(viewModelScope)
    }

    private fun collect3dsState(state: ThreeDsState) {

        when(state) {
            is ThreeDsState.ErrorState -> {
                handleException(state.throwable, state.paymentId)
            }
            ThreeDsState.LoadedState -> {
                changeScreenState(LoadedState)
            }
            ThreeDsState.LoadingState -> {
                changeScreenState(LoadingState)
            }
            else -> {
                threeDSState.value = state
            }
        }
    }
}
