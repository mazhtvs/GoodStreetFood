package screens

import androidx.appcompat.R
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import io.github.kakaocup.kakao.text.KTextView
import io.qameta.allure.kotlin.Allure
import ru.tinkoff.core.testing.kaspresso.screens.BaseScreen

class AlertElement(testContext: TestContext<*>) : BaseScreen(testContext) {

    private val alertTitle = KTextView {withId(R.id.alertTitle)}
    private val alertMessage = KTextView {withId(android.R.id.message)}
    private val alertButton = KTextView {withId(android.R.id.button1)}

    fun checkAlert(titleText: String, messageText: String, buttonText: String) {
        step("Проверить отображение алерта: title - '$titleText', message - '$messageText'," +
                "button - '$buttonText'") {
            alertTitle {
                isDisplayed()
                hasText(titleText)
            }
            alertMessage.hasText(messageText)
            alertButton.hasText(buttonText)
        }
    }

    companion object {

        const val reportName: String = "Alert"
        const val snapshotName: String = "Alert"

        inline operator fun invoke(
            testContext: TestContext<*>,
            crossinline block: AlertElement.() -> Unit,
        ): Unit =
            Allure.step(reportName) { AlertElement(testContext).block() }
    }

    override val reportName: String = AlertElement.reportName
    override val snapshotName: String = AlertElement.snapshotName
}