package biz.ajoshi.kolchat

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import biz.ajoshi.kolchat.MainActivity.EXTRA_LAUNCH_TO_CHAT_ID
import biz.ajoshi.kolchat.model.ServerChatMessage
import biz.ajoshi.kolchat.persistence.RoomInserter
import biz.ajoshi.kolchat.util.StringUtil
import java.io.IOException

// normally we'll poll every 3 seconds
const val DEFAULT_POLL_INTERVAL = 3000
const val FOREGROUND_NOTIFICATION_ID = 666
const val PM_NOTIFICATION_ID = 667

/**
 * A Handler that lets us read chat and insert to DB
 */
class ChatServiceHandler(looper: Looper, val service: ChatService) : Handler(looper) {

    interface ChatService {
        fun stopChatService(id: Int)
        fun getContext(): Context
    }

    val roomInserter = RoomInserter()
    var pollInterval: Long = DEFAULT_POLL_INTERVAL.toLong()
    var lastFetchedTime: Long = 0

    override fun handleMessage(msg: Message?) {
        try {
            if (ChatSingleton.chatManager == null ||  // chatmgr is null so we have no userinfo to use for login
                    (!ChatSingleton.chatManager!!.network.isLoggedIn  // we're not logged in (but have the ability)
                            && !ChatSingleton.chatManager!!.network.login())) { // tried to login, but couldnt
                // not logged in so exit service. may be premature and a bad idea
                // we couldn't log in so... stopChatService the service?
                // TODO maybe notify user that error occurred here?
                service.stopChatService(msg?.arg1 ?: -1)
                return
            }
            if (msg?.obj != null) {
                // if we had a message to send then send it
                val serviceMessage = (msg.obj as ChatServiceMessage)
                when (serviceMessage.type) {
                    MessageType.CHAT_MESSAGE -> {
                        if (serviceMessage.textmessage != null) {
                            insertChatsIntoDb((ChatSingleton.postChat(serviceMessage.textmessage)))
                        }
                    }

                    MessageType.STOP -> {
                        // we got told to quit
                        service.stopChatService(msg.arg1)
                        return
                    }

                    MessageType.START -> {
                        // we got told to start
                        sendMessageDelayed(obtainLoopMessage(msg.arg1), pollInterval)
                    }

                    MessageType.READ_ONCE -> {
                        // read once and do not send messae to read again
                        readChat()
                    }
                }

            } else {
                // else check for new commands and reschedule to check in a bit
                if (msg != null) {
                    sendMessageDelayed(obtainLoopMessage(msg.arg1), pollInterval)
                }
                readChat()
            }
        } catch (exception: IOException) {
            // an ioexception occured. This means we're in a bad network location. try again later
            val newMessage = cloneMessage(msg)
            newMessage ?: sendMessageDelayed(newMessage, pollInterval)

        }
    }

    /**
     * Reads queued chat messages and inserts them into DB. If user has any direct messages, a notification is also created
     */
    private fun readChat() {
        val messages = ChatSingleton.readChat(lastFetchedTime)
        // if we can, read the chat and stick in db
        insertChatsIntoDb(messages)
        notifyUserOfPm(messages)
        lastFetchedTime = ChatSingleton.chatManager!!.lastSeen
    }

    fun insertChatsIntoDb(messages: List<ServerChatMessage>?) {
        if (messages != null) {
            roomInserter.insertAllMessages(messages)
        }
    }

    /**
     * gets the default message for our looper. Takes in the startid of the service
     */
    fun obtainLoopMessage(id: Int): Message? {
        // arg1 holds the service startid
        // arg2 holds the poll interval we want the polling to have so it can be changed at will
        // obj is used to hold any new chat commands we want to post
        val msg = obtainMessage()
        msg?.arg1 = id
        msg?.arg2 = DEFAULT_POLL_INTERVAL
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
     * Creates a notification when a direct message has been received. Vibrates and shows the passed in text
     */
    fun makeMentionNotification(ctx: Context, message: CharSequence, chatId: String) {
        // intent meant for main activity. will launch the app
        val launchMainActivityIntent = Intent(ctx, MainActivity::class.java)
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
     * Notify the user if a private msage was received
     */
    private fun notifyUserOfPm(messages: List<ServerChatMessage>?) {
        messages?.let {
            for (message in messages) {
                // it's a PM and not a system message
                if (message.channelNameServer.isPrivate && message.author.id != "-1") {
                    makeMentionNotification(service.getContext(), StringUtil.getHtml(message.channelNameServer.name + ": " + message.htmlText), message.author.id)
                }
            }
        }
    }
}

/**
 * Types of message that can be sent our handler
 */
enum class MessageType {
    CHAT_MESSAGE, STOP, START, READ_ONCE
}

/**
 * Message that can be sent to our handler. textmessage is only used if this is a CHAT_MESSAGE
 */
data class ChatServiceMessage(val type: MessageType, val textmessage: String?)
