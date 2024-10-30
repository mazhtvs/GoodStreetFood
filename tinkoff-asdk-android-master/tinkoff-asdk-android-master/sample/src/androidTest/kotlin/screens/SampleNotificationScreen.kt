package screens

import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import io.github.kakaocup.kakao.text.KTextView
import io.qameta.allure.kotlin.Allure
import ru.tinkoff.core.testing.kaspresso.screens.BaseScreen

class SampleNotificationScreen(testContext: TestContext<*>) : BaseScreen(testContext) {

    private val text = KTextView {withId(ru.tinkoff.acquiring.sample.R.id.tv_confirm)}

    fun checkText(text: String) {
        step("Проверить текст нотификации - '$text'") {
            text {
                isDisplayed()
                hasText(text)
            }
        }
    }

    companion object {

        const val reportName: String = "Экран с нотификацией"
        const val snapshotName: String = "SampleNotificationScreen"

        inline operator fun invoke(
            testContext: TestContext<*>,
            crossinline block: SampleNotificationScreen.() -> Unit,
        ): Unit =
            Allure.step(reportName) { SampleNotificationScreen(testContext).block() }
    }

    override val reportName: String = Companion.reportName
    override val snapshotName: String = Companion.snapshotName
}