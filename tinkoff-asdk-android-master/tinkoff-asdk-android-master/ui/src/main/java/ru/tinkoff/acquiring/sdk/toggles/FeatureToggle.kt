package ru.tinkoff.acquiring.sdk.toggles

/**
 * @author k.shpakovskiy
 */
interface FeatureToggle {
    val id : String
    val name: String
    val issueKey: String
    var defaultValue: Boolean
}
