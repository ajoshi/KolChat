package biz.ajoshi.kolchat

import android.util.Log
import biz.ajoshi.commonutils.Logg
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.services.common.Crash

/**
 * Logger that will log these to Crashlytics. Central so we can turn it off easily (GDPR?)
 */
class CrashlyticsLogger() : Logg.CustomLogger {
    override fun logException(tag: String?, throwable: Throwable?) {
        Crashlytics.log(Log.ERROR, tag, "Exception occurred")
        Crashlytics.logException(throwable)
    }

    override fun logE(tag: String?, message: String?) {
        Crashlytics.log(Log.ERROR, tag, message)
    }

    override fun logW(tag: String?, message: String?) {
        Crashlytics.log(Log.WARN, tag, message)
    }

    override fun logI(tag: String?, message: String?) {
        Crashlytics.log(Log.INFO, tag, message)
    }
}
