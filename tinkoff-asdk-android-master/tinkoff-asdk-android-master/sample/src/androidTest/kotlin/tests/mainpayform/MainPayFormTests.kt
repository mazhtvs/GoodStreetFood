package tests.mainpayform

import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import dataproviders.card.Apps.*
import dataproviders.card.Apps
import io.github.kakaocup.kakao.screen.Screen
import ru.tinkoff.core.testing.mock.stubs_factory.AllureStepMock.stubFor
import mocks.GetTerminalPayMethodsMock
import mocks.models.PayMethod
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.eq
import ru.tinkoff.acquiring.sample.ui.MainActivity
import ru.tinkoff.acquiring.sdk.redesign.common.util.InstalledAppCheckerSdkImpl
import screens.SampleMainScreen
import screens.SampleProductDetailScreen
import tests.BaseTest

abstract class MainPayFormTests : BaseTest<MainActivity>(MainActivity::class.java) {

    protected fun TestContext<Unit>.openPayForm() {
        SampleMainScreen(this) {
            clickOnDetailButton()
        }
        SampleProductDetailScreen(this) {
            clickOnBuyByPayFormButton()
        }
        Screen.idle()
    }

    protected fun setGetTerminalPayMethodsMock(addScheme: Boolean, vararg methods: PayMethod) {
        stubFor(
            GetTerminalPayMethodsMock.matcher,
            willReturn = GetTerminalPayMethodsMock.withPayMethods(addScheme, *methods)
        )
    }

    protected fun setupApps(vararg apps: Apps) {
        val pm = mock(PackageManager::class.java)
        val packageInfo = PackageInfo()
        val resolveInfo = ResolveInfo()

        apps.forEach {
            `when`(pm.getPackageInfo(it.packageName, 0)).thenReturn(packageInfo)
            if (it == SBP_APP || it == TINKOFF) {
                resolveInfo.activityInfo = ActivityInfo()
                resolveInfo.activityInfo.packageName = it.packageName
                `when`(pm.queryIntentActivities(any(), eq(0))).thenReturn(listOf(resolveInfo))
            }
        }

        InstalledAppCheckerSdkImpl.replacePackageManager(pm)
    }
}