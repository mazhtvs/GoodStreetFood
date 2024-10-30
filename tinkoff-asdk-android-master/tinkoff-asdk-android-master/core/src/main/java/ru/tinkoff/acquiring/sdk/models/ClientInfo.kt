package ru.tinkoff.acquiring.sdk.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Информация о покупателе (Обязательно для FfdVersion =1.2)
 *
 * @author Michael Babayan
 */
data class ClientInfo(

    /**
     * Дата рождения в формате ДД.ММ.ГГГГ
     */
    @SerializedName("Birthdate")
    var birthdate: String? = null,

    /**
     * Числовой код страны по ОКСМ
     */
    @SerializedName("Citizenship")
    var citizenship: String? = null,

    /**
     * Код вида документа, удостоверяющего личность
     */
    @SerializedName("DocumentCode")
    var documentCode: String? = null,

    /**
     * Реквизиты документа (например, серия и номер паспорта)
     */
    @SerializedName("DocumentData")
    var documentData: String? = null,

    /**
     * Адрес покупателя или грузополучателя
     */
    @SerializedName("Address")
    var address: String? = null

) : Serializable
