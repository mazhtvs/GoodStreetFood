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

package ru.tinkoff.acquiring.sdk.models.enums

/**
 * Статус в ответе на запрос методов Acquiring API
 *
 * @author Mariya Chernyadieva
 */
enum class ResponseStatus {
    NEW,
    FORMSHOWED,
    UNKNOWN,
    FORM_SHOWED,
    COMPLETED,

    //PROCESS
    AUTHORIZING,
    PREAUTHORIZING,
    PAY_CHECKING,

    //HAPPY PASS
    AUTHORIZED,
    REVERSING,
    REVERSED,
    PARTIAL_REVERSED,
    CONFIRMING,
    CONFIRMED,
    ASYNC_REFUNDING,
    REFUNDED,
    REFUNDING,
    PARTIAL_REFUNDED,
    CANCEL_REFUNDED,
    CONFIRM_CHECKING,

    //3DS
    THREE_DS_CHECKING,
    THREE_DS_CHECKED,

    //UNHAPPY PASS
    REJECTED,
    AUTH_FAIL,
    CANCELED,
    DEADLINE_EXPIRED,
    ATTEMPTS_EXPIRED;

    override fun toString(): String {
        return when (this) {
            THREE_DS_CHECKING -> TDS_CHECKING_STRING
            THREE_DS_CHECKED -> TDS_CHECKED_STRING
            else -> super.toString()
        }
    }

    companion object {

        private const val TDS_CHECKING_STRING = "3DS_CHECKING"
        private const val TDS_CHECKED_STRING = "3DS_CHECKED"
        val successStatuses = setOf(
            AUTHORIZED,
            REVERSING,
            REVERSED,
            PARTIAL_REVERSED,
            CONFIRMING,
            CONFIRMED,
            ASYNC_REFUNDING,
            REFUNDED,
            REFUNDING,
            PARTIAL_REFUNDED,
            CANCEL_REFUNDED,
            CONFIRM_CHECKING,
        )

        val processStatuses = setOf(
            AUTHORIZING,
            PREAUTHORIZING,
            PAY_CHECKING,
        )

        val unhappyPassStatuses = setOf(
            REJECTED,
            AUTH_FAIL,
            CANCELED,
            ATTEMPTS_EXPIRED
        )

        fun checkSuccessStatuses(status: ResponseStatus): Boolean = status in successStatuses

        fun checkProcessStatuses(status: ResponseStatus): Boolean = status in processStatuses

        @JvmStatic
        fun fromString(stringValue: String): ResponseStatus {
            return when (stringValue) {
                TDS_CHECKING_STRING -> THREE_DS_CHECKING
                TDS_CHECKED_STRING -> THREE_DS_CHECKED
                else -> {
                    val isStatusDefined = values().any { it.name == stringValue }
                    if (isStatusDefined) {
                        valueOf(stringValue)
                    } else UNKNOWN
                }
            }
        }
    }
}
