package biz.ajoshi.kolchat.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import biz.ajoshi.kolchat.persistence.PersistenceModels.Message;
/**
 * Created by ajoshi on 7/8/2017.
 */
@Dao
interface MessageDao {
    @Query("SELECT * FROM message")
    fun getMessages () : List <Message>

    @Query("SELECT * FROM message WHERE ")
    fun getMessages (channelId : String) : List <Message>

    @Insert
    fun insert(messageServer: Message)

    @Delete
    fun delete(messageServer: Message)
}
