package ru.tinkoff.acquiring.sdk.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Дробное количество маркированного товара
 *
 * @author Michael Babayan
 */
data class MarkQuantity(

    /**
     * Числитель дробной части предмета расчета.
     * Значение реквизита «числитель» должно быть строго меньше значения реквизита «знаменатель».
     * Не может равняться «0».
     */
    @SerializedName("Numerator")
    var numerator: Int? = null,

    /**
     * Знаменатель дробной части предмета расчета.
     * Заполняется значением, равным количеству товара в партии (упаковке), имеющей общий код маркировки товара.
     * Не может равняться «0».
     */
    @SerializedName("Denominator")
    var denominator: Int? = null
) : Serializable
