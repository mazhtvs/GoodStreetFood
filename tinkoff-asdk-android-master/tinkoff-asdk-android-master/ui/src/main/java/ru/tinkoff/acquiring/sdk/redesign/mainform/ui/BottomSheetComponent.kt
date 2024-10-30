package ru.tinkoff.acquiring.sdk.redesign.mainform.ui

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.android.awaitFrame

/**
 * Created by i.golovachev
 */
internal class BottomSheetComponent(
    private val root: CoordinatorLayout,
    private val sheet: View,
    private val bottomSheetBehavior: BottomSheetBehavior<View> = BottomSheetBehavior.from(sheet),
    private val onSheetHidden: () -> Unit
) {

    private var contentHeight: Int = 0

    init {
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> onSheetHidden()
                    else -> Unit
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        })
        setRootOnTouchListener()
    }

    fun collapse() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun expand() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun onAttachedToWindow() {
        expand()
    }

    suspend fun trimSheetToContent(measuredView: View) {
        awaitFrame()
        awaitFrame()
        val insets = ViewCompat.getRootWindowInsets(sheet)
        val bottom = insets!!.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        val measuredHeight = measuredView.measuredHeight + bottom
        contentHeight = measuredHeight
        bottomSheetBehavior.setPeekHeight(measuredHeight, true)
    }

    private fun setRootOnTouchListener(){
        root.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val sheetRect = Rect()
                sheet.getGlobalVisibleRect(sheetRect)
                if (!sheetRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
}
