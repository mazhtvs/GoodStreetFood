import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.Test
import junit.framework.TestCase.assertEquals
import ru.tinkoff.acquiring.sdk.models.SectoralItemProps

@RunWith(Parameterized::class)
class SectoralItemPropsTest(
    private val input: String, private val expectedResult: Boolean
) {

    @Test fun testIsValidDateFormat() {
        assertEquals(expectedResult, SectoralItemProps.isValidDateFormat(input))
    }

    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{index}: isValidDateFormat({0})={1}") fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf("12.12.2020", true),
                arrayOf("13.13.2020", false),
                arrayOf("29.02.2020", true),
                arrayOf("29.02.2019", false),
                arrayOf("31.01.2020", true),
                arrayOf("31.04.2020", false),
                arrayOf("00.01.2020", false),
                arrayOf("-01.01.2020", false),
                arrayOf("01.01.2000", true),
                arrayOf("01.01.20", false),
                arrayOf("01.01.20000", false),
                arrayOf("28.02.2018", true),
                arrayOf("29.02.1900", false),
                arrayOf("29.02.2000", true)
            )
        }
    }
}
