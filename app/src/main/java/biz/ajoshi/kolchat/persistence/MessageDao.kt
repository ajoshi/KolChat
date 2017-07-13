package biz.ajoshi.kolchat.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Flowable

/**
 * Created by ajoshi on 7/8/2017.
 */
@Dao
interface MessageDao {
    @Query("SELECT * FROM message")
    fun getMessages () : List <Message>

    @Query("SELECT * FROM message WHERE channelId = :channel_id ORDER BY timeStamp DESC")
    fun getMessagesForChannel (channel_id : String) : Flowable<List <Message>>

    @Insert
    fun insert(message: Message)

    @Delete
    fun delete(message: Message)
}
