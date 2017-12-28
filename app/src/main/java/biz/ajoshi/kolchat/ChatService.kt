package biz.ajoshi.kolchat

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.support.v4.app.NotificationCompat


const val EXTRA_POLL_INTERVAL_IN_MS = "biz.ajoshi.kolchat.ChatService.pollInterval";
const val EXTRA_CHAT_MESSAGE_TO_SEND = "biz.ajoshi.kolchat.ChatService.messageToSend";
const val EXTRA_STOP = "biz.ajoshi.kolchat.ChatService.staaaaahp";
// normally we'll poll ever 3 seconds
const val DEFAULT_POLL_INTERVAL = 3000
const val SHARED_PREF_NAME = "chat"
const val SHARED_PREF_LAST_FETCH_TIME = "lastFetched"
/**
 * Service that spins bg task to periodically poll for chat commands.
 * Needs to be a service so we can get commands even when app isn't in foreground
 */
class ChatBackgroundService() : Service(), ChatServiceHandler.ChatService {
    override fun getContext(): Context {
        return this
    }

    var serviceLooper: Looper? = null
    var serviceHandler: ChatServiceHandler? = null

    var sharedPref: SharedPreferences? = null

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        super.onCreate()
        val thread = HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()

        serviceLooper = thread.looper
        if (serviceLooper != null) {
            serviceHandler = ChatServiceHandler(serviceLooper!!, this)
        }

        sharedPref = applicationContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        serviceHandler?.lastFetchedTime = sharedPref!!.getLong(SHARED_PREF_LAST_FETCH_TIME, 0)
    }

    /*
     * Called by OS with the given data. Parse input commands and do what needs to be done (start, stopChatService, delay, etc)
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val msg = serviceHandler?.obtainLoopMessage(startId)
        val interval = intent?.extras?.getInt(EXTRA_POLL_INTERVAL_IN_MS, DEFAULT_POLL_INTERVAL)
        val chatMessageToSend = intent?.extras?.getString(EXTRA_CHAT_MESSAGE_TO_SEND, null)
        val shouldStop = intent?.getBooleanExtra(EXTRA_STOP, false) ?: false
        if (shouldStop) {
            stopChatService(startId)
        } else {
            msg?.obj = if (chatMessageToSend == null) ChatServiceMessage(MessageType.START, null) else ChatServiceMessage(MessageType.CHAT_MESSAGE, chatMessageToSend)
            serviceHandler?.pollInterval = (interval?: DEFAULT_POLL_INTERVAL).toLong()
            serviceHandler?.sendMessage(msg)
            startForeground(R.string.notification_persistent, makePersistentNotification(this))
        }
        return START_STICKY;
    }



    /**
     * Called when this service needs to stopChatService. Calls stopSelf and cleans up
     */
    override fun stopChatService(id: Int) {
        stopForeground(true)
        stopSelf(id)
    }

    /**
     * Make the persistent notification
     */
    fun makePersistentNotification(ctx: Context): Notification {
        // intent meant for this service. will be used to stopChatService/start
        val stopServiceIntent = Intent(ctx, ChatBackgroundService::class.java)
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

    override fun onDestroy() {
        // we got told to quit, so shut down the service looper
        serviceLooper?.quit()
        // todo is this safe?
        val lastFetchedTime = serviceHandler?.lastFetchedTime
        lastFetchedTime?.let {
            sharedPref?.edit()?.putLong(SHARED_PREF_LAST_FETCH_TIME, lastFetchedTime)?.apply()
        }
        ChatSingleton.network?.logout()
    }
}
