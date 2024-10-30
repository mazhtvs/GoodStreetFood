package emailinput

import androidx.lifecycle.SavedStateHandle
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import ru.tinkoff.acquiring.sdk.redesign.common.emailinput.EmailInputViewModel
import ru.tinkoff.acquiring.sdk.redesign.common.emailinput.EmailInputViewModel.Companion.VIEW_STATE_KEY
import ru.tinkoff.acquiring.sdk.redesign.common.emailinput.models.EmailInputState
import ru.tinkoff.acquiring.sdk.redesign.common.emailinput.models.EmailValidator

@RunWith(Parameterized::class)
class EmailInputViewModelTest(
        private val inputEmail: String?,
        private val expectedValidationResult: Boolean
) {
    private val savedStateHandle = mockk<SavedStateHandle>()
    private lateinit var viewModel: EmailInputViewModel

    @Before
    fun setUp() {
        clearAllMocks()
        setUpMocks()

        viewModel = EmailInputViewModel(savedStateHandle)
    }

    private fun setUpMocks() {
        val initialEmailInputState = EmailInputState(
                email = inputEmail ?: "",
                validEmail = expectedValidationResult,
                cursorPosition = inputEmail?.length ?: 0
        )

        val stateFlow = MutableStateFlow(initialEmailInputState)

        every { savedStateHandle.get<String>(any()) } returns inputEmail
        every { savedStateHandle.getStateFlow<EmailInputState>(any(), any()) } returns stateFlow
        every { savedStateHandle[VIEW_STATE_KEY] = any<EmailInputState>() } answers {
            stateFlow.value = secondArg()
        }
        mockkObject(EmailValidator)
    }

    private fun verifySavedStateHandle() {
        verify { savedStateHandle.get<String>(any()) }
        verify { savedStateHandle.getStateFlow<EmailInputState>(any(), any()) }
    }

    @After
    fun tearDown() {
        unmockkObject(EmailValidator)
        verifySavedStateHandle()
    }

    @Test
    fun `given email arg, it should initialize view state correctly`() {
        assertEquals(inputEmail, viewModel.viewState.value.email)
        assertEquals(expectedValidationResult, viewModel.viewState.value.validEmail)
        assertEquals(inputEmail?.length ?: 0, viewModel.viewState.value.cursorPosition)
    }

    @Test
    fun `given same email, state should remain unchanged`() {
        viewModel.emailChanged(inputEmail, inputEmail?.length ?: 0)
        assertEquals(inputEmail, viewModel.viewState.value.email)
    }

    @Test
    fun `given email enabled, it should be reflected in the state`() {
        viewModel.enableEmail(true)
        assertTrue(viewModel.viewState.value.emailEnabled)
    }

    @Test
    fun `given email disabled, it should be reflected in the state`() {
        viewModel.enableEmail(false)
        assertFalse(viewModel.viewState.value.emailEnabled)
    }

    @Test
    fun `given email changed, it should update state accordingly`() {
        viewModel.emailChanged(inputEmail, inputEmail?.length ?: 0)
        assertEquals(inputEmail, viewModel.viewState.value.email)
        assertEquals(expectedValidationResult, viewModel.viewState.value.validEmail)
        assertEquals(inputEmail?.length ?: 0, viewModel.viewState.value.cursorPosition)
    }

    @Test
    fun `should request email input focus`() {
        viewModel.requestEmailInputFocus()
        assertTrue(viewModel.oneTimeEvents.value.requestEmailInputFocus?.data == true)
    }

    @Test
    fun `should clear email input focus`() {
        viewModel.clearEmailInputFocus()
        assertTrue(viewModel.oneTimeEvents.value.clearEmailInputFocus?.data == true)
    }

    companion object {
        @JvmStatic
        @Parameters(name = "{index}: emailChanged({0})={1}")
        fun data(): Collection<Array<Any?>> {
            return listOf(
                    arrayOf("test@email.com", true),
                    arrayOf("test@", false),
                    arrayOf("user@domain.com", true),
                    arrayOf("userdomain.com", false),
                    arrayOf("", false)
            )
        }
    }
}
