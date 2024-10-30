package ru.tinkoff.acquiring.sdk.redesign.common.emailinput

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import ru.tinkoff.acquiring.sdk.redesign.common.emailinput.EmailInputFragment.Companion.EMAIL_ARG
import ru.tinkoff.acquiring.sdk.redesign.common.emailinput.models.EmailInputState
import ru.tinkoff.acquiring.sdk.redesign.common.emailinput.models.EmailValidator
import ru.tinkoff.acquiring.sdk.redesign.common.emailinput.models.OneTimeEventState
import ru.tinkoff.acquiring.sdk.redesign.common.emailinput.utils.SnowFlake

/**
 * Created by Michael Babayan
 */
class EmailInputViewModel(private val state: SavedStateHandle) : ViewModel() {

    val oneTimeEvents = MutableStateFlow(OneTimeEventState())

    var viewState = state.getStateFlow(
            VIEW_STATE_KEY, EmailInputState(
            email = state.get<String>(EMAIL_ARG) ?: "",
            validEmail = EmailValidator.validate(state.get<String>(EMAIL_ARG) ?: "")
    )
    )

    fun emailChanged(email: String?, cursorPos: Int) {
        val isValid = EmailValidator.validate(email)

        val updatedState = viewState.value.copy(
                validEmail = isValid,
                email = email,
                errorHighlighted = !isValid,
                cursorPosition = cursorPos
        )

        if (viewState.value.email == email) return

        state[VIEW_STATE_KEY] = updatedState
    }

    fun enableEmail(isEnabled: Boolean) = with(viewState.value) {
        state[VIEW_STATE_KEY] = viewState.value.copy(emailEnabled = isEnabled)
        if (isEnabled) {
            hideEmailInputKeyboard()
        }
    }

    fun combineEmailAndCursor(): Flow<Triple<String?, Int, Boolean>> {
        return viewState.map { state ->
            Triple(state.email, state.cursorPosition, state.errorHighlighted)
        }
    }

    fun requestEmailInputFocus() {
        oneTimeEvents.value = oneTimeEvents.value.copy(requestEmailInputFocus = SnowFlake(true))
    }

    fun clearEmailInputFocus() {
        oneTimeEvents.value = oneTimeEvents.value.copy(clearEmailInputFocus = SnowFlake(true))
    }

    fun hideEmailInputKeyboard() {
        oneTimeEvents.value = oneTimeEvents.value.copy(hideEmailInputKeyboard = SnowFlake(true))
    }

    fun checkValidation() {
        val email = viewState.value.email
        val isValid = EmailValidator.validate(email)
        val updatedState = viewState.value.copy(
            errorHighlighted = !isValid,
        )
        state[VIEW_STATE_KEY] = updatedState
    }

    companion object {
        const val VIEW_STATE_KEY = "VIEW_STATE"
    }
}
