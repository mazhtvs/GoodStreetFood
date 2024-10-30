package ru.tinkoff.acquiring.sdk.ui.customview.editcard.keyboard

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import ru.tinkoff.acquiring.sdk.R
import java.lang.ref.WeakReference

/**
 *  Контроллер для управления отображением защищенной клавиатурой
 *
 * @author s.y.biryukov
 */
internal class SecureKeyboardController private constructor() {
    private var insetsView: WeakReference<View> = WeakReference(null)
    private var secureKeyboardHeight: Int = 0
    private var containerPadding: Int = 0
    private var systemKeyboardInset: Int = 0
    private var navigationInset: Int = 0
    private var keyClickListenerRef: SecureKeyboard.OnKeyClickListener? = null
    private var contentContainerRef: WeakReference<View> = WeakReference(null)
    private var keyboardStateListener: KeyboardStateListener? = null

    /**
     * @param container контейнер который необходимо смещать при появлении клавиатуры
     */
    fun setContentContainer(container: View?) {
        this.contentContainerRef = WeakReference(container)
        this.containerPadding = container?.paddingBottom ?: 0
        container?.setTag(R.id.acq_padding_tag, containerPadding)
    }

    fun registerInsets(view: View) {
        this.insetsView = WeakReference(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) {
            view, insets ->
                handleInsets(insets, view)
                insets
        }

        ViewCompat.requestApplyInsets(view)
    }

    fun setKeyListener(listener: SecureKeyboard.OnKeyClickListener?) {
        this.keyClickListenerRef =listener
    }

    fun setKeyboardStateListener(listener: KeyboardStateListener?) {
        this.keyboardStateListener = listener
    }

    fun clear() {
        hide()
        val initialPadding = this.contentContainerRef.get()?.getTag(R.id.acq_padding_tag) as? Int ?: 0
        setContainerBottomPadding(initialPadding)
        this.containerPadding = 0
        this.contentContainerRef.clear()
        this.keyClickListenerRef = null
        this.keyboardStateListener = null
        this.systemKeyboardInset = 0
        this.secureKeyboardHeight = 0
        this.navigationInset = 0
        val insetView = this.insetsView.get()
        if (insetView != null) {
           ViewCompat.setOnApplyWindowInsetsListener(insetView, null)
        }
    }

    /**
     * @param field поле для которого отображается клавиатура
     */
    fun show(field: View?) {
        hideSystemKeyboard(field)
        val keyboard: SecureKeyboard = requireKeyboard(field)
        keyboard.keyClickListener = this.keyClickListenerRef
        setKeyboardAnimationListenerIfNeed(keyboard)
        keyboard.show()
    }

    /**
     * @param field поле для которого скрывается клавиатура
     */
    fun hide(field: View? = null) {
        val requireKeyboard = requireKeyboard(field)
        requireKeyboard.hide()
    }

    private fun hideSystemKeyboard(view: View?) {
        val context = requireContext(view)
        (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow((context as Activity).window.decorView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
    }

    private fun setKeyboardAnimationListenerIfNeed(keyboard: SecureKeyboard) {
        val listener = object : SecureKeyboard.AnimationListener {
            override fun update(isShow: Boolean, fraction: Float) {
                val fr = if (isShow) fraction else (1 - fraction)
                this@SecureKeyboardController.secureKeyboardHeight = (keyboard.keyboardHeightPx * fr).toInt()
                onKeyboardHeightChanged(true)
            }
        }
        keyboard.animationListener = listener
    }



    private fun setContainerBottomPadding(bottomPadding: Int) {
        if (keyboardStateListener?.onPaddingUpdated(bottomPadding, navigationInset) != true) {
            val contentContainer = contentContainerRef.get() ?: return
            contentContainer.updatePadding(bottom = bottomPadding)
        }
    }

    private fun setContainerBottomPaddingForKeyboard(keyboardHeight: Int) {
        val padding = if (containerPadding >= keyboardHeight + navigationInset) {
            containerPadding
        } else {
            containerPadding + keyboardHeight + navigationInset
        }
        setContainerBottomPadding(padding)
    }

    /**
     * Получает ссылку на имеющуюся клавиатуру или создает новую
     */
    private fun requireKeyboard(field: View?): SecureKeyboard {
        val context = requireContext(field)
        val root: ViewGroup = (context as Activity).window.decorView.findViewById(android.R.id.content)
        val attachedKeyboard: SecureKeyboard? = root.findViewById(keyboardId)
        val keyboard: SecureKeyboard = attachedKeyboard ?: attachKeyboard(root)
        return keyboard
    }

    private fun requireContext(field: View?): Context {
        val context = field?.context ?: contentContainerRef.get()?.context
        ?: insetsView.get()?.context
        requireNotNull(context) { "Не удалось получить контекст" }
        return context
    }

    private fun attachKeyboard(
        root: ViewGroup
    ): SecureKeyboard {
        val newKeyboard = SecureKeyboard(root.context)
            .apply {
                id = keyboardId
                visibility = View.GONE
            }
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM
        }
        root.addView(newKeyboard, params)
        return newKeyboard
    }

    private fun handleInsets(insets: WindowInsetsCompat, view: View) {
        val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        val navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        onSystemKeyboardInsetsChanged(view, imeBottom, navBottom)
    }

    private fun onSystemKeyboardInsetsChanged(view: View, keyboard: Int, navigation: Int) {
        this.systemKeyboardInset = keyboard
        this.navigationInset = navigation
        val secureKeyboard = requireKeyboard(view)
        if (!secureKeyboard.isShowing()) {
            onKeyboardHeightChanged(false)
        }
    }

    private fun onKeyboardHeightChanged(isSecure: Boolean) {
        val bottomSpace = maxOf(secureKeyboardHeight, (systemKeyboardInset - navigationInset).coerceAtLeast(0))
        setContainerBottomPaddingForKeyboard(bottomSpace)
    }

    companion object {
        private val keyboardId = R.id.edit_card_secure_keyboard
        private val lazyInstance: SecureKeyboardController by lazy { SecureKeyboardController() }
        fun getInstance(): SecureKeyboardController = lazyInstance
    }

    interface KeyboardStateListener{
        fun onPaddingUpdated(height: Int, navigationHeight: Int) : Boolean
    }
}
