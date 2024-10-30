package ru.tinkoff.acquiring.sdk.network

import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.network.AcquiringApi.API_URL_DEBUG
import ru.tinkoff.acquiring.sdk.network.AcquiringApi.API_URL_RELEASE
import ru.tinkoff.acquiring.sdk.network.AcquiringApi.API_VERSION

/**
 * @author s.y.biryukov
 */
class AcquiringApiTest {

    @Before
    fun reset() {
        AcquiringSdk.customUrl = null
        AcquiringSdk.isDeveloperMode = false
    }

    @Test
    fun `test base url for method without configuration`() {
        val url = AcquiringApi.getUrl()

        assertEquals(API_URL_RELEASE, url)
    }

    @Test
    fun `test base url for method with developer mode configuration`() {
        AcquiringSdk.isDeveloperMode = true

        val url = AcquiringApi.getUrl()

        assertEquals(API_URL_DEBUG, url)
    }

    @Test
    fun `test custom base url without AcquiringApi#API_VERSION suffix for method with developer mode configuration`() {
        val customUrl = "http://customurl"
        AcquiringSdk.isDeveloperMode = true
        AcquiringSdk.customUrl = customUrl

        val url = AcquiringApi.getUrl()

        assertEquals("$customUrl/$API_VERSION", url)
    }

    @Test
    fun `test custom base url with AcquiringApi#API_VERSION suffix for method with developer mode configuration`() {
        val customUrl = "http://customurl/$API_VERSION"
        AcquiringSdk.isDeveloperMode = true
        AcquiringSdk.customUrl = customUrl

        val url = AcquiringApi.getUrl()

        assertEquals(customUrl, url)
    }
}
