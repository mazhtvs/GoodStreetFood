package utils

import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.acquiring.sdk.models.enums.CheckType

object SdkSettings {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun setCheckType(checkType: CheckType) {
        sharedPreferences.edit().apply {
            putString(
                context.getString(R.string.acq_sp_check_type_id),
                checkType.toString()
            )
        }.commit()
    }
}