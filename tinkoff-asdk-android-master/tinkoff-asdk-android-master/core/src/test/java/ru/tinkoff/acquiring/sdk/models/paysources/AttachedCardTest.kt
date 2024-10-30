package ru.tinkoff.acquiring.sdk.models.paysources

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author s.y.biryukov
 */
class AttachedCardTest {

    @Test
    fun `test string format for card id and cvc`() {
        val attachedCard = AttachedCard().apply {
            cardId = "000000"
            cvc = "123"
        }

        val stringForEncoding = attachedCard.buildStringForEncoding()

        assertEquals("CardId=000000;CVV=123", stringForEncoding)
    }

    @Test
    fun `test string format for rebillId encoding`() {
        val attachedCard = AttachedCard().apply {
            cardId = "000000"
            cvc = "123"
            rebillId = "222222"
        }

        val stringForEncoding = attachedCard.buildStringForEncoding()

        assertEquals("222222", stringForEncoding)
    }
}

