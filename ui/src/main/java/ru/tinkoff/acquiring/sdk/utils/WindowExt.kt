package ru.tinkoff.acquiring.sdk.utils

import android.view.Window
import android.view.WindowManager

fun Window.setFlagNotTouchable() =
    this.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    )

fun Window.setFlagTouchable() =
    this.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
