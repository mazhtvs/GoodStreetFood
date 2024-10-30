package utils.matchers

import android.view.View
import androidx.test.espresso.matcher.BoundedDiagnosingMatcher
import org.hamcrest.Description
import ru.tinkoff.acquiring.sdk.ui.customview.LoaderButton

fun withText(text: String): BoundedDiagnosingMatcher<View?, LoaderButton> {
    return object : BoundedDiagnosingMatcher<View?, LoaderButton>(LoaderButton::class.java) {
        override fun describeMoreTo(description: Description) {
            description.appendText("LoaderButton expected text - $text ")
        }

        override fun matchesSafely(item: LoaderButton, mismatchDescription: Description): Boolean {
            return text == item.text
        }
    }
}