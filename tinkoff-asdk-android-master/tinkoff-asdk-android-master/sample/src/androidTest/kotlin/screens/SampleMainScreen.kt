package screens

import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.text.KButton
import io.qameta.allure.kotlin.Allure
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.core.testing.kaspresso.screens.BaseScreen
import utils.AsdkView

class SampleMainScreen(testContext: TestContext<*>) : BaseScreen(testContext) {

    private val moreButton = KEditText { withContentDescription("Ещё") }
    private val savedCard = KView { withText("Сохраненные карты") }
    private val attachCard = KButton { withId(R.id.menu_action_attach_card) }
    private val bookDetailsButton = KButton { withIndex(0) { withId(R.id.tv_book_details) } }

    fun clickOnMoreButton() {
        step("Нажать на More button ") {
            moreButton.click()
        }
    }

    fun clickOnSavedCard() {
        step("Нажать на \"Сохраненные карты\"") {
            savedCard.click()
        }
    }

    fun clickOnAttachCard() {
        step("Нажать на \"Добавить карту\"") {
            attachCard.click()
        }
    }

    fun clickOnDetailButton() {
        step("Нажать на \"Подробнее\"") {
            bookDetailsButton.click()
        }
    }

    companion object {

        const val reportName: String = "Главный экран сэмпла"
        const val snapshotName: String = "SampleMainScreen"

        inline operator fun invoke(
            testContext: TestContext<*>,
            crossinline block: SampleMainScreen.() -> Unit,
        ): Unit =
            Allure.step(reportName) { SampleMainScreen(testContext).block() }
    }

    override val reportName: String = Companion.reportName
    override val snapshotName: String = Companion.snapshotName
}