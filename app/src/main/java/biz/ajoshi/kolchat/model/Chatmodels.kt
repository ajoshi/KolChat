package biz.ajoshi.kolchat.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

/**
 * Represents a sender of chat. Its own class because channelname can be a user's name and that can
 * change at any time. Don't want two channels for "ajoshi" vs "Ajoshi" or "ajoshi-tron"
 */
@Entity
data class ChatChannel (@ColumnInfo(name="name") val name: String,
                        @PrimaryKey @ColumnInfo(name="id") val id: String,
                        @ColumnInfo(name="is_private") val isPrivate: Boolean)

/**
 * A user seen in chat (or who or profile view)
 */
@Entity
data class User(@PrimaryKey @ColumnInfo(name="id") val id: String,
                @ColumnInfo(name="name") val name: String)

/**
 * Holds basic data for a chat message
 */
@Entity
data class ChatMessage(@ColumnInfo(name="author") val author: User,
                       @ColumnInfo(name="htmltext") val htmlText: String,
                       @ColumnInfo(name="channel_name") val channelName: ChatChannel,
                       @ColumnInfo(name="time") val time: Date)

/**
 * Represents the currently logged in user
 */
data class LoggedInUser(val player: User, val pwdHash: String,
                        val mainChannel: String?)