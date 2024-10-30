package ru.tinkoff.acquiring.sdk.redesign.common.emailinput.models

import java.io.Serializable

/**
 * Created by Michael Babayan
 */
data class EmailInputState(
        val validEmail: Boolean = false,
        val email: String? = null,
        var emailEnabled: Boolean = false,
        var cursorPosition: Int = 0,
        val errorHighlighted: Boolean = false
) : Serializable
