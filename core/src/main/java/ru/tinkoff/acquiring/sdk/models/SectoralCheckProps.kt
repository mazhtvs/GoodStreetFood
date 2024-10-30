package ru.tinkoff.acquiring.sdk.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Отраслевой реквизит чека
 *
 * @author Michael Babayan
 */
data class SectoralCheckProps(
    /**
     * Идентификатор ФОИВ (тег 1262).
     * Максимальное количество символов – 3.
     */
    @SerializedName("FederalId")
    val federalId: String,

    /**
     * Дата документа основания в формате ДД.ММ.ГГГГ (тег 1263).
     */
    @SerializedName("Date")
    val date: String,

    /**
     * Номер документа основания (тег 1264).
     */
    @SerializedName("Number")
    val number: String,

    /**
     * Значение отраслевого реквизита (тег 1265).
     */
    @SerializedName("Value")
    val value: String

): Serializable
