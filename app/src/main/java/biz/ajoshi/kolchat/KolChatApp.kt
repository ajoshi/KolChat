package biz.ajoshi.kolchat

import android.app.Application
import android.os.StrictMode
import androidx.preference.PreferenceManager
import biz.ajoshi.kolchat.chat.ChatJob
import biz.ajoshi.kolchat.persistence.KolDB
import com.facebook.drawee.backends.pipeline.Fresco

/**
 * Created by ajoshi on 7/14/17.
 */
class KolChatApp : Application() {

    override fun onCreate() {
        super.onCreate()
//        Fabric.with(this, Crashlytics())
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)

        // set the custom logger to log to Crashlytics if the user has allowed it.
        val shouldSendLogs = preferenceManager.getBoolean(KEY_PREF_SEND_LOGS, true)
        if (shouldSendLogs) {
//            Logg.setCustomLogger(CrashlyticsLogger())
        }
        Analytics.shouldTrackEvents = preferenceManager.getBoolean(KEY_PREF_TRACK_EVENTs, true)
        KolDB.createDb(this)
        Fresco.initialize(this)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().build())
        ChatJob.addJobCreator(this)
    }
}
