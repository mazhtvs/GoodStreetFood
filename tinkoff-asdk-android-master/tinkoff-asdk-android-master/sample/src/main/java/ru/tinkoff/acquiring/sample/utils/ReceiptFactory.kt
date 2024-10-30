package ru.tinkoff.acquiring.sample.utils

import ru.tinkoff.acquiring.sdk.models.ClientInfo
import ru.tinkoff.acquiring.sdk.models.FfdVersion
import ru.tinkoff.acquiring.sdk.models.Item105
import ru.tinkoff.acquiring.sdk.models.Item12
import ru.tinkoff.acquiring.sdk.models.Receipt
import ru.tinkoff.acquiring.sdk.models.ReceiptFfd105
import ru.tinkoff.acquiring.sdk.models.ReceiptFfd12
import ru.tinkoff.acquiring.sdk.models.SectoralItemProps
import ru.tinkoff.acquiring.sdk.models.enums.PaymentMethod
import ru.tinkoff.acquiring.sdk.models.enums.PaymentObject105
import ru.tinkoff.acquiring.sdk.models.enums.PaymentObject12
import ru.tinkoff.acquiring.sdk.models.enums.Tax
import ru.tinkoff.acquiring.sdk.models.enums.Taxation
import ru.tinkoff.acquiring.sdk.utils.Money

/**
 * Created by v.budnitskiy
 */
interface ReceiptFactory {
    fun getReceipt(): Receipt
    class Impl(private val ffdVersion: FfdVersion) : ReceiptFactory {
        override fun getReceipt() = when (ffdVersion) {
            FfdVersion.VERSION1_05 -> {
                ReceiptFfd105.receipt105(taxation = Taxation.OSN) {
                    val totalPrice = Money.ofRubles(20.99)
                    phone = "89132221144"
                    email = "test@mail.ru"
                    items = mutableListOf(
                        Item105(
                            name = "Товарное имя",
                            price = totalPrice.coins,
                            amount = totalPrice.coins,
                            tax = Tax.VAT_10,
                            quantity = 1.0,
                            ean13 = "123456789012",
                            shopCode = "Магазин_001",
                            paymentMethod = PaymentMethod.FULL_PAYMENT,
                            paymentObject = PaymentObject105.PAYMENT
                        )
                    )
                }
            }

            FfdVersion.VERSION1_2 -> {
                ReceiptFfd12.receipt12(
                    taxation = Taxation.OSN, clientInfo = ClientInfo(
                        birthdate = "21.11.1995",
                        citizenship = "643",
                        documentCode = "40",
                        documentData = "4507 443564",
                        address = "Gomel",
                    )
                ) {
                    val totalPrice = Money.ofRubles(20.99)
                    phone = "89132221144"
                    email = "test@mail.ru"
                    items = mutableListOf(
                        Item12(
                            name = "Товарное имя",
                            price = totalPrice.coins,
                            amount = totalPrice.coins,
                            excise = 12.2,
                            tax = Tax.VAT_10,
                            measurementUnit = "шт",
                            quantity = 1.0,
                            paymentMethod = PaymentMethod.FULL_PAYMENT,
                            paymentObject = PaymentObject12.PAYMENT,
                            sectoralItemProps = listOf(
                                SectoralItemProps(
                                    federalId = "001",
                                    date = "21.11.2020",
                                    number = "test value SectoralItemProps",
                                    value = "001",
                                )
                            )
                        )
                    )
                }
            }
        }
    }
}