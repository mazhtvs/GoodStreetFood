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

package ru.tinkoff.acquiring.sdk.models.options

import android.os.Parcel
import android.os.Parcelable
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringSdkException
import ru.tinkoff.acquiring.sdk.models.enums.CheckType
import ru.tinkoff.acquiring.sdk.utils.readParcelMap

private val defaultCheckType = CheckType.NO.toString()

/**
 * Данные покупателя
 *
 * @author Mariya Chernyadieva
 */
class CustomerOptions() : Options(), Parcelable {

    /**
     * Идентификатор покупателя в системе продавца. Максимальная длина - 36 символов
     */
    var customerKey: String? = null

    /**
     * Тип привязки карты.
     */
    var checkType: String = defaultCheckType

    /**
     * Email, на который будет отправлена квитанция об оплате
     */
    var email: String? = null

    /**
     * Объект содержащий дополнительные параметры в виде "ключ":"значение".
     * Данные параметры будут переданы в запросе платежа/привязки карты.
     * Максимальная длина для каждого передаваемого параметра:
     * Ключ – 20 знаков,
     * Значение – 100 знаков.
     * Максимальное количество пар "ключ-значение" не может превышать 20
     */
    var data: Map<String, String>? = null

    private constructor(parcel: Parcel) : this() {
        parcel.run {
            customerKey = readString()
            checkType = readString() ?: defaultCheckType
            email = readString()
            data = readParcelMap(String::class.java)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.run {
            writeString(customerKey)
            writeString(checkType)
            writeString(email)
            writeMap(data)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    @Throws(AcquiringSdkException::class)
    override fun validateRequiredFields() {
        check(if (customerKey == null) true else checkType != null) {
            "Check Type is required, when Customer Key was set"
        }
    }

    companion object CREATOR : Parcelable.Creator<CustomerOptions> {
        override fun createFromParcel(parcel: Parcel): CustomerOptions {
            return CustomerOptions(parcel)
        }

        override fun newArray(size: Int): Array<CustomerOptions?> {
            return arrayOfNulls(size)
        }
    }
}
