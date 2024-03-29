package biz.ajoshi.kolchat.chat

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import androidx.core.app.NotificationCompat
import biz.ajoshi.commonutils.Logg


const val EXTRA_POLL_INTERVAL_IN_MS = "biz.ajoshi.kolchat.ChatService.pollInterval"
const val EXTRA_CHAT_MESSAGE_TO_SEND = "biz.ajoshi.kolchat.ChatService.messageToSend"
const val EXTRA_MAIN_ACTIVITY_COMPONENTNAME =
    "biz.ajoshi.kolchat.ChatService.mainActivityComponentName"
const val EXTRA_STOP = "biz.ajoshi.kolchat.ChatService.staaaaahp"

// Name of the sharedPref file
const val CHAT_SHARED_PREF_NAME = "chat"

// sharedpref for the timestamp of the last fetched chat
const val SHARED_PREF_LAST_FETCH_TIME = "lastFetched"
const val PERSISTENT_NOTIFICATION_CHANNEL_ID = "kolPersist"
const val MENTION_NOTIFICATION_CHANNEL_ID = "kolMention"

/**
 * Service that spins bg task to periodically poll for chat commands.
 * Needs to be a service so we can get commands even when app isn't in foreground.
 * Users should listen for intents with ACTION_CHAT_COMMAND_FAILED action to show errors when sending chat fails
 */
class ChatBackgroundService : Service(), ChatServiceHandler.ChatService {
    override fun onRoIsOVer() {
        val broadcastIntent = Intent(ACTION_CHAT_ROLLOVER_OVER)
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(getContext())
            .sendBroadcast(broadcastIntent)
    }

    override fun onRollover() {
        val broadcastIntent = Intent(ACTION_CHAT_ROLLOVER)
        broadcastIntent.putExtra(
            EXTRA_FAILED_CHAT_MESSAGE,
            "Rollover in progress, restart in 5 minutes"
        )
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(getContext())
            .sendBroadcast(broadcastIntent)
    }

    override fun getContext(): Context {
        return this
    }

    private lateinit var serviceLooper: Looper
    private lateinit var serviceHandler: ChatServiceHandler
    private var mainActivityComponentName: ComponentName? = null

    var sharedPref: SharedPreferences? = null

    override fun onBind(intent: Intent?): IBinder {
        // Hammer time, they can't bind me, hammer time
        throw UnsupportedOperationException("You can't bind this")
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // O won't show notifications unless we add channels, and we can't define channels for anything less than O
            val notificationMgr =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // The persistent notification to tell people we're running and provide a logout button
            // This gets auto-collapsed so it doesn't eat up too much space
            val persistentChannel = NotificationChannel(
                PERSISTENT_NOTIFICATION_CHANNEL_ID,
                getContext().getString(R.string.aj_chat_persistent_channel_name),
                NotificationManager.IMPORTANCE_MIN
            )
            persistentChannel.description =
                getContext().getString(R.string.aj_chat_persistent_channel_desc)
            notificationMgr.createNotificationChannel(persistentChannel)

            // notification that only shows up when a PM is sent
            val mentionChannel = NotificationChannel(
                MENTION_NOTIFICATION_CHANNEL_ID,
                getContext().getString(R.string.aj_chat_pm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            mentionChannel.description = getContext().getString(R.string.aj_chat_pm_channel_desc)
            notificationMgr.createNotificationChannel(mentionChannel)
        }

        val thread = HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()

        serviceLooper = thread.looper
        serviceHandler = ChatServiceHandler(serviceLooper, this)

        sharedPref =
            applicationContext.getSharedPreferences(CHAT_SHARED_PREF_NAME, Context.MODE_PRIVATE)
        serviceHandler.lastFetchedTime = sharedPref!!.getLong(SHARED_PREF_LAST_FETCH_TIME, 0)
    }

    /*
     * Called by OS with the given data. Parse input commands and do what needs to be done (start, stopChatService, delay, etc)
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val msg = serviceHandler.obtainLoopMessage(startId)
        val interval = intent?.extras?.getLong(EXTRA_POLL_INTERVAL_IN_MS, DEFAULT_POLL_INTERVAL)
        val chatMessageToSend = intent?.extras?.getString(EXTRA_CHAT_MESSAGE_TO_SEND, null)
        intent?.extras?.getParcelable<ComponentName>(EXTRA_MAIN_ACTIVITY_COMPONENTNAME)?.let {
            mainActivityComponentName = it
        }
        if (mainActivityComponentName == null) {
            Logg.e("No componentname was passed in for ChatBGService- no activity launchable on tap")
        }
        val shouldStop = intent?.getBooleanExtra(EXTRA_STOP, false) ?: false
        if (shouldStop) {
            stopChatService(startId)
            ChatSingleton.network?.logout()
        } else {
            if (chatMessageToSend == null) {
                msg.obj = ChatServiceMessage(MessageType.START, null)
                msg.arg2 = serviceHandler.getAgeIntForNewMessage()
            } else {
                msg.obj = ChatServiceMessage(MessageType.SEND_CHAT_MESSAGE, chatMessageToSend)
            }
            serviceHandler.pollInterval = (interval ?: DEFAULT_POLL_INTERVAL)
            serviceHandler.sendMessage(msg)
            startForeground(FOREGROUND_NOTIFICATION_ID, makePersistentNotification(this))

        }
        return START_STICKY_COMPATIBILITY
    }


    /**
     * Called when this service needs to stopChatService. Calls stopSelf and cleans up
     */
    override fun stopChatService(id: Int) {
        stopForeground(true)
        stopSelf(id)
    }

    override fun getMainActivityIntent(): Intent {
        val launchMainActivityIntent = Intent()
        launchMainActivityIntent.component = mainActivityComponentName
        return launchMainActivityIntent
    }

    /**
     * Make the persistent notification
     */
    private fun makePersistentNotification(ctx: Context): Notification {
        // intent meant for this service. will be used to stopChatService/start
        val stopServiceIntent = Intent(ctx, ChatBackgroundService::class.java)
        val intentFlag = PendingIntent.FLAG_IMMUTABLE

        stopServiceIntent.putExtra(EXTRA_STOP, true)
        val stopPIntent =
            PendingIntent.getService(ctx, 1, stopServiceIntent, intentFlag)

        // intent meant for main activity. will launch the app
        val launchMainActivityIntent = getMainActivityIntent()
        val mainActivityPintent = PendingIntent.getActivity(
            ctx,
            1,
            launchMainActivityIntent,
            intentFlag
        )

        val notificationBuilder =
            NotificationCompat.Builder(ctx, NotificationCompat.CATEGORY_PROGRESS)
        notificationBuilder
            .setContentTitle("KolChat is running")
            .setContentText(ctx.getString(R.string.aj_chat_notification_description))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(mainActivityPintent)
            .setChannelId(PERSISTENT_NOTIFICATION_CHANNEL_ID)
            .addAction(android.R.drawable.ic_lock_power_off, "Logout", stopPIntent)
        return notificationBuilder.build()
    }

    override fun onDestroy() {
        // we got told to quit, so shut down the service looper
        serviceLooper.quit()
        // todo is this safe?
        val lastFetchedTime = serviceHandler.lastFetchedTime
        lastFetchedTime.let {
            sharedPref?.edit()?.putLong(SHARED_PREF_LAST_FETCH_TIME, lastFetchedTime)?.apply()
        }
    }
}
