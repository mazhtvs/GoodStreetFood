package screens

import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.toolbar.KToolbar
import io.qameta.allure.kotlin.Allure
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.core.testing.kaspresso.screens.BaseScreen

class PayNewCardScreen(testContext: TestContext<*>) : BaseScreen(testContext) {

    val cardInputFields = CardDataElements()
    private val payByNewCardButton = KButton { withId(R.id.acq_pay_btn) }
    private val toolbar = KToolbar { withId(R.id.acq_toolbar) }

    /**
     * Проверки
     */
    fun checkScreenTitle() {
        step("Проверить заголовок экрана оплаты новой картой") {
            toolbar.hasTitle("Оплата картой")
        }
    }

    fun checkPayByNewCardButtonIsEnabled() {
        step("Проверить, что кнопка \"Оплатить\" активна") {
            payByNewCardButton.isEnabled()
        }
    }

    fun checkPayByNewCardButtonIsDisable() {
        step("Проверить, что кнопка \"Оплатить\" НЕ активна") {
            payByNewCardButton.isDisabled()
        }
    }

    fun clickPayByNewCardButton() {
        step("Тап по кнопке \"Оплатить\" ") {
            payByNewCardButton.click()
        }
    }

    companion object {

        const val reportName: String = "Форма оплаты новой картой"
        const val snapshotName: String = "FinalScreen"

        inline operator fun invoke(
                testContext: TestContext<*>,
                crossinline block: PayNewCardScreen.() -> Unit,
        ): Unit =
                Allure.step(reportName) { PayNewCardScreen(testContext).block() }
    }

    override val reportName: String = PayNewCardScreen.reportName
    override val snapshotName: String = PayNewCardScreen.snapshotName
}