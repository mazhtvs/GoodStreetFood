package ru.tinkoff.acquiring.sdk.redesign.common.emailinput.utils

import java.io.Serializable

class SnowFlake<T>(data: T) : Serializable {
    var data: T? = data
        private set
        get() {
            val value = field
            field = null
            return value
        }
}