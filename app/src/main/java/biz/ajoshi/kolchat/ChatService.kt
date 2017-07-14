package biz.ajoshi.kolchat

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log


/**
 * Created by ajoshi on 7/13/17.
 */
class ChatService() : Service() {
    val MESSAGE_LOOP = 1;
    val MESSAGE_STOP_LOOPING = 2;

    var serviceLooper: Looper? = null
    var serviceHandler: ServiceHandler? = null

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    inner class ServiceHandler(looper: Looper) : Handler(looper) {
        var stopNow = false
        override fun handleMessage(msg: Message?) {
            if (ChatManagerSingleton.chatManager == null) {
/// log in
                val network = Network("a", "b", true)
                network.login()
                ChatManagerSingleton.chatManager = ChatManagerKotlin(network)
            }
            if (!stopNow)
                when (msg?.arg2) {
                    MESSAGE_LOOP -> sendMessageDelayed(obtainLoopMessage(msg.arg1), 5000)
                // after chat is read, kill the service regardless of what other commans were sent
                    MESSAGE_STOP_LOOPING -> stop(msg?.arg1)
                }
            // if we can, read the chat (and at some point stick in in db)
            val chatMessages = ChatManagerSingleton.chatManager?.readChat()
            if (0 < chatMessages?.size as Int) {
                Log.e("ajoshi", chatMessages[0].htmlText)
            }
        }

        fun stop(id : Int) {
            stopNow = true
            stopSelf(id)
            stopSelf(id)
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
        var action = intent?.extras?.getInt("STOP", MESSAGE_LOOP)
        if (action == null) {
            action = MESSAGE_LOOP
        }
        msg?.arg2 = action
        if (serviceHandler != null) {
            serviceHandler!!.sendMessage(msg)
        }
        return START_STICKY;
    }

    fun obtainLoopMessage(id: Int): Message? {
        val msg = serviceHandler?.obtainMessage()
        msg?.arg1 = id
        msg?.arg2 = MESSAGE_LOOP
        return msg
    }
}


