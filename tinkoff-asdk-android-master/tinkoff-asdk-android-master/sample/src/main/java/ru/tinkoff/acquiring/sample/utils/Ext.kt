package ru.tinkoff.acquiring.sample.utils

import android.app.Activity
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * @author k.shpakovskiy
 */
fun Activity.toast(message: String, duration: Int = Toast.LENGTH_SHORT) = runOnUiThread {
    Toast.makeText(this, message, duration).show()
}

fun Activity.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) = runOnUiThread {
    Toast.makeText(this, message, duration).show()
}
