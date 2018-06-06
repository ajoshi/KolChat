package biz.ajoshi.kolchat.chat

import android.content.SharedPreferences
import android.util.Log
import biz.ajoshi.kolnetwork.model.ServerChatChannel
import biz.ajoshi.kolnetwork.model.ServerChatCommandResponse
import biz.ajoshi.kolnetwork.model.ServerChatMessage
import biz.ajoshi.kolnetwork.model.User
import org.json.JSONObject
import java.io.IOException

/**
 * Created by ajoshi on 6/8/2017.
 */
//https://dev.to/lovis/gang-of-four-patterns-in-kotlin?utm_content=bufferdec23&utm_medium=social&utm_source=twitter.com&utm_campaign=buffer
//https://developer.android.com/topic/libraries/architecture/guide.html
// use these for ui https://material.io/components/android/catalog/

// https://github.com/stfalcon-studio/ChatKit
// https://github.com/bassaer/ChatMessageView
// https://github.com/Slyce-Inc/SlyceMessaging

const val SYSTEM_USER_ID = "-1"
const val SYSTEM_USER_NAME = "system"

class ChatManager(val network: biz.ajoshi.kolnetwork.Network, internal val sharedPrefs: SharedPreferences) {
    // when the last chat message was seen
    var lastSeen: Long = sharedPrefs.getLong(SHARED_PREF_LAST_FETCH_TIME, 0)

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
    @Throws(IOException::class)
    fun start() {
        if (!network.isLoggedIn && !network.login()) {
            throw IOException("Couldn't log in for magic reason")
        }

        readChat()
    }

    /**
     * Makes a post and also retrieves any unread chat commands
     */
    @Throws(IOException::class)
    fun post(message: String): ServerChatCommandResponse {
        val chatResponse = network.postChat(message)
        return parseSentChat(chatResponse)
    }

    fun parseSentChat(response: String?): ServerChatCommandResponse {
        /*
         * This seems wrong- chat and chat commands should be separated out
         */
        if (response.isNullOrEmpty()) {
            // this is sent when we failed to make the network call (RO and maybe general connectivity issues?)
    //        network.logout()
            return ServerChatCommandResponse("", emptyList())
        }
        val json = JSONObject(response)

        /* TODO support whois
        result of a non-chat command like whois
        whois ajoshi
        {"output":"<a target=mainpane href=\"showplayer.php?who=2129446\"><b style=\"color: green;\">ajoshi (#2129446)<\/b><\/a>, <font color=green>son of the patron SAINT(!) of vagrants<\/font>","msgs":[]}

        who:
        {"output":"<br><table><tr><td class=tiny><center><b>Players in this channel:<\/b><\/center><a target=mainpane href=\"showplayer.php?who=2239681\"><font color=black>Corman<\/font><\/a> (1&nbsp;total)<\/td><\/tr><\/table>","msgs":[]}
         */


        // TODO handle failed messages (by showing error?)
        // {"output":"<font color=green><b>Unknown recipient \"fydjdjsjsjsjsjsjsjshs\".  Message (jxd) not sent.<\/b><\/font>","msgs":[]}
        val parsedChat = parseJsonChat(json)
        /*
         *  Handle chat command responses (like /who and /count)
         */
        val output = json.optString("output")
        if (!output.isNullOrEmpty()) {
            // this was a chat command
            val systemMessageUser = ServerChatChannel(name = SYSTEM_USER_NAME, id = SYSTEM_USER_ID, isPrivate = true)
            // we want this message to show up in chat, but we also don't want it to look too new.
            // Since kol time != real time, we use the timestamp of the last received message as this one's timestamp.
            // Ensures that the next message received will always be seen as newer (wouldn't happen if we used local time)
            val commandResponseMessage = ServerChatMessage(author = User(id = systemMessageUser.id, name = systemMessageUser.name),
                    htmlText = output, channelNameServer = systemMessageUser, localTime = System.currentTimeMillis(), hideAuthorName = false, time = lastSeen)
            parsedChat.add(commandResponseMessage)
        }

        val chatCommandResponse = json.optString("output")
        return ServerChatCommandResponse(chatCommandResponse, parsedChat)
    }

    /**
     * Fetches unread chat commands from the server
     */
    @Throws(IOException::class)
    fun readChat(): List<ServerChatMessage> {
        return readChat(lastSeen)
    }

    /**
     * Fetches unread chat commands from the server since the given timestamp
     */
    @Throws(IOException::class)
    fun readChat(lastSeenTime: Long): List<ServerChatMessage> {
        // The responsebody we get from the server
        val chatResponse = network.readChat(lastSeenTime)
        // this will be the list of chats we return. We'll add chats to this list
        // break up the response by the br tag. It's what kol uses to delimit commands
        if (chatResponse.isNullOrEmpty()) {
        //    network.logout()
            return emptyList()//
        }
        val response = JSONObject(chatResponse)
        val timeStampString = response.getLong("last")
        setLastSeenTime(timeStampString)
        return parseJsonChat(response)
    }

    private fun setLastSeenTime(newTime: Long) {
        lastSeen = newTime
        sharedPrefs.edit().putLong(SHARED_PREF_LAST_FETCH_TIME, lastSeen).apply()

    }

    fun parseJsonChat(response: JSONObject): MutableList<ServerChatMessage> {
        val currentTime = System.currentTimeMillis()

        //       does this mean logout?
        /*
        {"msgs":[],"last":"1450743652","delay":3000,"out":1}
         */
/*
{
   "msgs":[
      {
         "msg":"<!--viva-->Gross quintinz",
         "type":"public",
         "mid":"1448600119",
         "who":{
            "name":"PIGRAt",
            "id":"2363017",
            "color":"black"
         },
         "format":"0",
         "channel":"newbie",
         "channelcolor":"green",
         "time":"1505676231"
      },
      {
         "msg":"yay!",
         "type":"public",
         "mid":"1448600120",
         "who":{
            "name":"RikAstley",
            "id":"1933641",
            "color":"black"
         },
         "format":"0",
         "channel":"games",
         "channelcolor":"green",
         "time":"1505676232"
      }
   ],
   "last":"1448600120",
   "delay":3000
}
 */
        //TODO this is where I can use retrofit or jackson to just give me pojos instead
        val msgs = response.getJSONArray("msgs")
        val msgCount = msgs.length()
        val list = mutableListOf<ServerChatMessage>()
        for (i in 0 until msgCount) {
            val msg = msgs.get(i) as JSONObject
            list.add(parseChatMessageJsonObject(chatMessageJson = msg, currentTime = currentTime))
        }
        // 1527977527
        // 1527977486167
        // * 1000
//ServerChatMessage(author=User(id=-1, name=system), htmlText=New message received from <a target=mainpane href='showplayer.php?who=2239681'><font color=green>Corman</font></a>., channelNameServer=ServerChatChannel(name=system, id=-1, isPrivate=true), time=1527977527, hideAuthorName=false, localTime=1527977486167)
        return list
    }

    /**
     * Handles chat response for one message in json format. Message can be PMs from/to me and public chats
     */
    fun parseChatMessageJsonObject(chatMessageJson: JSONObject, currentTime: Long): ServerChatMessage {
        val currentUser = network.currentUser.player
//{"msgs":[{"type":"private","who":{"id":"2239681","name":"Corman","color":"black"},"for":{"id":"2129446","name":"ajoshi","color":"black"},"msg":"Butts","time":1505616130,"format":0}]}
        //{"output":"<br><table><tr><td class=tiny><center><b>Players in this channel:<\/b><\/center><a target=mainpane href=\"showplayer.php?who=2129446\"><font color=blue>ajoshi<\/font><\/a>, <a target=mainpane href=\"showplayer.php?who=2239681\"><font color=black>Corman<\/font><\/a> (2&nbsp;total)<\/td><\/tr><\/table>","msgs":[]}
        /*
        Special chat effects (as comments)
        <!--viva--> = vivala
         */
        /*
         formats:
         format = 0   is normal chat
         format = 1   is /me

         maybe
         3 is mod warning (red)
         4 is mod announcement (green)?
         */
        val format = chatMessageJson.optInt("format")
        // if format is 1, then this is an emphasis/me post
        val isEmPost = format == 1
        // private, public, event
        val type = chatMessageJson.getString("type")
        // act like events are private commands
        val channelIsPrivate = "public" != type// == means structural equality, === means old ==
        val who = chatMessageJson.optJSONObject("who")
        // player 420 has been deleted. use as placeholder for public events? currently using -1
        var id = SYSTEM_USER_ID
        var name = SYSTEM_USER_NAME
        who?.let {
            id = who.getString("id")
            name = who.getString("name")
            /*
             * MORE SPECIAL CASING!
             * It turns out that when TPTB make a system message, they use their userids, but their name is shown as System Message.
             * But because REASONS, the name is actually "<font color=>System Message</font>". Empty color value? Why not.
             */
            if (name == "<font color=>System Message</font>") {
                name = SYSTEM_USER_NAME
                id = SYSTEM_USER_ID
            }
            // PMs seem to come under the fallback category
            val color = who.optString("color", "black")
            // can format string be a constant? It seems not?
            name = "<font color=$color>$name</font>"
        }

        val channel: ServerChatChannel
        channel = if (!channelIsPrivate) {
            // we're in a chat room
            val channelName = chatMessageJson.getString("channel")
            ServerChatChannel(name = channelName, id = channelName, isPrivate = channelIsPrivate)
        } else {
            if (id == currentUser.id) {
                // this was a pm from me to someone else so ensure the channel name is right
                val pmReceiver = chatMessageJson.getJSONObject("for")

                ServerChatChannel(name = pmReceiver.getString("name"), id = pmReceiver.getString("id"), isPrivate = channelIsPrivate)
            } else {
                ServerChatChannel(name = name, id = id, isPrivate = channelIsPrivate)
            }
        }
        /*
   "msg":"<a href='showplayer.php?who=2129446' target=mainpane class=nounder>
   <font color=green>ajoshi<\/font><\/a>
   <a href='campground.php' target=mainpane class=nounder><font color=green>has covered your Newbiesport&trade; tent with toilet paper.<\/font><\/a>",

   need html parsing to extract username and message. Probably similar for /me
 */

        val temptext = chatMessageJson.getString("msg")
        val text: String
        text = when (format) {
        // Mod warnings need to be colored correctly. 3 is a red warning, 4 is a green announcement
            3 -> "<font color=\"red\">" + replaceChatEffectImagesWithEmojis(temptext) + "</font>"
            4 -> "<font color=\"#117A65\">" + replaceChatEffectImagesWithEmojis(temptext) + "</font>"
            else -> {
                replaceChatEffectImagesWithEmojis(temptext)
            }
        }
        val time = chatMessageJson.getLong("time")

        val message = ServerChatMessage(author = User(id = id, name = name), htmlText = text, channelNameServer = channel, localTime = currentTime, hideAuthorName = isEmPost, time = time)
        return message
    }

    /**
     * Converts a list of raw chat commands into ServerChatMessage objects
     */
    fun parseChats(chatString: String): List<ServerChatMessage> {
        val currentTime = System.currentTimeMillis()
        val chats = chatString.split("<br>")

        /*
        {"msgs":[{"type":"private","who":{"id":"2239681","name":"Corman","color":"black"},"for":{"id":"2129446","name":"ajoshi","color":"black"},"msg":"Gfff","time":1504512754,"format":0}]}
         */
        /*
         <font color=green>[newbie]</font> <b><a target=mainpane href="showplayer.php?who=2744698"><font color=black>kirahvikoira</font></b></a>: either one works for the outfit <i>Multi Czar sucks!</i><br>
         <font color=green>[newbie]</font> <b><a target=mainpane href="showplayer.php?who=498028"><font color=black>Criswell</font></b></a>: ok offhands then<br>
         <a target=mainpane href="showplayer.php?who=2190946"><font color=blue><b>UncleHoboCrimbo (private):</b></font></a> <font color="blue">test</font><br>
         <!--lastseen:1447725918-->
         */
        val returnList = mutableListOf<ServerChatMessage>()
        var channelServer: ServerChatChannel? = null
        for (chat in chats) {
            if (chat.isEmpty()) {
                // login error/general failure to get chat
                // TODO send some sort of error back up so this failure can be identified and shown to the user/some ui
                continue
            }
            if (chat[1] == '!') {
                // it's a comment of some sort (hopefully timestamp)
                //    if (chat.startsWith(timeStampPrefix)) {
                val lastSeenRegexMatches = timeStampRegex.find(chat)?.groups
                if (lastSeenRegexMatches != null && lastSeenRegexMatches.size > 1) {
                    val nullableTimestamp = lastSeenRegexMatches[1]?.value?.toLong()
                    if (nullableTimestamp != null) {
                        setLastSeenTime(nullableTimestamp)
                    }
                }
                //   }
                // if this was a comment, then none of the upcoming logic applies
                continue
            }

            // iterate through each chat message and create a ChatObject
            // by default the channelServer is the one we logged in to. If something went wrong, it is empty
            var channelName = network.currentUser.mainChannel ?: ""
            val channelNameStartIndex = chat.indexOf('[')
            // if [ is too late, then it's likely not a channelServer name
            if (channelNameStartIndex < 25) {
                channelName = getStringUsingRegex(regex = channelNameRegex, sourceString = chat) ?: channelName
//                val channelNameRegexMatches = channelNameRegex.find(chat)?.groups
//                if (channelNameRegexMatches != null && channelNameRegexMatches.size > 1) {
//                    channelServer = channelNameRegexMatches[1]?.value ?: "defaultChannel"
//                }
            }
            var userId = getStringUsingRegex(regex = userIdRegex, sourceString = chat) ?: ""

            val userIdStartPosition = chat.indexOf(userId)

            //<a target=mainpane href="showplayer.php?who=2129446"><font color=blue><b>ajoshi (private):</b></font></a> <font color="blue">sd</font>


            val userIdTagEndPosition = chat.indexOf(">", userIdStartPosition) + 1
            // username is everything inside the <a> tag

            //                          java.lang.StringIndexOutOfBoundsException: length=33; regionStart=7; regionLength=-8
            // regionLength = endindex - beginindex
            val indexOfATag = chat.indexOf("</a>", userIdTagEndPosition)
            var tempUserName: String? = null
            if (indexOfATag > -1) {
                tempUserName = chat.substring(userIdTagEndPosition, indexOfATag)
            } else {
                Log.e("ajoshi", "error parsing $chat")
                Log.e("ajoshi", "error parsing $chatString")
            }

            if (tempUserName != null && tempUserName.contains("(private):")) {
                tempUserName = tempUserName.replace(oldValue = " (private):", newValue = "")
                // it's a pm so the channelServer name is the username
                channelServer = ServerChatChannel(name = tempUserName, id = userId, isPrivate = true)
                channelName = tempUserName
            }

            val isTp: Boolean = chat.indexOf(":") == -1
            // it's a special chat message like tp or snowball
            //<font color=green>
            // <a href='showplayer.php?who=2129446' target=mainpane class=nounder>
            // <font color=green>ajoshi</font></a>
            // <a href='campground.php' target=mainpane class=nounder>
            // <font color=green>has covered your Newbiesport&trade; tent with toilet paper.</font></a></font>

            val chatText: String

            if (!isTp) {
                // normal commands and PMs are after a colon
                chatText = chat.substring(chat.indexOf(":") + 1)
            } else if (tempUserName != null) {
                /*
                 * miasma causes index out of bounds here:
<font color=green>[newbie]</font> <b><a target=mainpane href="showplayer.php?who=1040326"><font color=black>CaptainUrsus</font></b></a>:
<br>Mimm mrmnks. <i>loneliness</i>
<br><font color=green>[newbie]</font> <b><a target=mainpane href="showplayer.php?who=1969411"><font color=black>Andalusia</font></b></a>: Awww Cap'n.

                 the effect adds additional <brs> which are viewed as new chats

                 A solution might be to append to previous chat but that seems like a bad idea
                 (when do we know to stop? how much state do we carry over from one iteration to the other?)

                 Safer solution seems to be to simply set the userid and username as "" or invalid so it just gets clumped together

                 */
                val smallerChat = chat.substring(chat.indexOf(tempUserName))
                val indexOfTpMessageBegin = smallerChat.indexOf("<font")
                chatText = tempUserName + " " + chat.substring(indexOfTpMessageBegin)
            } else {
                tempUserName = ""
                userId = "clumpWithLast"
                chatText = chat
            }

            val username = tempUserName ?: ""
            //TODO clean up username
            val chatObject = ServerChatMessage(author = User(id = userId, name = username),
                    htmlText = chatText,
                    channelNameServer = channelServer
                            ?: ServerChatChannel(name = channelName, id = channelName, isPrivate = false),
                    time = lastSeen, hideAuthorName = false, localTime = currentTime)
            returnList.add(chatObject)
        }
        return returnList
    }

    private fun getStringUsingRegex(regex: Regex, sourceString: String): String? {
        val potentialRegexMatches = regex.find(sourceString)?.groups
        if (potentialRegexMatches != null && potentialRegexMatches.size > 1) {
            return potentialRegexMatches[1]?.value
        }
        return null
    }

    companion object {
        // TODO can we do this without a companion object? Is it too much of a hassle to send in these 3 vars all the
        // way up to the post call in ChatMgr?
        /**
         * Business logic about how to construct a chat command from individual components
         * @param post Text to be posted. Can be a chat command or chat text
         * @param id id of the channel or user this is being sent to (or the username)
         * @param isPrivate true if this is a private chat, false if this is a channel
         */
        fun getChatString(post: CharSequence?, id: String, isPrivate: Boolean): String {
            post?.takeIf { post.isNotBlank() }?.let {
                // the user could have sent in a username instead so replace space with _
                val safeId = id.replace(' ', '_')
                if (post.isNotBlank() && post[0] == '/') {
                    // this is a command, but /me go in regular chat and should be scoped to channel
                    if (!post.startsWith("/me") && !post.startsWith("/em")) {
                        if (post.toString() == "/who") {
                            // /who commands are scoped to channel, but as "who <channel>" instead of "<channel> who"
                            return "$post $safeId"
                        }
                        return post.toString()
                    }
                }
                return when (isPrivate) {
                    true -> "/w $safeId $post"
                    false -> "/$safeId $post"
                }
            }
            // null chat string, we can't do anything anyway
            return ""
        }


        val imageBasePath = "<img src=\"https:\\/\\/s3\\.amazonaws\\.com\\/images\\.kingdomofloathing\\.com\\/otherimages\\/12x12%s\\.gif\" height=\"12\" width=\"12\" \\/>"
        val skullRegex = Regex(imageBasePath.format("skull"))
        val heartRegex = Regex(imageBasePath.format("heart"))
        /**
         * Some chat effects have tiny images in them. Instead of converting these images to ImageSpans and then trying
         * to load the image, we just replace the img tags with emojis
         */
        fun replaceChatEffectImagesWithEmojis(input: String): String {
            // replace all <img src="https://s3.amazonaws.com/images.kingdomofloathing.com/otherimages/<imagename>" height="12" width="12" />
            // 12x12skull ☠☠️
            // 12x12heart ❤️ 💓 💕 💖 💗 💙 💚 💛
            // not doing yet 12x12snowman ☃️  ⛄  ❄️

            // I would have thought that using stringbuilders would be better here, but strings are much faster
            return input
                    .replace(skullRegex, "☠")
                    .replace(heartRegex, "❤")
        }
    }
}
