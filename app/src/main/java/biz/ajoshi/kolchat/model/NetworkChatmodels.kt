package biz.ajoshi.kolchat.model

import java.util.*

/**
 *  These are all representations of data as sent by the server. Some massaging is done, but the overall structure is intact
 */

/**
 * Represents a sender of chat. Its own class because channelname can be a user's name and that can
 * change at any time. Don't want two channels for "ajoshi" vs "Ajoshi" or "ajoshi-tron"
 *
 * Representation of data as sent by the server. Some massaging is done, but the overall structure is intact
 */
data class ServerChatChannel(val name: String,
                             val id: String,
                             val isPrivate: Boolean)

/**
 * Holds basic data for a chat message
 *
 *  Representation of data as sent by the server. Some massaging is done, but the overall structure is intact
 */
data class ServerChatMessage(val author: User,
                             val htmlText: String,
                             val channelNameServer: ServerChatChannel,
                             val time: Long)

/**
 * A user seen in chat (or who or profile view)
 */
data class User(val id: String,
                val name: String)

/**
 * Represents the currently logged in user
 */
data class LoggedInUser(val player: User, val pwdHash: String,
                        val mainChannel: String?)
