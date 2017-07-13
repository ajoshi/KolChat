package biz.ajoshi.kolchat.persistence

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by ajoshi on 7/12/17.
 */
@Entity
data class Channel(
        @PrimaryKey(autoGenerate = true)
        val id: String,
        val name: String,
        val isPrivate: Boolean,
        val lastMessage: String,
        @ColumnInfo(name = "last_message_time")
        val lastMessageTime: Long
)


@Entity
data class Message (
    @PrimaryKey(autoGenerate = true)
    val userId: String? = null,
    val userName: String? = null,
    val text: String? = null,
    val channelId: String? = null,
    val timeStamp: Long = 0
)
