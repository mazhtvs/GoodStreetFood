package ru.tinkoff.acquiring.sdk.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Дополнительный реквизит пользователя (тег 1084)
 *
 * @author Michael Babayan
 */
data class AddUserProp(

    /**
     * Наименование дополнительного реквизита пользователя (тег 1085)
     */
    @SerializedName("Name")
    val name: String,

    /**
     * Значение дополнительного реквизита пользователя (тег1086)
     */
    @SerializedName("Value")
    val value: String

) : Serializable
