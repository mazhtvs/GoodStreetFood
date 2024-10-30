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
import ru.tinkoff.acquiring.sdk.cardscanners.delegate.CardScannerContract
import ru.tinkoff.acquiring.sdk.models.DarkThemeMode

/**
 * Настройки для конфигурирования визуального отображения и функций экранов SDK
 *
 * @author Mariya Chernyadieva
 */
class FeaturesOptions() : Options(), Parcelable {

    /**
     * Режим темной темы
     */
    var darkThemeMode: DarkThemeMode = DarkThemeMode.AUTO

    /**
     * Использовать безопасную клавиатуру для ввода данных карты
     */
    var useSecureKeyboard: Boolean = false

    /**
     * Контракт, для внедрения стороннего сканнера в приложение
     */
    var cameraCardScannerContract: CardScannerContract? = null

    /**
     * При выставлении параметра в true, введенный пользователем на форме оплаты email будет
     * продублирован в объект чека при отправке запроса Init.
     *
     * Не имеет эффекта если объект чека отсутствует.
     */
    var duplicateEmailToReceipt: Boolean = false

    /**
     * Если false то, не показываются стандартные шторки информирующие о финальных ошибках или
     * успехе операции, сразу возарвщвется результат.
     */
    var showPaymentNotifications: Boolean = true

    /**
     * Идентификатор карты в системе банка.
     * Если передан на экран оплаты - в списке карт на экране отобразится первой карта с этим cardId.
     * Если передан на экран списка карт - в списке карт отобразится выбранная карта.
     * Если не передан, или в списке нет карты с таким cardId -
     * список карт будет отображаться по-умолчанию
     */
    internal var selectedCardId: String? = null

    private constructor(parcel: Parcel) : this() {
        parcel.run {
            useSecureKeyboard = readByte().toInt() != 0
            cameraCardScannerContract = readSerializable() as CardScannerContract?
            darkThemeMode = DarkThemeMode.valueOf(readString() ?: DarkThemeMode.AUTO.name)
            selectedCardId = readString()
            duplicateEmailToReceipt = readByte().toInt() != 0
            showPaymentNotifications = readByte().toInt() != 0
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.run {
            writeByte((if (useSecureKeyboard) 1 else 0).toByte())
            writeSerializable(cameraCardScannerContract)
            writeString(darkThemeMode.name)
            writeString(selectedCardId)
            writeByte((if (duplicateEmailToReceipt) 1 else 0).toByte())
            writeByte((if (showPaymentNotifications) 1 else 0).toByte())
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun validateRequiredFields() {
        //class have not required fields
    }

    companion object CREATOR : Parcelable.Creator<FeaturesOptions> {
        override fun createFromParcel(parcel: Parcel): FeaturesOptions {
            return FeaturesOptions(parcel)
        }

        override fun newArray(size: Int): Array<FeaturesOptions?> {
            return arrayOfNulls(size)
        }
    }
}
