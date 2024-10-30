package screens

import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.kaspersky.components.kautomator.component.text.UiTextView
import com.kaspersky.components.kautomator.screen.UiScreen
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import io.qameta.allure.kotlin.Allure
import ru.tinkoff.core.testing.allure.step
import ru.tinkoff.core.testing.kaspresso.performEmu
import ru.tinkoff.core.testing.utils.TestComponentHelper

class ChromeBrowserScreen(val testContext: TestContext<*>) : UiScreen<ChromeBrowserScreen>() {

    override val packageName = "com.android.chrome"

    val title = UiTextView { withText("Стать клиентом") }

    fun checkIsOpened() {
        step("Проверить, что страница \"Стать клиентом банка\" открыта") {
            title.isDisplayed()
        }
    }

    companion object {

        const val reportName: String = "Браузер Chrome"
        const val snapshotName: String = "ChromeBrowserScreen"

        inline operator fun invoke(
            testContext: TestContext<*>,
            crossinline block: ChromeBrowserScreen.() -> Unit,
        ): Unit =
            Allure.step(reportName) { ChromeBrowserScreen(testContext).block() }
    }
}