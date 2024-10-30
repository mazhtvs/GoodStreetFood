package utils.matchers

import android.view.View
import androidx.test.espresso.matcher.BoundedDiagnosingMatcher
import org.hamcrest.Description
import ru.tinkoff.acquiring.sdk.smartfield.AcqTextFieldView

fun withTitle(title: String): BoundedDiagnosingMatcher<View?, AcqTextFieldView> {
    return object : BoundedDiagnosingMatcher<View?, AcqTextFieldView>(AcqTextFieldView::class.java) {
        override fun describeMoreTo(description: Description) {
            description.appendText("AcqTextFieldView expected title - $title ")
        }

        override fun matchesSafely(item: AcqTextFieldView, mismatchDescription: Description): Boolean {
            return title == item.title
        }
    }
}