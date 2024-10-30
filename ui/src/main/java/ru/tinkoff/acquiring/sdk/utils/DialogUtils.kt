package ru.tinkoff.acquiring.sdk.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

/**
 * @author s.y.biryukov
 */
internal fun Context.showErrorDialog(
    @StringRes title: Int,
    @StringRes message: Int?,
    @StringRes buttonText: Int,
    onButtonClick: (() -> Unit)? = null
) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .apply { message?.let { setMessage(it) } }
        .setPositiveButton(buttonText) { _, _ ->
            onButtonClick?.invoke()
        }.show()
}

internal fun Context.showErrorDialog(
    title: String,
    message: String?,
    buttonText: String,
    onButtonClick: (() -> Unit)? = null
) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .apply { message?.let { setMessage(it) } }
        .setPositiveButton(buttonText) { _, _ ->
            onButtonClick?.invoke()
        }.show()
}
