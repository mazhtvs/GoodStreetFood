package screens

import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import dataproviders.card.Card
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import io.qameta.allure.kotlin.Allure
import mocks.models.PayMethod
import org.junit.Assert.assertEquals
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.core.testing.kaspresso.screens.BaseScreen
import utils.asserts.hasTag
import utils.elements.AcqTextFieldViewElement
import utils.elements.KLoaderButton

class MainPayFormScreen(testContext: TestContext<*>) : BaseScreen(testContext) {

    private val mainPayForm = KView { withId(R.id.acq_main_form_content) }
    val loader = KView { withId(R.id.acq_main_form_loader) }
    val gerbIcon = KView { withId(R.id.acq_ic_main_form_gerb) }
    val toPayTitle = KView { withText("К оплате") }
    val amountText = KTextView { withId(R.id.acq_main_form_amount) }
    val descriptionText = KTextView { withId(R.id.acq_main_form_description) }
    private val primaryPayButton = KButton {
        withId(R.id.primary)
        withParent {
            withParent {
                withId(R.id.acq_main_form_primary_button)
            }
        }
    }
    private val primaryPayButtonText = KTextView { withId(R.id.acq_primary_button_text) }
    private val primaryPayButtonImage = KTextView { withId(R.id.acq_primary_button_image) }

    private val cardField = KView { withId(R.id.chosenCard) }
    private val cardFieldIcon = KView { withId(R.id.acq_card_choosen_item_logo) }
    private val cardFieldNumber = KTextView { withId(R.id.acq_card_choosen_item) }
    private val cardFieldChangeButton = KView { withId(R.id.acq_card_change) }
    private val cardFieldCvc = AcqTextFieldViewElement { withId(R.id.cvc_input) }
    private val cvcEditText = KEditText {
        withId(R.id.edit_text)
        withParent {
            withParent {
                withId(R.id.cvc_input)
            }
        }
    }
    private val loaderButton = KLoaderButton { withId(R.id.loaderButton) }
    private val emailInput = KView { withId(R.id.emailInput) }

    val secondaryPayButtons = KRecyclerView(
        builder = { withId(R.id.secondaryList) },
        itemTypeBuilder = { itemType(::SecondaryPayButtonsList) }
    )

    /**
     * Проверки
     */
    fun checkPrimaryPayButtonIsEnabled() {
        step("Проверить, что кнопка \"Оплатить\" активна") {
            primaryPayButton.isEnabled()
        }
    }

    fun checkPrimaryPayButtonIsDisabled() {
        step("Проверить, что кнопка \"Оплатить\" не активна") {
            primaryPayButton.isDisabled()
        }
    }

    fun clickPrimaryPayButton() {
        step("Тап по кнопке \"Оплатить\"") {
            primaryPayButton.isDisplayed()
            primaryPayButton.click()
        }
    }

    fun checkPrimaryButton(method: PayMethod) {
        step("Проверить, что главная кнопка оплаты - ${method.primaryText}") {
            primaryPayButton.isDisplayed()
            primaryPayButtonImage {
                method.primaryIcon?.let { hasTag(it) }
            }
            primaryPayButtonText {
                hasText(method.primaryText!!)
            }
        }
    }

    fun checkSecondaryPayButtons(vararg methods: PayMethod) {
        step("Проверить альтернативные способы оплаты в поряде - $methods") {
            secondaryPayButtons {
                assertEquals(
                    "Ожидаемое количество альтернативных способов оплаты не соответствует актуальному",
                    methods.size, this.getSize()
                )
                for (index in methods.indices) {
                    childAt<SecondaryPayButtonsList>(index) {
                        paymentName.hasText(methods[index].secondaryTitle!!)
                        methods[index].secondarySubtitle?.let { paymentSubtitle.hasText(it) }
                            ?: paymentSubtitle.hasEmptyText()
                    }
                }
            }
        }
    }

    fun checkSecondaryPayMethodsIsNotDisplayed() {
        step("Проверить, что альтернативные способы оплаты отстутствуют") {
            secondaryPayButtons.isNotDisplayed()
        }
    }

    fun clickOnSecondaryPayMethod(method: PayMethod) {
        step("Нажать на способ $method в альтернативных способах оплаты") {
            secondaryPayButtons {
                childWith<SecondaryPayButtonsList> {
                    withDescendant { withText(method.secondaryTitle!!) }
                } perform {
                    click()
                }
            }

        }
    }

    fun checkPrimaryForSavedCard(card: Card, buttonText: String) {
        checkCardField(card)
        checkEmailInputField()
        checkLoaderButton(buttonText)
    }

    fun checkCardField(card: Card) {
        step("Проверить поле с предзаполненной картой") {
            cardField.isDisplayed()
            cardFieldIcon.isDisplayed()
            cardFieldNumber {
                isDisplayed()
                hasText(card.getBankName() + " • " + card.pan)
            }
            cardFieldChangeButton.isDisplayed()
            cardFieldCvc.isDisplayed()
        }
    }

    fun clickOnChangeCardButton() {
        step("Нажать на \"Сменить карту\"") {
            cardFieldChangeButton.click()
        }
    }

    fun checkLoaderButton(text: String) {
        step("Проверить кнопку \"$text\" для оплаты сохраненной картой") {
            loaderButton.isDisplayed()
            loaderButton.hasText(text)
        }
    }

    fun clickOnLoaderButton() {
        step("Нажать кнопку \"Оплатить\" для оплаты сохраненной картой") {
            loaderButton.click()
        }
    }

    fun checkEmailInputField() {
        step("Проверить отображение поля ввода email") {
            emailInput.isDisplayed()
        }
    }

    fun typeCvc(cvc: String) {
        step("Ввести CVV - \"$cvc\"") {
            cardFieldCvc.click()
            cvcEditText.typeText(cvc)
        }
    }

    companion object {

        const val reportName: String = "Главная платежная форма"
        const val snapshotName: String = "MainPayFormScreen"

        inline operator fun invoke(
            testContext: TestContext<*>,
            crossinline block: MainPayFormScreen.() -> Unit,
        ): Unit =
            Allure.step(reportName) { MainPayFormScreen(testContext).block() }
    }

    override val reportName: String = Companion.reportName
    override val snapshotName: String = Companion.snapshotName
}