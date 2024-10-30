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

package ru.tinkoff.acquiring.sdk.models

import com.google.gson.annotations.SerializedName
import ru.tinkoff.acquiring.sdk.models.enums.Taxation
import ru.tinkoff.acquiring.sdk.utils.builders.ReceiptBuilder.*
import java.io.Serializable
import java.util.*

/**
 * Данные чека
 *
 * @author Mariya Chernyadieva
 */
enum class FfdVersion(val value: String) {
    VERSION1_05("1.05"), VERSION1_2("1.2")
}

sealed class Receipt : Serializable {

    abstract val ffdVersion: String

    abstract var email: String?
}

/**
 * По умолчанию версия ФФД - '1.05'
 */
data class ReceiptFfd105(

    /**
     * Код магазина
     */
    @SerializedName("ShopCode")
    var shopCode: String? = null,

    /**
     * Электронный адрес для отправки чека покупателю.
     * Параметр `email` или `phone` должен быть заполнен.
     */
    @SerializedName("Email")
    override var email: String? = null,

    /**
     * Телефон покупателя.
     * Параметр `email` или `phone` должен быть заполнен.
     */
    @SerializedName("Phone")
    var phone: String? = null,

    /**
     * Система налогообложения.
     */
    @SerializedName("Taxation")
    var taxation: Taxation,

    /**
     * Массив, содержащий в себе информацию о товарах.
     */
    @SerializedName("Items")
    var items: MutableList<Item105>,

    /**
     * Данные агента.
     */
    @SerializedName("AgentData")
    var agentData: AgentData? = null,

    /**
     * Данные поставщика платежного агента.
     */
    @SerializedName("SupplierInfo")
    var supplierInfo: SupplierInfo? = null,

    /**
     * Версия фискализации
     */
    @SerializedName("FfdVersion")
    override val ffdVersion: String = FfdVersion.VERSION1_05.value,

    ) : Receipt() {

    private constructor(builder: ReceiptBuilder105) : this(
        taxation = builder.taxation,
        phone = builder.phone,
        email = builder.email,
        items = builder.items
    )

    companion object {
        inline fun receipt105(taxation: Taxation, block: ReceiptBuilder105.() -> Unit) =
            ReceiptBuilder105(taxation).apply(block).build()
    }
}

data class ReceiptFfd12(

    /**
     * Информация о клиенте.
     */
    @SerializedName("ClientInfo")
    var clientInfo: ClientInfo? = null,

    /**
     * Система налогообложения.
     */
    @SerializedName("Taxation")
    var taxation: Taxation,

    /**
     * Электронный адрес для отправки чека покупателю.
     * Параметр `email` или `phone` должен быть заполнен.
     */
    @SerializedName("Email")
    override var email: String? = null,

    /**
     * Телефон покупателя.
     * Параметр `email` или `phone` должен быть заполнен.
     */
    @SerializedName("Phone")
    var phone: String? = null,

    /**
     * Идентификатор покупателя.
     */
    @SerializedName("Customer")
    var customer: String? = null,

    /**
     * Инн покупателя.
     */
    @SerializedName("CustomerInn")
    var customerInn: String? = null,

    /**
     * Массив, содержащий в себе информацию о товарах.
     */
    @SerializedName("Items")
    var items: MutableList<Item12>,

    /**
     * Детали платежа.
     */
    @SerializedName("Payments")
    var payments: Payments? = null,

    /**
     * Операционный реквизит чека.
     */
    @SerializedName("OperatingСheckProps")
    var operatingCheckPros: OperatingCheckProps? = null,

    /**
     * Отраслевой реквизит чека.
     */
    @SerializedName("SectoralCheckProps")
    var sectoralCheckProps: SectoralCheckProps? = null,

    /**
     * Дополнительный реквизит пользователя.
     */
    @SerializedName("AddUserProp")
    var addUserProp: AddUserProp? = null,

    /**
     * Дополнительный реквизит чека (БСО).
     */
    @SerializedName("AdditionalCheckProps")
    var additionalCheckProps: String? = null,

    /**
     * Версия фискализации
     */
    @SerializedName("FfdVersion")
    override val ffdVersion: String = FfdVersion.VERSION1_2.value

) : Receipt() {

    private constructor(builder: ReceiptBuilder12) : this(
        clientInfo = builder.clientInfo,
        taxation = builder.taxation,
        phone = builder.phone,
        email = builder.email,
        items = builder.items
    )

    companion object {
        inline fun receipt12(
            taxation: Taxation,
            clientInfo: ClientInfo,
            block: ReceiptBuilder12.() -> Unit
        ) = ReceiptBuilder12(taxation, clientInfo).apply(block).build()
    }
}