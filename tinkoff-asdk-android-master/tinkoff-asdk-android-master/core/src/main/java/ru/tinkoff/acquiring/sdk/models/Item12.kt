package ru.tinkoff.acquiring.sdk.models

import com.google.gson.annotations.SerializedName
import ru.tinkoff.acquiring.sdk.models.enums.PaymentMethod
import ru.tinkoff.acquiring.sdk.models.enums.PaymentObject12
import ru.tinkoff.acquiring.sdk.models.enums.Tax
import java.io.Serializable

/**
 * Информация о товаре
 *
 * @author Michael Babayan
 */
data class Item12(

    /**
     * Сумма в копейках. Целочисленное значение не более 10 знаков
     */
    @SerializedName("Price")
    var price: Long = 0,

    /**
     * Количество/вес. Целая часть не более 8 знаков
     */
    @SerializedName("Quantity")
    var quantity: Double = 0.0,

    /**
     * Наименование товара. Максимальная длина строки – 128 символов
     */
    @SerializedName("Name")
    var name: String? = null,

    /**
     * Сумма в копейках. Целочисленное значение не более 10 знаков
     */
    @SerializedName("Amount")
    var amount: Long? = null,

    /**
     * Ставка налога
     */
    @SerializedName("Tax")
    var tax: Tax? = null,

    /**
     * Тип оплаты
     */
    @SerializedName("PaymentMethod")
    var paymentMethod: PaymentMethod? = null,

    /**
     * Признак предмета расчета
     */
    @SerializedName("PaymentObject")
    var paymentObject: PaymentObject12? = null,

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

    /**
     * Дополнительный реквизит предмета расчета.
     */
    @SerializedName("UserData")
    var userData: String? = null,

    /**
     * Сумма акциза в рублях с учетом копеек, включенная в стоимость предмета расчета.
     */
    @SerializedName("Excise")
    var excise: Double? = null,

    /**
     * Цифровой код страны происхождения товара в соответствии с Общероссийским классификатором стран мира (3 цифры).
     */
    @SerializedName("CountryCode")
    var countryCode: String? = null,

    /**
     * Номер таможенной декларации (32 цифры максимум).
     */
    @SerializedName("DeclarationNumber")
    var declarationNumber: String? = null,

    /**
     * Обозначение единицы измерения в соответствии с метрическими системами на русском или английском
     */
    @SerializedName("MeasurementUnit")
    var measurementUnit: String,

    /**
     * Режим обработки кода маркировки.
     */
    @SerializedName("MarkProcessingMode")
    var markProcessingMode: String? = null,

    /**
     * Данные маркировки
     */
    @SerializedName("MarkCode")
    var markCode: MarkCode? = null,

    /**
     * Реквизит «дробное количество маркированного товара»
     */
    @SerializedName("MarkQuantity")
    var markQuantity: MarkQuantity? = null,

    /**
     * Реквизит предусмотренный НПА
     */
    @SerializedName("SectoralItemProps")
    var sectoralItemProps: List<SectoralItemProps>? = null

) : Serializable, Item