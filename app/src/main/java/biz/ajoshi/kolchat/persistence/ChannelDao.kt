package biz.ajoshi.kolchat.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

import biz.ajoshi.kolchat.persistence.PersistenceModels.Channel;

/**
 * Created by ajoshi on 7/8/2017.
 */
@Dao
interface ChannelDao {
    @Query("SELECT * FROM channel")
    fun getAllChannels () : List <Channel>
//    fun getChannels () : Flowable <List <Channel>> when using rxjava

    @Insert
    fun insert(channelServer: Channel)

    @Delete
    fun delete(channelServer: Channel)
}
