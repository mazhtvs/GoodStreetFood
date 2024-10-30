package ru.tinkoff.acquiring.sdk.models.paysources

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author s.y.biryukov
 */
class CardDataTest {

    @Test
    fun `test string format for pan, expire date and cvc`() {
        val cardData = CardData().apply {
            pan = "0000000000000000"
            expiryDate = "07/30"
            securityCode = "321"
        }

        val stringForEncoding = cardData.buildStringForEncoding()

        assertEquals("PAN=0000000000000000;ExpDate=0730;CVV=321", stringForEncoding)
    }
}
