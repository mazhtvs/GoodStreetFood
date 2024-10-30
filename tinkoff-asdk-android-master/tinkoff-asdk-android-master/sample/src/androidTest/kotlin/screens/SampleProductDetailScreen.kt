package screens

import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import io.github.kakaocup.kakao.text.KButton
import io.qameta.allure.kotlin.Allure
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.core.testing.kaspresso.screens.BaseScreen

class SampleProductDetailScreen(testContext: TestContext<*>) : BaseScreen(testContext) {

    private val buyByPayFormButton = KButton { withId(R.id.btn_buy_now) }
    private val tinkoffPayButton = KButton { withId(R.id.tinkoff_pay_button) }

    /**
     * Проверки
     */
    fun checkBuyByPayFormButtonIsVisible() {
        step("Проверить, что кнопка \"Купить\" отображается") {
            buyByPayFormButton.isVisible()
        }
    }

    fun clickOnBuyByPayFormButton() {
        step("Тап по кнопке \"Купить\"") {
            buyByPayFormButton.click()
        }
    }

    fun clickOnTinkoffPayButton() {
        step("Тап по кнопке \"TinkoffPay\"") {
            tinkoffPayButton.click()
        }
    }

    companion object {

        const val reportName: String = "Детали продукта"
        const val snapshotName: String = "SampleProductDetailScreen"

        inline operator fun invoke(
            testContext: TestContext<*>,
            crossinline block: SampleProductDetailScreen.() -> Unit,
        ): Unit =
            Allure.step(reportName) { SampleProductDetailScreen(testContext).block() }
    }

    override val reportName: String = Companion.reportName
    override val snapshotName: String = Companion.snapshotName
}