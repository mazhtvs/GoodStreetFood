package utils.elements

import android.view.View
import androidx.test.espresso.DataInteraction
import io.github.kakaocup.kakao.common.builders.ViewBuilder
import io.github.kakaocup.kakao.common.views.KBaseView
import org.hamcrest.Matcher
import ru.tinkoff.acquiring.sdk.smartfield.AcqTextFieldView
import utils.asserts.AcqTextFieldViewAssertion

class AcqTextFieldViewElement : KBaseView<AcqTextFieldView>, AcqTextFieldViewAssertion {
    constructor(function: ViewBuilder.() -> Unit) : super(function)
    constructor(parent: Matcher<View>, function: ViewBuilder.() -> Unit) : super(parent, function)
    constructor(parent: DataInteraction, function: ViewBuilder.() -> Unit) : super(parent, function)
}
