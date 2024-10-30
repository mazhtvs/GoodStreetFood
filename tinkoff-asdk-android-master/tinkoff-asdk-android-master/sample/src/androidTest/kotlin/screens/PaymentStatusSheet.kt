package screens

import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.text.KTextView
import io.qameta.allure.kotlin.Allure
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.core.testing.kaspresso.screens.BaseScreen
import tests.DEFAULT_TIMEOUT
import utils.elements.KLoaderButton

class PaymentStatusSheet(testContext: TestContext<*>) : BaseScreen(testContext) {

    private val sheet = KView { withId(R.id.acq_payment_status_form) }
    private val logo = KImageView { withId(R.id.acq_payment_status_form_icon) }
    private val loader = KImageView { withId(R.id.acq_payment_status_form_progress) }
    private val title = KTextView { withId(R.id.acq_payment_status_form_title) }
    private val subtitle = KTextView { withId(R.id.acq_payment_status_form_subtitle) }
    private val mainButton = KLoaderButton { withId(R.id.acq_payment_status_form_main_button) }
    private val secondButton = KLoaderButton { withId(R.id.acq_payment_status_form_second_button) }

    /**
     * Проверки
     */
    fun logoIsVisible() {
        step("Проверить, что иконка на статусной шторке видна") {
            logo.isVisible()
        }
    }

    fun loaderIsVisible() {
        step("Проверить, что на статусной шторке отображается лоадер") {
            loader.isVisible()
        }
    }

    fun mainButtonIsVisible() {
        step("Проверить, что основная кнопка на статусной шторке видна") {
            mainButton.isVisible()
        }
    }

    fun secondaryButtonIsVisible() {
        step("Проверить, что дополнительная кнопка на статусной шторке видна") {
            secondButton.isVisible()
        }
    }

    fun mainButtonClick() {
        step("Тап по основной кнопке") {
            mainButton.click()
        }
    }

    fun checkTitleText(text: String) {
        step("Проверка заголовка статусной шторки - \"$text\"") {
            title.containsText(text)
        }
    }

    fun checkSubtitleText(text: String) {
        step("Проверка подзаголовка статусной шторки - \"$text\"") {
            subtitle.containsText(text)
        }
    }

    fun checkMainButtonText(text: String) {
        step("Проверка текста primary кнопки - \"$text\"") {
            mainButton.hasText(text)
        }
    }

    fun checkSecondaryButtonText(text: String) {
        step("Проверка текста primary кнопки - \"$text\"") {
            secondButton.hasText(text)
        }
    }

    fun checkSheetContents(
        titleText: String,
        logoIsVisible: Boolean = true,
        loaderIsVisible: Boolean = false,
        subtitleText: String? = null,
        mainButtonText: String? = null,
        secondaryButtonText: String? = null,
        timeout: Long = DEFAULT_TIMEOUT
    ) {
        step("Проверить отображение шторки об успешном платеже") {
            flakySafely(timeout, sheet) {
                if (logoIsVisible) logoIsVisible()
                if (loaderIsVisible) loaderIsVisible()
                checkTitleText(titleText)
                subtitleText?.let { checkSubtitleText(it) }
                mainButtonText?.let {
                    mainButtonIsVisible()
                    checkMainButtonText(it)
                }
                secondaryButtonText?.let {
                    secondaryButtonIsVisible()
                    checkSecondaryButtonText(it)
                }
            }

        }
    }

    fun checkIsNotDisplayed() {
        step("Проверить, что статусная шторка не отображается") {
            sheet.doesNotExist()
        }
    }

    companion object {

        const val reportName: String = "Финальная шторка платежа"
        const val snapshotName: String = "FinalScreen"

        inline operator fun invoke(
            testContext: TestContext<*>,
            crossinline block: PaymentStatusSheet.() -> Unit,
        ): Unit =
            Allure.step(reportName) { PaymentStatusSheet(testContext).block() }
    }

    override val reportName: String = PaymentStatusSheet.reportName
    override val snapshotName: String = PaymentStatusSheet.snapshotName
}