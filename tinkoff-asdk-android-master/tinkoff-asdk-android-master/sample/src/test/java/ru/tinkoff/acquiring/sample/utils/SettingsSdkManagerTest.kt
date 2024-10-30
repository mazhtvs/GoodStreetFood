package ru.tinkoff.acquiring.sample.utils

import android.content.Context
import android.content.SharedPreferences
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import ru.tinkoff.acquiring.sample.R

class SettingsSdkManagerTest {
    @Test
    fun `test default preference values`() {
        val context = mock<Context> {
            on { getString(R.string.acq_sp_use_system_keyboard) } doReturn acq_sp_use_system_keyboard
            on { getString(R.string.acq_sp_recurrent_payment) } doReturn acq_sp_recurrent_payment
            on { getString(R.string.acq_sp_fps) } doReturn acq_sp_fps
            on { getString(R.string.acq_sp_tinkoff_pay) } doReturn acq_sp_tinkoff_pay
            on { getString(R.string.acq_sp_yandex_pay) } doReturn acq_sp_yandex_pay
            on { getString(R.string.acq_sp_combi_init) } doReturn acq_sp_combi_init
        }
        val mockPrefs = mock<SharedPreferences>()
        val settingsSdkManager = SettingsSdkManager(context, mockPrefs)

        settingsSdkManager.isCustomKeyboardEnabled
        verify(mockPrefs).getBoolean(acq_sp_use_system_keyboard, true)

        settingsSdkManager.isRecurrentPayment
        verify(mockPrefs).getBoolean(acq_sp_recurrent_payment, false)

        settingsSdkManager.isFpsEnabled
        verify(mockPrefs).getBoolean(acq_sp_fps, true)

        settingsSdkManager.isTinkoffPayEnabled
        verify(mockPrefs).getBoolean(acq_sp_tinkoff_pay, true)

        settingsSdkManager.yandexPayEnabled
        verify(mockPrefs).getBoolean(acq_sp_yandex_pay, true)

        settingsSdkManager.isEnableCombiInit
        verify(mockPrefs).getBoolean(acq_sp_combi_init, false)
    }

    companion object {
        const val acq_sp_use_system_keyboard = "Использовать системную клавиатуру"
        const val acq_sp_recurrent_payment = "Рекуррентный платеж"
        const val acq_sp_fps = "CБП"
        const val acq_sp_tinkoff_pay = "Tinkoff Pay"
        const val acq_sp_yandex_pay = "Yandex Pay"
        const val acq_sp_combi_init = "Combi Init"
    }
}