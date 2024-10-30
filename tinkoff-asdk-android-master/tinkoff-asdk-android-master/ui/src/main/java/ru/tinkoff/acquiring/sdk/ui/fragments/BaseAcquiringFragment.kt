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

package ru.tinkoff.acquiring.sdk.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.localization.ASDKString
import ru.tinkoff.acquiring.sdk.models.PaymentSource
import ru.tinkoff.acquiring.sdk.models.paysources.CardSource
import java.util.regex.Pattern

/**
 * @author Mariya Chernyadieva
 */
internal open class BaseAcquiringFragment : Fragment() {

    companion object {
        private const val EMAIL_PATTERN = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    protected fun validateInput(paymentSource: PaymentSource, email: String? = null): Boolean {
        if (paymentSource is CardSource) {
            try {
                paymentSource.validate()
            } catch (e: IllegalStateException) {
                Toast.makeText(
                        requireActivity(),
                        getString(ASDKString.acq_pay_dialog_validation_invalid_card),
                        Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }

        if (email != null && (email.isEmpty() || !Pattern.compile(EMAIL_PATTERN).matcher(email).matches())) {
            Toast.makeText(
                    requireActivity(),
                    getString(ASDKString.acq_pay_dialog_validation_invalid_email),
                    Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    protected fun TextView.resolveScroll() {
        movementMethod = ScrollingMovementMethod()
        setOnTouchListener { _, _ ->
            val canScroll = canScrollVertically(1) || canScrollVertically(-1)
            parent.requestDisallowInterceptTouchEvent(canScroll)
            false
        }
    }

    protected fun modifySpan(amount: String): CharSequence {
        val amountSpan = SpannableString(amount)
        val commaIndex = amount.indexOf(",")

        return if (commaIndex < 0) {
            amount
        } else {
            val coinsColor = ContextCompat.getColor(requireContext(), R.color.acq_colorCoins)
            amountSpan.setSpan(ForegroundColorSpan(coinsColor), commaIndex + 1, amount.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            amountSpan
        }
    }
}
