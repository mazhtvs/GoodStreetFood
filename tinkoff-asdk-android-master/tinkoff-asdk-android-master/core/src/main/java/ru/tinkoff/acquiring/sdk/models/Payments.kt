package ru.tinkoff.acquiring.sdk.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Класс данных, представляющий различные формы платежей.
 *
 * @author Michael Babayan
 */
data class Payments (

    /**
     * Вид оплаты "Наличные". Сумма к оплате в копейках
     */
    @SerializedName("Cash")
    val cash: Int? = null,

    /**
     * Вид оплаты "Безналичный".
     */
    @SerializedName("Electronic")
    val electronic: Int,

    /**
     * Вид оплаты "Предварительная оплата (Аванс)".
     */
    @SerializedName("AdvancePayment")
    val advancePayment: Int? = null,

    /**
     * Вид оплаты "Постоплата (Кредит)".
     */
    @SerializedName("Credit")
    val credit: Int? = null,

    /**
     * Вид оплаты "Иная форма оплаты".
     */
    @SerializedName("Provision")
    val provision: Int? = null

): Serializable
