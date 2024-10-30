package ru.tinkoff.acquiring.sdk.models

import com.google.gson.annotations.SerializedName
import ru.tinkoff.acquiring.sdk.models.enums.PaymentMethod
import ru.tinkoff.acquiring.sdk.models.enums.PaymentObject105
import ru.tinkoff.acquiring.sdk.models.enums.Tax
import java.io.Serializable

/**
 * Информация о товаре
 *
 * @author Michael Babayan
 */
data class Item105(
    /**
     * Наименование товара. Максимальная длина строки – 64 символова.
     */
    @SerializedName("Name")
    var name: String,

    /**
     * Сумма в копейках. Целочисленное значение не более 10 знаков.
     */
    @SerializedName("Price")
    var price: Long = 0,

    /**
     * Количество/вес - целая часть не более 8 знаков, дробная часть не более 3 знаков.
     */
    @SerializedName("Quantity")
    var quantity: Double = 0.0,

    /**
     * Сумма в копейках. Целочисленное значение не более 10 знаков.
     */
    @SerializedName("Amount")
    var amount: Long,

    /**
     * Ставка налога
     */
    @SerializedName("Tax")
    var tax: Tax,

    /**
     * Штрих-код.
     */
    @SerializedName("Ean13")
    var ean13: String? = null,

    /**
     * Код магазина. Необходимо использовать значение параметра Submerchant_ID, полученного в ответ при регистрации магазинов через xml. Если xml не используется, передавать поле не нужно.
     */
    @SerializedName("ShopCode")
    var shopCode: String? = null,

    /**
     * Тип оплаты
     */
    @SerializedName("PaymentMethod")
    var paymentMethod: PaymentMethod? = null,

    /**
     * Признак предмета расчета
     */
    @SerializedName("PaymentObject")
    var paymentObject: PaymentObject105? = null,

    /**
     * Данные агента
     */
    @SerializedName("AgentData")
    var agentData: AgentData? = null,

    /**
     * Данные поставщика платежного агента
     */
    @SerializedName("SupplierInfo")
    var supplierInfo: SupplierInfo? = null,

) : Serializable, Item