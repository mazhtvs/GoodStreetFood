package ru.tinkoff.acquiring.sdk.toggles

import ru.tinkoff.acquiring.sdk.BuildConfig


/**
 * @author k.shpakovskiy
 */
class FeatureToggleManager(
    private val storage: Storage,
) {

    fun isEnabled(toggle: FeatureToggle): Boolean {
        if (BuildConfig.DEBUG) {
            return storage.read(
                id = toggle.id,
                defaultValue = toggle.defaultValue
            )
        }
        return toggle.defaultValue
    }

    fun changeValue(toggle: FeatureToggle, value: Boolean) {
        storage.write(id = toggle.id, value = value)
    }
}
