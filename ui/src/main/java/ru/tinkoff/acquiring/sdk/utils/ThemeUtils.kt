package ru.tinkoff.acquiring.sdk.utils

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import ru.tinkoff.acquiring.sdk.models.DarkThemeMode

/**
 * @author s.y.biryukov
 */
internal fun applyThemeMode(mode: DarkThemeMode) {
    val newMode = when (mode) {
        DarkThemeMode.DISABLED -> AppCompatDelegate.MODE_NIGHT_NO
        DarkThemeMode.ENABLED -> AppCompatDelegate.MODE_NIGHT_YES
        DarkThemeMode.AUTO -> {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                }

                else -> AppCompatDelegate.MODE_NIGHT_NO
            }
        }
    }
    AppCompatDelegate.setDefaultNightMode(newMode)
}
