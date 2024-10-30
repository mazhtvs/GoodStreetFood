package ru.tinkoff.acquiring.sdk.toggles

/**
 * @author k.shpakovskiy
 */
object TestFeatureToggle : FeatureToggle {
    override val id: String = "EACQAPW-7975-test-id"
    override val name: String = "EACQAPW-7975-test-name"
    override val issueKey: String = "EACQAPW-7975"
    override var defaultValue: Boolean = false
}
