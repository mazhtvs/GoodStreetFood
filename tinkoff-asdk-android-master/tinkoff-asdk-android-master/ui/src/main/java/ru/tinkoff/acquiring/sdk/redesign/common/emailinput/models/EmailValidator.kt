package ru.tinkoff.acquiring.sdk.redesign.common.emailinput.models

import java.util.regex.Pattern

object EmailValidator {
    private const val EMAIL_REGEX = ".+\\@.+\\..+"
    private val pattern = Pattern.compile(EMAIL_REGEX)

    fun validate(text: String?): Boolean {
        return text?.let {
            it.isNotBlank() && pattern.matcher(it).matches()
        } ?: false
    }
}