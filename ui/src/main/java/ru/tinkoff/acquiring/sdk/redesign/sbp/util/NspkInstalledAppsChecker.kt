package ru.tinkoff.acquiring.sdk.redesign.sbp.util

import android.content.pm.PackageManager
import ru.tinkoff.acquiring.sdk.redesign.common.util.InstalledAppCheckerSdkImpl
import ru.tinkoff.acquiring.sdk.responses.NspkC2bResponse

/**
 * Created by i.golovachev
 */
fun interface NspkInstalledAppsChecker {

    fun checkInstalledApps(nspkBanks: List<NspkC2bResponse.NspkAppInfo>, deeplink: String): Map<String,String>
}

class NspkInstalledAppsCheckerImpl(val packageManager: PackageManager) : NspkInstalledAppsChecker {

    override fun checkInstalledApps(
        nspkBanks: List<NspkC2bResponse.NspkAppInfo>,
        deeplink: String
    ): Map<String, String> {
        return SbpHelper.getBankApps(resolvePackageManager(), deeplink, nspkBanks)
    }

    private fun resolvePackageManager() = (InstalledAppCheckerSdkImpl.testPackageManager ?: packageManager)
}