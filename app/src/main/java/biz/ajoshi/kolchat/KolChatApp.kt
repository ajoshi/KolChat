package biz.ajoshi.kolchat

import android.app.Application
import android.arch.persistence.room.Room
import biz.ajoshi.kolchat.persistence.KolDB
import com.facebook.drawee.backends.pipeline.Fresco

/**
 * Created by ajoshi on 7/14/17.
 */
class KolChatApp : Application() {
    companion object {
        var database: KolDB? = null
    }

    override fun onCreate() {
        super.onCreate()
        database =  Room.databaseBuilder(this, KolDB::class.java, "KolDB").build()
        Fresco.initialize(this)
    }
}
