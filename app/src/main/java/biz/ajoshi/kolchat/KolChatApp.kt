package biz.ajoshi.kolchat

import android.app.Application
import android.os.StrictMode
import android.support.v7.preference.PreferenceManager
import biz.ajoshi.commonutils.Logg
import biz.ajoshi.kolchat.chat.ChatJob
import biz.ajoshi.kolchat.persistence.KolDB
import com.crashlytics.android.Crashlytics
import com.facebook.drawee.backends.pipeline.Fresco
import io.fabric.sdk.android.Fabric

/**
 * Created by ajoshi on 7/14/17.
 */
class KolChatApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // migrations are probably dags to allow db upgrades from one version to another?
        Fabric.with(this, Crashlytics())
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)

        // set the custom logger to log to Crashlytics if the user has allowed it.
        val shouldSendLogs = preferenceManager.getBoolean(KEY_PREF_SEND_LOGS, true)
        if (shouldSendLogs) {
            Logg.setCustomLogger(CrashlyticsLogger())
        }
        KolDB.createDb(this)
        Fresco.initialize(this)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().build())
        ChatJob.addJobCreator(this)
    }
}
