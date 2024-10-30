package ru.tinkoff.acquiring.sdk.utils

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import ru.tinkoff.acquiring.sdk.models.options.screen.BaseAcquiringOptions
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants
import java.io.Serializable
import kotlin.reflect.KClass

/**
 * Created by i.golovachev
 */


internal const val EXTRA_OPTIONS = "options"

fun Intent.putOptions(options: BaseAcquiringOptions) {
    putExtra(EXTRA_OPTIONS, options)
}

fun Intent.getAsError(key: String) = getSerializableExtra(key) as Throwable

fun Intent.getLongOrNull(key: String) : Long? = getLongExtra(key,-1).takeIf { it > -1 }

fun <T : BaseAcquiringOptions> Intent.getOptions(): T {
    return checkNotNull(getParcelableExtra<T>(EXTRA_OPTIONS)) {
        "extra by key $EXTRA_OPTIONS not fount"
    }
}

fun <T : BaseAcquiringOptions> SavedStateHandle.getExtra(): T {
    return checkNotNull(get<T>(EXTRA_OPTIONS)) {
        "extra by key $EXTRA_OPTIONS not fount"
    }
}

fun <T : Serializable> Intent.getExtra(name: String, clazz: KClass<T>): T? {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(name) as? T
    } else {
        getSerializableExtra(name, clazz.java)
    }
}

fun <T : Parcelable> Intent.getParcelable(name: String, clazz: KClass<T>): T? {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name) as? T
    } else {
        getParcelableExtra(name, clazz.java)
    }
}

fun Intent?.getError(): Throwable {
    return checkNotNull(this?.getExtra(LauncherConstants.EXTRA_ERROR, Throwable::class))
}
