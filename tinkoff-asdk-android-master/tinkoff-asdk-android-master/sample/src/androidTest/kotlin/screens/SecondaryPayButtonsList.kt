package screens

import android.view.View
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher
import ru.tinkoff.acquiring.sample.R

class SecondaryPayButtonsList(matcher: Matcher<View>) : KRecyclerItem<SecondaryPayButtonsList>(matcher) {

    val paymentTypeIcon = KView(matcher) { withId(R.id.paymentTypeIcon) }
    val paymentSubtitle = KTextView(matcher) { withId(R.id.paymentSubtitle) }
    val paymentName = KTextView(matcher) { withId(R.id.paymentName) }
    val paymentTypeNavIcon = KTextView(matcher) { withId(R.id.paymentTypeNavIcon) }
}