package biz.ajoshi.kolchat.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import biz.ajoshi.kolchat.model.ChatChannel

/**
 * Created by ajoshi on 7/8/2017.
 */
@Dao
interface ChatchannelDao {
    @Query("SELECT * FROM chatchannel")
    fun getChannels () : List <ChatChannel>
//    fun getChannels () : Flowable <List <ChatChannel>> when using rxjava

    @Insert
    fun insert(channel : ChatChannel)

    @Delete
    fun delete(channel : ChatChannel)
}