package biz.ajoshi.kolchat.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import biz.ajoshi.kolchat.model.ChatMessage

/**
 * Created by ajoshi on 7/8/2017.
 */
@Dao
interface ChatmessageDao {
    @Query("SELECT * FROM chatmessage")
    fun getMessages () : List <ChatMessage>

    @Query("SELECT * FROM chatmessage WHERE ")
    fun getMessages (channelId : String) : List <ChatMessage>

    @Insert
    fun insert(message : ChatMessage)

    @Delete
    fun delete(message : ChatMessage)
}