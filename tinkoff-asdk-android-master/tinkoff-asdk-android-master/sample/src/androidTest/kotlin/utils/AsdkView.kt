package utils

import android.view.View
import androidx.test.espresso.DataInteraction
import io.github.kakaocup.kakao.common.builders.ViewBuilder
import io.github.kakaocup.kakao.common.views.KBaseView
import io.github.kakaocup.kakao.common.views.KView
import org.hamcrest.Matcher

class AsdkView: KBaseView<AsdkView> {

    val matcher: ViewBuilder.() -> Unit

    constructor(function: ViewBuilder.() -> Unit) : super(function) {
        matcher = function
    }
}