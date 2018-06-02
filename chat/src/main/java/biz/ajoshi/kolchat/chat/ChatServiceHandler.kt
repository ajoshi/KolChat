package biz.ajoshi.kolchat.chat

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.LocalBroadcastManager
import biz.ajoshi.commonutils.Logg
import biz.ajoshi.commonutils.StringUtilities
import biz.ajoshi.kolchat.chat.persistence.RoomInserter
import biz.ajoshi.kolnetwork.model.ServerChatMessage
import java.io.IOException

// normally we'll poll every 3 seconds
const val DEFAULT_POLL_INTERVAL = 3000
const val FOREGROUND_NOTIFICATION_ID = 666
const val PM_NOTIFICATION_ID = 667
const val ERROR_STRING = "error"

// if this id is sent in, launch this chat as soon as possible- the user tapped on a notification for this chat
const val EXTRA_LAUNCH_TO_CHAT_ID = "biz.ajoshi.kolchat.chat.ChatServiceHandler.EXTRA_LAUNCH_TO_CHAT_ID"

const val ACTION_CHAT_COMMAND_FAILED = "biz.ajoshi.kolchat.chat.ChatServiceHandler.ACTION_CHAT_COMMAND_FAILED"
const val EXTRA_FAILED_CHAT_MESSAGE = "biz.ajoshi.kolchat.chat.ChatServiceHandler.EXTRA_FAILED_CHAT_MESSAGE"

/**
 * A Handler that lets us read chat and insert to DB
 */
class ChatServiceHandler(looper: Looper, val service: ChatService) : Handler(looper) {

    interface ChatService {
        fun getCurrentUsername(): String
        fun getCurrentUserPassword(): String
        fun stopChatService(id: Int)
        fun getContext(): Context
        /**
         * Returns an Intent that will launch 1 'main' activity. If any decision making needs to be done to
         * correctly direct the user to another activity, the 'main' activity must do this.
         */
        fun getMainActivityIntent(): Intent
    }

    val roomInserter = RoomInserter()
    var pollInterval: Long = DEFAULT_POLL_INTERVAL.toLong()
    var lastFetchedTime: Long = 0

    var inverseAgeOfMessage = 0

    override fun handleMessage(msg: Message?) {
        try {
            Logg.i("ChatServiceHandler", "Handler received a message")
            if (ChatSingleton.chatManager == null || !ChatSingleton.isLoggedIn()) {
                ChatSingleton.login(service.getCurrentUsername(), service.getCurrentUserPassword(), true, service.getContext())
            }
            if (ChatSingleton.chatManager == null ||  // chatmgr is null so we have no userinfo to use for login
                    (!ChatSingleton.chatManager!!.network.isLoggedIn  // we're not logged in (but have the ability)
                            && !ChatSingleton.chatManager!!.network.login())) { // tried to login, but couldnt
                // not logged in so exit service. may be premature and a bad idea
                // we couldn't log in so... stopChatService the service?
                // TODO maybe notify user that error occurred here?
                Logg.i("ChatServiceHandler", "Not logged in. Handler exiting")
                service.stopChatService(msg?.arg1 ?: -1)
                return
            }
            if (msg?.obj != null) {
                // if we had a message to send then send it
                // periodic chat read message has no object
                val serviceMessage = (msg.obj as ChatServiceMessage)
                when (serviceMessage.type) {

                    MessageType.START -> {
                        // we got told to start. read chat asap and also send a delayed read request
                        Logg.i("ChatServiceHandler", "Starting periodic chat read")
                        readChat()
                        sendMessageDelayed(obtainLoopMessage(msg.arg1), pollInterval)
                    }
                    MessageType.STOP -> {
                        // we got told to quit
                        Logg.i("ChatServiceHandler", "Shutting down chat reading")
                        service.stopChatService(msg.arg1)
                        return
                    }
                    MessageType.SEND_CHAT_MESSAGE -> {
                        Logg.i("ChatServiceHandler", "Sending message")
                        if (serviceMessage.textmessage != null) {
                            val response = ChatSingleton.postChat(serviceMessage.textmessage)
                            response?.let {
                                insertChatsIntoDb((response.messages), ChatSingleton.network?.currentUser?.player?.name
                                        ?: ERROR_STRING)
                                /*
                                  Chat commands (and chat message sends) don't actually end up returning messages- they
                                  only return an output (if anything)
                                  There is no contract that specifies this though, and assuming this might cause dropped messages.

                                  So right now just stick output in System chat and ALSO broadcast it out
                                 */
                                if (response.output.isNotEmpty()) {
                                    val broadcastIntent = Intent(ACTION_CHAT_COMMAND_FAILED)
                                    broadcastIntent.putExtra(EXTRA_FAILED_CHAT_MESSAGE, response.output)
                                    LocalBroadcastManager.getInstance(service.getContext()).sendBroadcast(broadcastIntent)
                                }
                            }
                        }
                    }
                    MessageType.READ_ONCE -> {
                        Logg.i("ChatServiceHandler", "Reading chat once")
                        // read once and do not send message to read again
                        readChat()
                    }
                    MessageType.READ_UNTIL_THRESHOLD -> {
                        val threshold = 10
                        Logg.i("ChatServiceHandler", "Reading chat until threshold $threshold")
                        // read  until fewer than 10 messages come from the server
                        readChat(threshold)
                    }
                }

            } else {
                Logg.i("ChatServiceHandler", "No message, reading chat and scheduling future read")
                // else check for new commands and reschedule to check in a bit
                if (msg != null && msg.arg2 == inverseAgeOfMessage) {
                    sendMessageDelayed(obtainLoopMessage(msg.arg1), pollInterval)
                    readChat()
                }
            }
        } catch (exception: IOException) {
            Logg.logThrowable(exception)
            // an ioexception occured. This means we're in a bad network location. try again later
            val newMessage = cloneMessage(msg)
            newMessage ?: sendMessageDelayed(newMessage, pollInterval)

        }
    }

    /**
     * Reads queued chat messages and inserts them into DB. If user has any direct messages, a notification is also created
     */
    private fun readChat() {
        // TODO add instrumentation here so we can measure fetch and db insertion times
        val messages = ChatSingleton.readChat(lastFetchedTime)
        // if we can, read the chat and stick in db
        insertChatsIntoDb(messages, ChatSingleton.network?.currentUser?.player?.name ?: ERROR_STRING)
        notifyUserOfPm(messages)
        lastFetchedTime = ChatSingleton.chatManager!!.lastSeen
    }

    /**
     * Reads queued chat messages and inserts them into DB. If there are more than n messages, read again.
     * The idea is that the server isn't giving us all the messages in one go and so multiple requests are needed.
     * If user has any direct messages, a notification is also created.
     * @param threshold min number of messages that should be returned for us to poll the server again
     */
    private fun readChat(threshold: Int) {
        // TODO add instrumentation here so we can measure fetch and db insertion times
        Logg.i("ChatServiceHandler", "Fetching chat data")
        val messages = ChatSingleton.readChat(lastFetchedTime)
        Logg.i("ChatServiceHandler", "chat data fetched " + messages?.size + " messages read")
        // if we can, read the chat and stick in db
        insertChatsIntoDb(messages, ChatSingleton.network?.currentUser?.player?.name ?: ERROR_STRING)
        Logg.i("ChatServiceHandler", "db insertion complete")
        notifyUserOfPm(messages)
        lastFetchedTime = ChatSingleton.chatManager!!.lastSeen
        messages?.let {
            if (it.size > threshold) {
                // if we got more than n messages in the last read, there might still be a bunch remaining. Poll until
                // there aren't any left (or we get fewer than n messages)
                readChat(threshold)
            }
        }
    }

    fun insertChatsIntoDb(messages: List<ServerChatMessage>?, currentUserName: String) {
        if (messages != null) {
            roomInserter.insertAllMessages(messages, currentUserName)
        }
    }

    /**
     * gets the default message for our looper. Takes in the startid of the service
     */
    fun obtainLoopMessage(id: Int): Message? {
        // arg1 holds the service startid
        // arg2 holds the poll interval we want the polling to have so it can be changed at will
        // obj is used to hold an integer that tells us how old this message really is. Big numbers are newer
        val msg = obtainMessage()
        msg?.arg1 = id
        msg?.arg2 = inverseAgeOfMessage
        return msg
    }

    private fun cloneMessage(oldMessage: Message?): Message? {
        val msg = obtainMessage()
        msg?.arg1 = oldMessage?.arg1
        msg?.arg2 = oldMessage?.arg2
        msg?.obj = oldMessage?.obj
        return msg
    }

    /**
     * Returns an int that lets us know which message is newest. This ensures that delayed messages that have been
     * waiting for a while do not suddenly start running and queueing child jobs when their time comes in.
     *
     * Eg: poll interval is at 1 minute when app is in bg, but set to 3 sec when app is launched. If we don't check age,
     * delayed messages will pile up with 3 second intervals
     */
    fun getAgeIntForNewMessage(): Int {
        return ++inverseAgeOfMessage
    }

    /**
     * Creates a notification when a direct message has been received. Vibrates and shows the passed in text
     */
    fun makeMentionNotification(ctx: Context, message: CharSequence, chatId: String) {
        // intent meant for main activity. will launch the app
        val launchMainActivityIntent = service.getMainActivityIntent()
        launchMainActivityIntent.putExtra(EXTRA_LAUNCH_TO_CHAT_ID, chatId)
        val mainActivityPintent = PendingIntent.getActivity(ctx, 1, launchMainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // TODO use messagingstyle notificationcompat to group multiple message notifications. right now just replace to avoid spam
        // Will have to keep track of which notifications are shown and when notification is tapped/dismissed to correctly
        // update the notification
        val notificationBuilder = NotificationCompat.Builder(ctx, MENTION_NOTIFICATION_CHANNEL_ID)
        notificationBuilder
                .setContentTitle(ctx.getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_send)
                .setGroup("mentions")
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(mainActivityPintent)

        val notificationManager = NotificationManagerCompat.from(ctx)
        notificationManager.notify(PM_NOTIFICATION_ID, notificationBuilder.build())
    }

    /**
     * Notify the user if a private message was received
     */
    private fun notifyUserOfPm(messages: List<ServerChatMessage>?) {
        messages?.let {
            for (message in messages) {
                // it's a PM and not a system message
                if (message.channelNameServer.isPrivate && message.author.id != "-1") {
                    makeMentionNotification(service.getContext(), StringUtilities.getHtml(message.channelNameServer.name + ": " + message.htmlText), message.author.id)
                }
            }
        }
    }
}

/**
 * Types of message that can be sent our handler
 */
enum class MessageType {
    SEND_CHAT_MESSAGE, STOP, START, READ_ONCE, READ_UNTIL_THRESHOLD
}

/**
 * Message that can be sent to our handler. textmessage is only used if this is a SEND_CHAT_MESSAGE
 */
data class ChatServiceMessage(val type: MessageType, val textmessage: String?)
