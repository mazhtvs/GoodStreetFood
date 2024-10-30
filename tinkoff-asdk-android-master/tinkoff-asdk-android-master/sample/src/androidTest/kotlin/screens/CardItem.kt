package screens

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers.withChild
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher
import ru.tinkoff.acquiring.sample.R

class CardItem(matcher: Matcher<View>) : KRecyclerItem<CardItem>(matcher) {

    val logo = KImageView(matcher) { withId(R.id.acq_card_list_item_logo) }
    val title = KTextView(matcher) { withId(R.id.acq_card_list_item_masked_name) }
    val checkmark = KView(matcher) { withId(R.id.acq_card_list_item_choose) }
    val deleteCardButton = KButton(matcher) { withId(R.id.acq_card_list_item_delete) }
}