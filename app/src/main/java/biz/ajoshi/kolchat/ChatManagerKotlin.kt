package biz.ajoshi.kolchat

import biz.ajoshi.kolchat.model.ServerChatChannel
import biz.ajoshi.kolchat.model.ServerChatMessage
import biz.ajoshi.kolchat.model.User
import java.io.IOException
import java.util.*

/**
 * Created by ajoshi on 6/8/2017.
 */
//https://dev.to/lovis/gang-of-four-patterns-in-kotlin?utm_content=bufferdec23&utm_medium=social&utm_source=twitter.com&utm_campaign=buffer
//https://developer.android.com/topic/libraries/architecture/guide.html
// use these for ui https://material.io/components/android/catalog/

// https://github.com/stfalcon-studio/ChatKit
// https://github.com/bassaer/ChatMessageView
// https://github.com/Slyce-Inc/SlyceMessaging
class ChatManagerKotlin(val network: Network) {
    // when the last chat message was seen
    private var lastSeen: Long = 0

    // Each chat response contains this followed by the timestamp
    private val timeStampPrefix = "<!--lastseen:"
    // timestamp comes in a comment
    private val timeStampRegex = Regex("<!--lastseen:(\\d+)-->")
    // channelServer name regex. Channels might have numbers in them (talkie?)
    private val channelNameRegex = Regex("\\[(\\S+)\\]")
    private val userIdRegex = Regex("showplayer\\.php\\?who=(\\d+)['\"]")

    /**
     * Log in if necessary and fetch chat
     */
    fun start() {
        if (!network.isLoggedIn && !network.login()) {
            throw IOException("Couldn't log in for magic reason")
        }

        readChat()
    }

    /**
     * Makes a post and also retrieves any unread chat messages
     */
    fun post(message: String): List<ServerChatMessage> {
        val chatResponse = network.postChat(message)
        return parseChats(chatResponse.split("<br>"))
    }


    /**
     * Fetches unread chat messages from the server
     */
    fun readChat():List<ServerChatMessage> {
        // The responsebody we get from the server
        val chatResponse = network.readChat(lastSeen)
        // this will be the list of chats we return. We'll add chats to this list
        // break up the response by the br tag. It's what kol uses to delimit messages
        return parseChats(chatResponse.split("<br>"))
    }

    /**
     * Converts a list of raw chat messages into ServerChatMessage objects
     */
    fun parseChats(chats: List<String>): List<ServerChatMessage> {
        val returnList = mutableListOf<ServerChatMessage>()
        var channelServer: ServerChatChannel? = null
        for (chat in chats) {
            if (chat.isEmpty()) {
                // login error/general failure to get chat
                // TODO send some sort of error back up so this failure can be identified and shown to the user/some ui
                continue
            }
            if (chat.get(1) == '!') {
                // it's a comment of some sort (hopefully timestamp)
                //    if (chat.startsWith(timeStampPrefix)) {
                val lastSeenRegexMatches = timeStampRegex.find(chat)?.groups
                if (lastSeenRegexMatches != null && lastSeenRegexMatches.size > 1) {
                    val nullableTimestamp = lastSeenRegexMatches[1]?.value?.toLong()
                    if (nullableTimestamp != null) {
                        lastSeen = nullableTimestamp
                    }
                }
                //   }
                // if this was a comment, then none of the upcoming logic applies
                continue
            }

            // iterate through each chat message and create a ChatObject
            // by default the channelServer is the one we logged in to. If something went wrong, it is empty
            var channelName  = network.currentUser.mainChannel ?: ""
            val channelNameStartIndex = chat.indexOf('[')
            // if [ is too late, then it's likely not a channelServer name
            if (channelNameStartIndex < 25) {
                channelName = getStringUsingRegex(regex = channelNameRegex, sourceString = chat) ?: channelName
//                val channelNameRegexMatches = channelNameRegex.find(chat)?.groups
//                if (channelNameRegexMatches != null && channelNameRegexMatches.size > 1) {
//                    channelServer = channelNameRegexMatches[1]?.value ?: "defaultChannel"
//                }
            }
            val userId = getStringUsingRegex(regex = userIdRegex, sourceString = chat) ?: ""

            val userIdStartPosition = chat.indexOf(userId)

            //<a target=mainpane href="showplayer.php?who=2129446"><font color=blue><b>ajoshi (private):</b></font></a> <font color="blue">sd</font>


            val userIdTagEndPosition = chat.indexOf(">", userIdStartPosition) + 1
            // username is everything inside the <a> tag
            var tempUserName = chat.substring(userIdTagEndPosition, chat.indexOf("</a>", userIdTagEndPosition))

            if (tempUserName.contains("(private):")) {
                tempUserName.replace(oldValue = " (private):", newValue = "")
                // it's a pm so the channelServer name is the username
                channelServer = ServerChatChannel(name = tempUserName, id = userId, isPrivate = true)
                channelName = tempUserName
            }
            val username = tempUserName

            val isTp : Boolean = chat.indexOf(":") == -1
            // it's a special chat message like tp or snowball
            //<font color=green>
            // <a href='showplayer.php?who=2129446' target=mainpane class=nounder>
            // <font color=green>ajoshi</font></a>
            // <a href='campground.php' target=mainpane class=nounder>
            // <font color=green>has covered your Newbiesport&trade; tent with toilet paper.</font></a></font>

            val chatText : String

            if (!isTp) {
                // normal messages and PMs are after a colon
                chatText = chat.substring(chat.indexOf(":") + 1)
            } else {
                val smallerChat = chat.substring(chat.indexOf(username))
                val indexOfTpMessageBegin = smallerChat.indexOf("<font")
                chatText = username + " " +  chat.substring(indexOfTpMessageBegin)
            }

            //TODO clean up username
            val chatObject = ServerChatMessage(author = User(id = userId, name = username),
                    htmlText = chatText,
                    channelNameServer = channelServer ?: ServerChatChannel(name = channelName, id = channelName, isPrivate = false),
                    time = lastSeen)
            returnList.add(chatObject)
        }
        return returnList
    }

    private fun getStringUsingRegex(regex :Regex, sourceString: String) :String? {
        val potentialRegexMatches = regex.find(sourceString)?.groups
        if (potentialRegexMatches != null && potentialRegexMatches.size > 1) {
            return potentialRegexMatches[1]?.value
        }
        return null
    }

}
