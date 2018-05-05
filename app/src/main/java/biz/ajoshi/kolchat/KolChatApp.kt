package biz.ajoshi.kolchat

import android.app.Application
import biz.ajoshi.commonutils.Logg
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
        KolDB.createDb(this)
        Fresco.initialize(this)
    }
}
