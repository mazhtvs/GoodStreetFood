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

package ru.tinkoff.acquiring.sample.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.acquiring.sample.camera.DemoCameraScanActivity
import ru.tinkoff.acquiring.sdk.cardscanners.delegate.CardScannerContract
import ru.tinkoff.acquiring.sdk.models.DarkThemeMode
import ru.tinkoff.cardio.CameraCardIOScannerContract

/**
 * @author Mariya Chernyadieva
 */
class SettingsSdkManager(
        private val context: Context,
        private val preferences: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)
) {

    val isCustomKeyboardEnabled: Boolean
        get() {
            val key = context.getString(R.string.acq_sp_use_system_keyboard)
            return preferences.getBoolean(key, true).not()
        }

    val isRecurrentPayment: Boolean
        get() = preferences.getBoolean(context.getString(R.string.acq_sp_recurrent_payment), false)

    val isFpsEnabled: Boolean
        get() = preferences.getBoolean(context.getString(R.string.acq_sp_fps), true)

    val isTinkoffPayEnabled: Boolean
        get() = preferences.getBoolean(context.getString(R.string.acq_sp_tinkoff_pay), true)

    val yandexPayEnabled: Boolean
        get() = preferences.getBoolean(context.getString(R.string.acq_sp_yandex_pay), true)

    val isEnableCombiInit: Boolean
        get() = preferences.getBoolean(context.getString(R.string.acq_sp_combi_init), false)

    val showPaymentNotifications: Boolean
        get() = preferences.getBoolean(context.getString(R.string.acq_show_payment_notifications), true)

    val checkType: String
        get() {
            val defaultCheckType = context.getString(R.string.acq_sp_check_type_no)
            return preferences.getString(context.getString(R.string.acq_sp_check_type_id), defaultCheckType)
                    ?: defaultCheckType
        }

    val cameraScannerContract: CardScannerContract
        get() {
            val cardIOCameraScan = context.getString(R.string.acq_sp_camera_type_card_io)
            val cameraScan = preferences.getString(context.getString(R.string.acq_sp_camera_type_id), cardIOCameraScan)
            return if (cardIOCameraScan == cameraScan) CameraCardIOScannerContract else DemoCameraScanActivity.Contract
        }

    var customUrl: String?
        set(value)  {
            preferences.edit().apply {
                putString(context.getString(R.string.acq_sp_custom_url), value)
            }
                .commit()
        }
        get() = preferences.getString(context.getString(R.string.acq_sp_custom_url), null)

    private val darkThemeMode: String?
        get() {
            val defaultDarkModeId = context.getString(R.string.acq_sp_dark_mode_auto_id)
            return preferences.getString(context.getString(R.string.acq_sp_dark_mode_id), defaultDarkModeId)
        }

    fun resolveDarkThemeMode(): DarkThemeMode {
        return when (darkThemeMode) {
            context.getString(R.string.acq_sp_dark_mode_auto_id) -> DarkThemeMode.AUTO
            context.getString(R.string.acq_sp_dark_mode_enabled_id) -> DarkThemeMode.ENABLED
            else -> DarkThemeMode.DISABLED
        }
    }
}
