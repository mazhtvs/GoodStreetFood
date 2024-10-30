package ru.tinkoff.acquiring.sdk.toggles

import android.content.Context
import android.content.Context.MODE_PRIVATE

/**
 * @author k.shpakovskiy
 */
class Storage(
    context: Context
) {
    private val prefs = context.applicationContext.getSharedPreferences(TOGGLE_PREFS, MODE_PRIVATE)

    fun read(id: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(id, defaultValue)
    }

    fun write(id: String, value: Boolean) {
        prefs.edit().putBoolean(id, value).commit()
    }

    private companion object {
        const val TOGGLE_PREFS = "TOGGLE_PREFS"
    }
}
