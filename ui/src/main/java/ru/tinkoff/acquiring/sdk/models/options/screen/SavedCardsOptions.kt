package ru.tinkoff.acquiring.sdk.models.options.screen

import android.os.Parcel
import android.os.Parcelable
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring

/**
 * Настройки экрана сохраненных карт
 *
 * @author Mariya Chernyadieva
 */
class SavedCardsOptions : BaseCardsOptions<SavedCardsOptions>, Parcelable {

    var mode: Mode = Mode.LIST
    var withArrowBack: Boolean = false

    /**
     * Флаг, разрешающий добавление новой карты или оплату новой картой
     */
    var allowNewCard: Boolean = true

    /**
     * Флаг, активирующий отображение только кард, для которых возможен рекуррентный платеж
     */
    var showOnlyRecurrentCards: Boolean = false

    /**
     * [TinkoffAcquiring.savedCardsOptions]
     */
    internal constructor() : super()

    private constructor(parcel: Parcel) : super(parcel) {
        parcel.run {
            allowNewCard = readByte().isTrue()
            withArrowBack = readByte().isTrue()
            mode = readString()?.let { Mode.valueOf(it) } ?: Mode.LIST
            showOnlyRecurrentCards = readByte().isTrue()
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.run {
            writeByte(allowNewCard.toByte())
            writeByte(withArrowBack.toByte())
            writeString(mode.name)
            writeByte(showOnlyRecurrentCards.toByte())
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun setOptions(options: SavedCardsOptions.() -> Unit): SavedCardsOptions {
        return SavedCardsOptions().apply(options)
    }

    companion object CREATOR : Parcelable.Creator<SavedCardsOptions> {

        override fun createFromParcel(parcel: Parcel): SavedCardsOptions {
            return SavedCardsOptions(parcel)
        }

        override fun newArray(size: Int): Array<SavedCardsOptions?> {
            return arrayOfNulls(size)
        }
    }

    enum class Mode {
        /**
         * Режим отображения списка карт
         */
        LIST,

        /**
         * Режим выбора карты для оплаты
         */
        PAYMENT
    }
}
