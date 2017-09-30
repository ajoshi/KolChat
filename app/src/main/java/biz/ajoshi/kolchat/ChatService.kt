package biz.ajoshi.kolchat

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import biz.ajoshi.kolchat.model.ServerChatMessage
import biz.ajoshi.kolchat.model.User
import biz.ajoshi.kolchat.persistence.RoomInserter


const val EXTRA_POLL_INTERVAL_IN_MS = "biz.ajoshi.kolchat.ChatService.pollInterval";
const val EXTRA_CHAT_MESSAGE_TO_SEND = "biz.ajoshi.kolchat.ChatService.messageToSend";
// normally we'll poll ever 5 seconds
const val DEFAULT_POLL_INTERVAL = 5000
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
                stopSelf(msg?.arg1?: -1)
                return
            }
            if (msg != null && msg.obj != null) {
                // if we had a message to send then send it
                insertChatsIntoDb((ChatSingleton.postChat(msg.obj as String)))
            } else {
                // else check for new messages and reschedule to check in 5 seconds
                if (msg != null) {
                    sendMessageDelayed(obtainLoopMessage(msg.arg1), pollInterval)
                }

                // if we can, read the chat and stick in db
                insertChatsIntoDb(ChatSingleton.readChat(lastFetchedTime))
                lastFetchedTime = ChatSingleton.chatManager!!.lastSeen
            }
        }

        fun insertChatsIntoDb(messages: List<ServerChatMessage>?) {
            if (messages != null) {
                roomInserter.insertAllMessages(messages)
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val msg = obtainLoopMessage(startId)
        val interval = intent?.extras?.getInt(EXTRA_POLL_INTERVAL_IN_MS, DEFAULT_POLL_INTERVAL)
        val chatMessageToSend = intent?.extras?.getString(EXTRA_CHAT_MESSAGE_TO_SEND, null)
        msg?.obj = chatMessageToSend
        pollInterval = (interval?: DEFAULT_POLL_INTERVAL).toLong()
        serviceHandler?.sendMessage(msg)
        return START_STICKY;
    }

    fun obtainLoopMessage(id: Int): Message? {
        // arg1 holds the service startid
        // arg2 holds the poll interval we want the polling to have so it can be changed at will
        // obj is used to hold any new chat messages we want to post
        val msg = serviceHandler?.obtainMessage()
        msg?.arg1 = id
        msg?.arg2 = DEFAULT_POLL_INTERVAL
        return msg
    }


    override fun onDestroy() {
        // we got told to quit, so shut down the service looper
        serviceLooper?.quit()
        // todo is this safe?
        sharedPref?.edit()?.putLong(SHARED_PREF_LAST_FETCH_TIME, lastFetchedTime)?.apply()
    }
}


