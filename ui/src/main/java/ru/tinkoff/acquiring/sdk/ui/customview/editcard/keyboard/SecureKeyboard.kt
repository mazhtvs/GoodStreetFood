/*
 * Copyright © 2020 Tinkoff Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ru.tinkoff.acquiring.sdk.ui.customview.editcard.keyboard

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.GridLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.ui.customview.editcard.dpToPx

/**
 * @author Mariya Chernyadieva
 */
internal class SecureKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), OnClickListener {

    var keyClickListener: OnKeyClickListener? = null
    var animationListener: AnimationListener? = null

    val keyboardHeightPx: Int
    private var keyboardBackgroundColor: Int = 0
    private var keyboardKeyTextColor: Int = 0

    private var isOpen = false
    private var openRunning : Runnable? = null
    private var hideRunning : Runnable? = null
    private var hideAnimation: Animator? = null
    private var showAnimation: Animator? = null

    init {
        val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.SecureKeyboard)
        try {
            attrsArray.apply {
                val defaultColor =
                    ContextCompat.getColor(context, R.color.acq_colorKeyboardBackground)
                keyboardBackgroundColor =
                    getColor(R.styleable.SecureKeyboard_acqKeyboardBackgroundColor, defaultColor)
                keyboardKeyTextColor =
                    getColor(R.styleable.SecureKeyboard_acqKeyboardKeyTextColor, Color.WHITE)
            }
        } finally {
            attrsArray.recycle()
        }
        keyboardHeightPx =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                DEFAULT_KEYBOARD_HEIGHT_DP
            } else {
                LANDSCAPE_KEYBOARD_HEIGHT_DP
            }.dpToPx(context).toInt()
        createKeyboard()

        translationY = keyboardHeightPx.toFloat()
    }

    internal fun isShowing(): Boolean {
        return openRunning != null || isOpen
    }

    private fun createKeyboard() {
        val gridLayout = GridLayout(context).apply {
            layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeightPx)
            columnCount = 3
            rowCount = 4
            orientation = GridLayout.HORIZONTAL
            setBackgroundColor(keyboardBackgroundColor)
            setPadding(40.dpToPx(context).toInt(), 0, 40.dpToPx(context).toInt(), 0)
        }

        val keyLayoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        for (i in 1..9) {
            val key = KeyView(context).apply {
                layoutParams = keyLayoutParams
                keyCode = i
                textColor = keyboardKeyTextColor
                keyColor = keyboardBackgroundColor
                setOnClickListener(this@SecureKeyboard)
            }
            gridLayout.addView(key)
        }

        val keyZero = KeyView(context).apply {
            layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(3, GridLayout.CENTER),
                GridLayout.spec(1, GridLayout.CENTER)
            )
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            keyCode = 0
            textColor = keyboardKeyTextColor
            keyColor = keyboardBackgroundColor
            setOnClickListener(this@SecureKeyboard)
        }
        gridLayout.addView(keyZero)

        val keyDel = KeyView(context).apply {
            layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(3, GridLayout.CENTER),
                GridLayout.spec(2, GridLayout.CENTER)
            )
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            keyCode = 10
            textColor = keyboardKeyTextColor
            keyColor = keyboardBackgroundColor
            contentImage = BitmapFactory.decodeResource(resources, R.drawable.acq_back_arrow)
            setOnClickListener(this@SecureKeyboard)
        }
        gridLayout.addView(keyDel)

        this.addView(gridLayout)
    }

    override fun onClick(view: View?) {
        val key = view as KeyView
        keyClickListener?.onKeyClick(key.keyCode)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        val toWindowInsetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets)
        val bottom = toWindowInsetsCompat
            .getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        this.updatePadding(bottom = bottom)
        return super.onApplyWindowInsets(insets)
    }

    fun show() {
        if (hideRunning != null || hideAnimation != null) {
            removeCallbacks(hideRunning)
            hideRunning = null
            hideAnimation?.cancel()
        }
        if (!isOpen && openRunning == null) {
            this.openRunning = Runnable {
                val animation = createVisibilityAnimator(true)
                        .apply {
                            addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationStart(animation: Animator) {
                                    isVisible = true
                                }

                                override fun onAnimationEnd(animation: Animator) {
                                    isVisible = true
                                    isOpen = true
                                    openRunning = null
                                    showAnimation = null
                                }
                            })
                        }
                this.showAnimation = animation
                animation.start()
            }
            postDelayed(openRunning, KEYBOARD_SHOW_DELAY_MILLIS.toLong())
        }
    }

    fun hide() {
        if (openRunning !=null || showAnimation != null) {
            removeCallbacks(openRunning)
            openRunning = null
            showAnimation?.cancel()
            showAnimation = null
        }
        if (isOpen && hideRunning == null) {
            this.hideRunning = Runnable {
                val animation = createVisibilityAnimator(false)
                        .apply {
                            addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    isVisible = false
                                    isOpen = false
                                    hideRunning = null
                                    hideAnimation = null
                                }
                            })
                        }
                this.hideAnimation = animation
                animation.start()
            }
            postDelayed(hideRunning, KEYBOARD_HIDE_DELAY_MILLIS.toLong())
        }
    }

    private fun createVisibilityAnimator(show: Boolean): Animator {
        val startY: Float
        val endY: Float
        if (show) {
            startY = keyboardHeightPx.toFloat()
            endY = 0f
        } else {
            startY = 0f
            endY = keyboardHeightPx.toFloat()
        }
        return ValueAnimator.ofFloat(startY, endY).apply {
            duration = KEYBOARD_ANIMATION_MILLIS.toLong()
            addUpdateListener {
                this@SecureKeyboard.translationY = it.animatedValue as Float
                animationListener?.update(show, animatedFraction)
            }
        }
    }


    companion object {
        private const val KEYBOARD_SHOW_DELAY_MILLIS = 100
        private const val KEYBOARD_ANIMATION_MILLIS = 200
        private const val KEYBOARD_HIDE_DELAY_MILLIS = 100

        private const val DEFAULT_KEYBOARD_HEIGHT_DP = 240
        private const val LANDSCAPE_KEYBOARD_HEIGHT_DP = 170
    }

    interface OnKeyClickListener {
        fun onKeyClick(keyCode: Int)
    }

    interface AnimationListener {
        fun update(isShow: Boolean, fraction: Float)
    }
}
