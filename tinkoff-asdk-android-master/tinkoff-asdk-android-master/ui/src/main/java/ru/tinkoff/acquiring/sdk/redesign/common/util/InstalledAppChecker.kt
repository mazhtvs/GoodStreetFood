package ru.tinkoff.acquiring.sdk.redesign.common.util

import android.content.pm.PackageManager
import androidx.annotation.VisibleForTesting
import ru.tinkoff.acquiring.sdk.redesign.sbp.util.SbpHelper
import ru.tinkoff.acquiring.sdk.utils.isPackageInstalled

/**
 * Created by i.golovachev
 */
interface InstalledAppChecker {
    fun isInstall(packageName: String): Boolean
}


class InstalledAppCheckerSdkImpl(
    private val packageManager: PackageManager
) : InstalledAppChecker {

    override fun isInstall(packageName: String): Boolean {
        return resolvePackageManager().isPackageInstalled(packageName)
    }

    private fun resolvePackageManager() = (testPackageManager ?: packageManager)

    companion object {
        var testPackageManager: PackageManager? = null

        @VisibleForTesting
        fun replacePackageManager(packageManager: PackageManager?) {
            testPackageManager = packageManager
        }
    }
}
