package utils.asserts

import androidx.test.espresso.assertion.ViewAssertions
import io.github.kakaocup.kakao.common.assertions.BaseAssertions
import utils.matchers.withTitle

interface AcqTextFieldViewAssertion : BaseAssertions {

    fun hasTitle(title: String) {
        view.check(
            ViewAssertions.matches(
                withTitle(title)
            )
        )
    }
}