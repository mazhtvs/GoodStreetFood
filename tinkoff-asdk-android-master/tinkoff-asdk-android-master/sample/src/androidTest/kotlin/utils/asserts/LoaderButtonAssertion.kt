package utils.asserts

import androidx.test.espresso.assertion.ViewAssertions
import io.github.kakaocup.kakao.common.assertions.BaseAssertions
import utils.matchers.withText
import utils.matchers.withTitle

interface LoaderButtonAssertion : BaseAssertions {

    fun hasText(text: String) {
        view.check(
            ViewAssertions.matches(
                withText(text)
            )
        )
    }
}