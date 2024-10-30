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

package ru.tinkoff.acquiring.sdk.models

import ru.tinkoff.acquiring.sdk.threeds.ThreeDsAppBasedTransaction

/**
 * @author Mariya Chernyadieva
 */
internal sealed interface ScreenState

internal data object DefaultScreenState : ScreenState
internal class ErrorScreenState(val message: String) : ScreenState
internal class FinishWithErrorScreenState(val error: Throwable, val paymentId: Long? = null) :
    ScreenState

internal sealed interface Screen
internal data object PaymentScreen : Screen
internal class ThreeDsScreen(
    val data: ThreeDsData,
    val transaction: ThreeDsAppBasedTransaction?,
    val panSuffix: String = ""
) : Screen

internal sealed interface LoadState
internal data object LoadingState : LoadState
internal data object LoadedState : LoadState

internal sealed interface ScreenEvent
