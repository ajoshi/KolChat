package biz.ajoshi.kolchat

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import biz.ajoshi.kolchat.persistence.RoomInserter


const val EXTRA_POLL_INTERVAL_IN_MS = "biz.ajoshi.kolchat.ChatService.pollInterval";

/**
 * Service that spins bg task to periodically poll for chat messages.
 * Needs to be a service so we can get messages even when app isn't in foreground
 */
class ChatService() : Service() {
    // normally we'll poll ever 5 seconds
    val defaultPollInterval = 5000
    var pollInterval: Long = defaultPollInterval.toLong();

    var serviceLooper: Looper? = null
    var serviceHandler: ServiceHandler? = null

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message?) {
            if (ChatSingleton.chatManager == null) {
                // not logged in so exit service. may be premature and a bad idea
                stopSelf(msg?.arg1?: -1)
                return
            }
            if (msg != null) {
                sendMessageDelayed(obtainLoopMessage(msg.arg1), pollInterval)
            }
            // if we can, read the chat and stick in db
            val chatMessages = ChatSingleton.chatManager!!.readChat()
            val roomInserter = RoomInserter()
            for (chatMessage in chatMessages)  roomInserter.insertMessage(chatMessage)
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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val msg = obtainLoopMessage(startId)
        val interval = intent?.extras?.getInt(EXTRA_POLL_INTERVAL_IN_MS, defaultPollInterval)
        pollInterval = (interval?: defaultPollInterval).toLong()
        serviceHandler?.sendMessage(msg)
        return START_STICKY;
    }

    fun obtainLoopMessage(id: Int): Message? {
        val msg = serviceHandler?.obtainMessage()
        msg?.arg1 = id
        msg?.arg2 = defaultPollInterval
        return msg
    }


    override fun onDestroy() {
        // we got told to quit, so shut down the service looper
        serviceLooper?.quit()
    }
}


