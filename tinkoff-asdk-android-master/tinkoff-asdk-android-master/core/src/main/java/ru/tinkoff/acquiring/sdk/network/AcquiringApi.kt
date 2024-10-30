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

package ru.tinkoff.acquiring.sdk.network

import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.AcquiringSdk.Companion.isDeveloperMode

/**
 * Содержит константы для создания запросов к Acquiring API
 *
 * @author Mariya Chernyadieva
 */
object AcquiringApi {

    const val ADD_CARD_METHOD = "AddCard"
    const val ATTACH_CARD_METHOD = "AttachCard"
    const val CHARGE_METHOD = "Charge"
    const val CANCEL_METHOD = "Cancel"
    const val FINISH_AUTHORIZE_METHOD = "FinishAuthorize"
    const val GET_ADD_CARD_STATE_METHOD = "GetAddCardState"
    const val CHECK_3DS_VERSION_METHOD = "Check3dsVersion"
    const val GET_CARD_LIST_METHOD = "GetCardList"
    const val GET_STATE_METHOD = "GetState"
    const val INIT_METHOD = "Init"
    const val CONFIRM_METHOD = "Confirm"
    const val REMOVE_CARD_METHOD = "RemoveCard"
    const val SUBMIT_RANDOM_AMOUNT_METHOD = "SubmitRandomAmount"
    const val GET_QR_METHOD = "GetQr"
    const val GET_STATIC_QR_METHOD = "GetStaticQr"
    const val SUBMIT_3DS_AUTHORIZATION = "Submit3DSAuthorization"
    const val SUBMIT_3DS_AUTHORIZATION_V2 = "Submit3DSAuthorizationV2"
    const val COMPLETE_3DS_METHOD_V2 = "Complete3DSMethodv2"
    const val GET_TERMINAL_PAY_METHODS = "GetTerminalPayMethods"
    const val MIR_PAY_GET_DEEPLINK_METHOD = "MirPay/GetDeepLink"

    const val API_ERROR_CODE_3DSV2_NOT_SUPPORTED = "106"
    const val API_ERROR_CODE_CUSTOMER_NOT_FOUND = "7"
    const val API_ERROR_CODE_CHARGE_REJECTED = "104"
    const val API_ERROR_CODE_NO_ERROR = "0"

    const val RECURRING_TYPE_KEY = "recurringType"
    const val RECURRING_TYPE_VALUE = "12"
    const val FAIL_MAPI_SESSION_ID = "failMapiSessionId"

    /**
     * Коды ошибок, сообщение которых можно показать конечным пользователям
     */
    val errorCodesForUserShowing = listOf("53", "206", "224", "225", "252", "99", "101",
            "1006", "1012", "1013", "1014", "1015", "1030", "1033", "1034", "1035", "1036", "1037", "1038",
            "1039", "1040", "1041", "1042", "1043", "1051", "1054", "1057", "1065", "1082", "1089", "1091", "1096")

    /**
     * Коды ошибок, вызванные временными неполадками системы
     */
    val errorCodesFallback = listOf("9999", "231", "3", "3001")

    /**
     * Коды ошибок при привязке карты
     */
    val errorCodesAttachedCard = listOf("3", "6")

    internal const val STREAM_BUFFER_SIZE = 4096
    internal const val API_REQUEST_METHOD_POST = "POST"
    internal const val API_REQUEST_METHOD_GET = "GET"

    const val JSON = "application/json"
    internal const val FORM_URL_ENCODED = "application/x-www-form-urlencoded"
    /**
     * Таймаут сетевых запросов в секундах.
     */
    internal const val NETWORK_TIMEOUT_SECONDS: Long = 40

    internal const val API_VERSION = "v2"
    internal const val API_URL_RELEASE = "https://securepay.tinkoff.ru/$API_VERSION"
    internal const val API_URL_DEBUG = "https://rest-api-test.tinkoff.ru/$API_VERSION"
    internal const val API_URL_PREPROD = "https://qa-mapi.tcsbank.ru/$API_VERSION"

    /**
     * Возвращает базовый Url.
     * Зависит от режима работы SDK [AcquiringSdk.isDeveloperMode] и [AcquiringSdk.customUrl]
     */
    fun getUrl(): String {
        val customUrl = AcquiringSdk.customUrl
        return when {
            isDeveloperMode && customUrl != null -> customUrl.fixUrlEnding(API_VERSION)
            isDeveloperMode -> API_URL_DEBUG
            else -> API_URL_RELEASE
        }
    }

    private fun String.fixUrlEnding(ending: String): String {
        return when {
            this.endsWith(ending) -> this
            else -> "$this/$ending"
        }
    }
}
