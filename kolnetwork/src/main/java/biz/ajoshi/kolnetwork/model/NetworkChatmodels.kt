package biz.ajoshi.kolnetwork.model

/**
 *  These are all representations of data as sent by the server. Some massaging is done, but the overall structure is intact
 */

/**
 * Represents a sender of chat. Its own class because channelname can be a user's name and that can
 * change at any time. Don't want two channels for "ajoshi" vs "Ajoshi" or "ajoshi-tron"
 *
 * Representation of data as sent by the server. Some massaging is done, but the overall structure is intact
 */
data class ServerChatChannel(
    val name: String,
    val id: String,
    val isPrivate: Boolean
)

/**
 * Holds basic data for a chat message
 *
 *  Representation of data as sent by the server. Some massaging is done, but the overall structure is intact
 */
data class ServerChatMessage(
    val author: User,
    val htmlText: String,
    val channelNameServer: ServerChatChannel,
    val time: Long,
    val hideAuthorName: Boolean,
    val localTime: Long
)

/**
 * The response we get from the server when we make a new post/send a chat command
 * The messages list seems to be empty each time, but I'd rather be safe
 */
data class ServerChatResponse(
    val output: String,
    val messages: List<ServerChatMessage>,
    val status: NetworkStatus
)

/**
 * A user seen in chat (or who or profile view)
 */
data class User(
    val id: String,
    val name: String
)

/**
 * Represents the currently logged in user
 */
data class LoggedInUser(
    val player: User, val pwdHash: String,
    val mainChannel: String?
)
