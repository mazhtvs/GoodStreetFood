package utils.asserts

import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import io.github.kakaocup.kakao.common.assertions.BaseAssertions
import org.hamcrest.Matchers

fun BaseAssertions.hasTag(tag: Int) {
    view.check(
        ViewAssertions.matches(
            ViewMatchers.withTagValue(Matchers.`is`(tag))
        )
    )
}