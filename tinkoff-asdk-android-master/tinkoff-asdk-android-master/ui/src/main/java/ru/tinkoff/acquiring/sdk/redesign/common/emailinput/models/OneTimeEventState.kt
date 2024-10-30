package ru.tinkoff.acquiring.sdk.redesign.common.emailinput.models

import ru.tinkoff.acquiring.sdk.redesign.common.emailinput.utils.SnowFlake

/**
 * Created by Michael Babayan
 */
data class OneTimeEventState(
    var hideEmailInputKeyboard: SnowFlake<Boolean>? = null,
    var clearEmailInputFocus: SnowFlake<Boolean>? = null,
    var requestEmailInputFocus: SnowFlake<Boolean>? = null
)
