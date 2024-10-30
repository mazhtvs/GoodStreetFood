package ru.tinkoff.acquiring.sdk.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Реквизит предусмотренный НПА
 *
 * @author Michael Babayan
 */
data class SectoralItemProps(
    /**
     * Идентификатор ФОИВ (федеральный орган исполнительной власти)
     */
    @SerializedName("FederalId")
    var federalId: String,
    /**
     *
     * Дата нормативного акта ФОИВ
     * Значение в формате в формате ДД.ММ.ГГГГ
     */
    @SerializedName("Date")
    var date: String,

    /**
     * Номер нормативного акта ФОИВ, регламентирующего порядок заполнения реквизита «значение отраслевого реквизита»
     */
    @SerializedName("Number")
    var number: String,

    /**
     * Состав значений, определенных нормативного актом ФОИВ.
     */
    @SerializedName("Value")
    var value: String
) : Serializable {

    companion object {

        private val DATE_PATTERN = """\d{2}\.\d{2}\.\d{4}""".toRegex()

        fun isValidDateFormat(date: String): Boolean {
            if (!DATE_PATTERN.matches(date)) {
                return false
            }

            val parts = date.split(".")
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()

            if (month !in 1..12) return false

            val maxDay = when (month) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
                else -> return false
            }

            if (day !in 1..maxDay) return false

            return true
        }
    }
}