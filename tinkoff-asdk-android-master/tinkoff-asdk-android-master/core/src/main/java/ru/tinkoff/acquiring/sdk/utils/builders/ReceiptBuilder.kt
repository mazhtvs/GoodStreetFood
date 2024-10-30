package ru.tinkoff.acquiring.sdk.utils.builders

import ru.tinkoff.acquiring.sdk.models.ClientInfo
import ru.tinkoff.acquiring.sdk.models.Item105
import ru.tinkoff.acquiring.sdk.models.Item12
import ru.tinkoff.acquiring.sdk.models.Receipt
import ru.tinkoff.acquiring.sdk.models.ReceiptFfd105
import ru.tinkoff.acquiring.sdk.models.ReceiptFfd12
import ru.tinkoff.acquiring.sdk.models.enums.Taxation

/**
 * Класс для построения объектов чека с предоставленными параметрами и списком товаров.
 * Предоставляет высокоуровневый интерфейс для создания различных видов чеков.
 *
 * @author Michael Babayan
 */
sealed class ReceiptBuilder<T : Receipt, I> {
    abstract fun build(): T

    class ReceiptBuilder105(val taxation: Taxation) : ReceiptBuilder<ReceiptFfd105, Item105>() {
        var phone: String? = null
        var email: String? = null
        var items: MutableList<Item105> = mutableListOf()

        fun addItems(vararg itemsToAdd: Item105) = apply { this.items.addAll(itemsToAdd) }

        override fun build(): ReceiptFfd105 {
            return ReceiptFfd105(
                taxation = taxation,
                phone = phone,
                email = email,
                items = items
            )
        }
    }

    class ReceiptBuilder12(val taxation: Taxation, val clientInfo: ClientInfo) : ReceiptBuilder<ReceiptFfd12, Item12>() {
        var phone: String? = null
        var email: String? = null
        var items: MutableList<Item12> = mutableListOf()

        fun addItems(vararg itemsToAdd: Item12) = apply { this.items.addAll(itemsToAdd) }

        override fun build(): ReceiptFfd12 {
            return ReceiptFfd12(
                taxation = taxation,
                phone = phone,
                email = email,
                items = items,
                clientInfo = clientInfo
            )
        }
    }
}