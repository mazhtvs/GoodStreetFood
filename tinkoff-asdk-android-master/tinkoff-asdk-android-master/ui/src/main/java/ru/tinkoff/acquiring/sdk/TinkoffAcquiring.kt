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

package ru.tinkoff.acquiring.sdk

import android.content.Context
import androidx.annotation.MainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.tinkoff.acquiring.sdk.models.options.screen.AttachCardOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.BaseAcquiringOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.SavedCardsOptions
import ru.tinkoff.acquiring.sdk.payment.MirPayProcess
import ru.tinkoff.acquiring.sdk.payment.SbpPaymentProcess
import ru.tinkoff.acquiring.sdk.payment.TpayProcess
import ru.tinkoff.acquiring.sdk.requests.performSuspendRequest
import ru.tinkoff.acquiring.sdk.responses.TerminalInfo

/**
 * Точка входа для взаимодействия с Acquiring SDK
 *
 * В некоторых случаях для формирования запросов к API может потребоваться генерация токена для
 * подписи запроса, см. [AcquiringSdk.tokenGenerator].
 *
 * @param terminalKey    ключ терминала. Выдается после подключения к Tinkoff Acquiring
 * @param publicKey      экземпляр PublicKey созданный из публичного ключа, выдаваемого вместе с
 *                       terminalKey
 *
 * @author Mariya Chernyadieva
 */
class TinkoffAcquiring(
    private val context: Context,
    private val terminalKey: String,
    private val publicKey: String
) {

    val sdk = AcquiringSdk(terminalKey, publicKey)

    /**
     * Создает платежную сессию в рамках оплаты по Системе быстрых платежей
     */
    @MainThread
    fun initSbpPaymentSession() {
        SbpPaymentProcess.init(sdk, context.packageManager)
    }

    /**
     * Создает платежную сессию в рамках оплаты по tinkoffPay
     */
    @MainThread
    fun initTinkoffPayPaymentSession() {
        TpayProcess.init(sdk)
    }

    /**
     * Создает платежную сессию в рамках оплаты по MirPay
     */
    @MainThread
    fun initMirPayPaymentSession() {
        MirPayProcess.init(sdk)
    }

    /**
     * Проверка доступных спосбов оплаты
     */
    fun checkTerminalInfo(onSuccess: (TerminalInfo?) -> Unit,
                          onFailure: ((Throwable) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val mainScope = this
            val result = sdk.getTerminalPayMethods()
                .performSuspendRequest()
                .map { it.terminalInfo }
            withContext(Dispatchers.Main) {
                result.fold(onSuccess = onSuccess, onFailure = { onFailure?.invoke(it) })
                mainScope.cancel()
            }
        }
    }

    fun attachCardOptions(setup: AttachCardOptions.() -> Unit) = AttachCardOptions().also { options ->
        addKeys(options)
        setup(options)
    }

    fun savedCardsOptions(setup: SavedCardsOptions.() -> Unit) = SavedCardsOptions().also { options ->
        addKeys(options)
        setup(options)
    }

    internal fun addKeys(options: BaseAcquiringOptions) : BaseAcquiringOptions {
        options.setTerminalParams(terminalKey, publicKey)
        return options
    }
}
