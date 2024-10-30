package ru.tinkoff.acquiring.sdk.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Операционный реквизит чека (тег 1270)
 *
 * @author Michael Babayan
 */
data class OperatingCheckProps(

    /**
     * Идентификатор операции (тег 1271)
     */
    @SerializedName("Name")
    val name: String,

    /**
     * Данные операции (тег 1272)
     */
    @SerializedName("Value")
    val value: String,

    /**
     * Дата и время операции в формате ДД.ММ.ГГГГ ЧЧ:ММ:СС (тег 1273)
     */
    @SerializedName("Timestamp")
    val timestamp: String

): Serializable