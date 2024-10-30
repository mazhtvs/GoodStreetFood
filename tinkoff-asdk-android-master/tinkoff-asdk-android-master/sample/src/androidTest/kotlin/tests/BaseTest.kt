package tests

import androidx.appcompat.app.AppCompatActivity
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import io.qameta.allure.android.rules.ScreenshotRule
import org.junit.Before
import org.junit.Rule
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.core.testing.kaspresso.tests.BaseKaspressoTest
import ru.tinkoff.core.testing.mock.rules.TestingContourRule
import ru.tinkoff.core.testing.mock.server.WIRE_MOCK_PORT
import ru.tinkoff.core.testing.rule.experimental.CoreActivityScenarioRule
import screens.SampleMainScreen
import screens.SampleProductDetailScreen

const val BASE_URL = "http://localhost:$WIRE_MOCK_PORT"
const val DEFAULT_TIMEOUT = 7000L
const val ONE_MINUTE = 60_000L

abstract class BaseTest<T : AppCompatActivity>(activityClass: Class<T>) :
    BaseKaspressoTest(timeoutInMillis = DEFAULT_TIMEOUT, intervalInMillis = 500L) {

    @get:Rule
    val activityRule = CoreActivityScenarioRule(activityClass, launchActivity = true)

    @get:Rule
    val screenshotRule = ScreenshotRule(mode = ScreenshotRule.Mode.FAILURE)

    @get:Rule
    val mockRule = TestingContourRule()

    @Before
    fun before() {
        AcquiringSdk.customUrl = BASE_URL
    }
}
