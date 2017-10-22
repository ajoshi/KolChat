package biz.ajoshi.kolchat

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.support.v4.app.NotificationCompat
import biz.ajoshi.kolchat.model.ServerChatMessage
import biz.ajoshi.kolchat.model.User
import biz.ajoshi.kolchat.persistence.RoomInserter
import android.app.NotificationManager
import android.text.Html


const val EXTRA_POLL_INTERVAL_IN_MS = "biz.ajoshi.kolchat.ChatService.pollInterval";
const val EXTRA_CHAT_MESSAGE_TO_SEND = "biz.ajoshi.kolchat.ChatService.messageToSend";
const val EXTRA_STOP = "biz.ajoshi.kolchat.ChatService.staaaaahp";
// normally we'll poll ever 3 seconds
const val DEFAULT_POLL_INTERVAL = 3000
const val SHARED_PREF_NAME = "chat"
const val SHARED_PREF_LAST_FETCH_TIME = "lastFetched"
/**
 * Service that spins bg task to periodically poll for chat messages.
 * Needs to be a service so we can get messages even when app isn't in foreground
 */
class ChatService() : Service() {
    var pollInterval: Long = DEFAULT_POLL_INTERVAL.toLong();

    var serviceLooper: Looper? = null
    var serviceHandler: ServiceHandler? = null

    var sharedPref: SharedPreferences? = null
    var lastFetchedTime: Long = 0

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    inner class ServiceHandler(looper: Looper) : Handler(looper) {
        val roomInserter = RoomInserter()
        override fun handleMessage(msg: Message?) {
            if (ChatSingleton.chatManager == null) {
                // not logged in so exit service. may be premature and a bad idea
                stop(msg?.arg1?: -1)
                return
            }
            if (msg != null && msg.obj != null) {
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
                        stop(msg.arg1?: -1)
                        return
                    }

                    MessageType.START -> {
                        // we got told to start
                        sendMessageDelayed(obtainLoopMessage(msg.arg1), pollInterval)
                    }
                }

            } else {
                // else check for new messages and reschedule to check in 5 seconds
                if (msg != null) {
                    sendMessageDelayed(obtainLoopMessage(msg.arg1), pollInterval)
                }

                val messages = ChatSingleton.readChat(lastFetchedTime)
                // if we can, read the chat and stick in db
                insertChatsIntoDb(messages)
                notifyUserOfPm(messages)
                lastFetchedTime = ChatSingleton.chatManager!!.lastSeen
            }
        }

        fun insertChatsIntoDb(messages: List<ServerChatMessage>?) {
            if (messages != null) {
                roomInserter.insertAllMessages(messages)
            }
        }
    }

    /**
     * Notify the user if a private msage was received
     */
    private fun notifyUserOfPm(messages: List<ServerChatMessage>?) {
        messages ?.let {
            for (message in messages) {
                if (message.channelNameServer.isPrivate) {
                    makeMentionNotification(this, Html.fromHtml(message.channelNameServer.name + ": " + message.htmlText))
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val thread = HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()

        serviceLooper = thread.looper
        if (serviceLooper != null) {
            serviceHandler = ServiceHandler(serviceLooper!!)
        }

        sharedPref = applicationContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        lastFetchedTime = sharedPref!!.getLong(SHARED_PREF_LAST_FETCH_TIME, 0)
    }

    /*
     * Called by OS with the given data. Parse input commands and do what needs to be done (start, stop, delay, etc)
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val msg = obtainLoopMessage(startId)
        val interval = intent?.extras?.getInt(EXTRA_POLL_INTERVAL_IN_MS, DEFAULT_POLL_INTERVAL)
        val chatMessageToSend = intent?.extras?.getString(EXTRA_CHAT_MESSAGE_TO_SEND, null)
        val shouldStop = intent?.getBooleanExtra(EXTRA_STOP, false) ?: false
        if (shouldStop) {
            stop(startId)
        } else {
            msg?.obj = if (chatMessageToSend == null) ChatServiceMessage(MessageType.START, null) else ChatServiceMessage(MessageType.CHAT_MESSAGE, chatMessageToSend)
            pollInterval = (interval?: DEFAULT_POLL_INTERVAL).toLong()
            serviceHandler?.sendMessage(msg)
            startForeground(R.string.notification_persistent, makePersistentNotification(this))
        }
        return START_STICKY;
    }

    /**
     * gets the default message for our looper. Takes in the startid of the service
     */
    fun obtainLoopMessage(id: Int): Message? {
        // arg1 holds the service startid
        // arg2 holds the poll interval we want the polling to have so it can be changed at will
        // obj is used to hold any new chat messages we want to post
        val msg = serviceHandler?.obtainMessage()
        msg?.arg1 = id
        msg?.arg2 = DEFAULT_POLL_INTERVAL
        return msg
    }

    /**
     * Called when this service needs to stop. Calls stopSelf and cleans up
     */
    fun stop (id: Int) {
        stopForeground(true)
        stopSelf(id)
    }

    /**
     * Make the persistent notification
     */
    fun makePersistentNotification(ctx: Context): Notification {
        // intent meant for this service. will be used to stop/start
        val stopServiceIntent = Intent(ctx, ChatService::class.java)
        stopServiceIntent.putExtra(EXTRA_STOP, true)
        val stopPIntent = PendingIntent.getService(ctx, 1, stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // intent meant for main activity. will launch the app
        val launchMainActivityIntent = Intent(ctx, MainActivity::class.java)
        val mainActivityPintent = PendingIntent.getActivity(ctx, 1, launchMainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(ctx, NotificationCompat.CATEGORY_PROGRESS)
        notificationBuilder
                .setContentTitle("KolChat is running")
                .setContentText(ctx.getString(R.string.notification_description))
                .setSmallIcon(R.drawable.ic_send)
                .setContentIntent(mainActivityPintent)
                .addAction(R.drawable.abc_ic_clear_material, "Logout", stopPIntent)

        return notificationBuilder.build()
    }

    /**
     * Creates a notification when a direct message has been received. Vibrates and shows the passed in text
     */
    fun makeMentionNotification(ctx: Context, message: CharSequence) {
        // intent meant for main activity. will launch the app
        val launchMainActivityIntent = Intent(ctx, MainActivity::class.java)
        val mainActivityPintent = PendingIntent.getActivity(ctx, 1, launchMainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(ctx, NotificationCompat.CATEGORY_PROGRESS)
        notificationBuilder
                .setContentTitle(ctx.getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setGroup("mentions")
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(mainActivityPintent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(R.string.notification_non_persistent, notificationBuilder.build())
    }

    override fun onDestroy() {
        // we got told to quit, so shut down the service looper
        serviceLooper?.quit()
        // todo is this safe?
        sharedPref?.edit()?.putLong(SHARED_PREF_LAST_FETCH_TIME, lastFetchedTime)?.apply()
    }
}

/**
 * Types of message that can be sent our handler
 */
private enum class MessageType {
    CHAT_MESSAGE, STOP, START
}

/**
 * Message that can be sent to our handler. textmessage is only used if this is a CHAT_MESSAGE
 */
private data class ChatServiceMessage(val type: MessageType, val textmessage: String?)
