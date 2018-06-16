package biz.ajoshi.kolchat.chat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.HandlerThread
import android.os.Process
import biz.ajoshi.kolnetwork.model.NetworkStatus
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat

/**
 * This job will run one in a while. It will read chat once and then go away.
 * The hope is that the user will be using the foreground service enough to not really need this
 * Users should listen for intents with ACTION_CHAT_COMMAND_FAILED action to show errors when sending chat fails
 *
 * Alternatives:
 * https://developers.google.com/cloud-messaging/network-manager (deprecated?)
 * https://github.com/firebase/firebase-jobdispatcher-android
 */
class ChatJob : Job() {
    var shouldReschedule = true

    override fun onRunJob(params: Params): Result {

        val thread = HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()
        val serviceLooper = thread.looper
        if (serviceLooper != null) {
            // it seems we sometimes want the slow polling job to log in. This is because the OS might not call the job
            // for a few hours and our session token becomes invalid
            if (ChatSingleton.loginIfNeeded(username = params.extras.getString(extra_username, ""),
                            password = params.extras.getString(extra_password, ""),
                            silent = true,
                            context = context) == NetworkStatus.SUCCESS) {
                val serviceHandler = ChatServiceHandler(serviceLooper, ServiceImpl())
                val message = serviceHandler.obtainLoopMessage(1)
                message?.obj = ChatServiceMessage(MessageType.READ_UNTIL_THRESHOLD, null)
                serviceHandler.sendMessage(message)
                // TODO maybe delay until we know success has happened?
                return Result.SUCCESS
            }
            // do nothing- the next run will run fine
            return Result.FAILURE
        }
        return Result.FAILURE
    }

    companion object {
        val tag = "chat_job_tag"
        val extra_activity_name = "extra_activity_name"
        val extra_package_name = "extra_package_name"
        val extra_username = "extra_username"
        val extra_password = "extra_password"
        var jobId: Int? = 0

        /**
         * Schedules the Chat fetching job. Chat will be fetched once every 15 minutes if internet is available
         */
        fun scheduleJob(mainActivityComponentName: ComponentName, username: String, password: String) {
            val bundleCompat = PersistableBundleCompat()
            bundleCompat.putString(extra_package_name, mainActivityComponentName.packageName)
            bundleCompat.putString(extra_activity_name, mainActivityComponentName.className)
            bundleCompat.putString(extra_username, username)
            bundleCompat.putString(extra_password, password)
            // schedule a poll once every 15 minutes (might be too slow)
            jobId = JobRequest.Builder(tag)
                    .setPeriodic(15 * 60_000)
                    .setExtras(bundleCompat)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .build()
                    .schedule()
        }

        /**
         * Call this in Application onCreate to set up the chat job manager
         */
        fun addJobCreator(appContext: Context) {
            JobManager.create(appContext).addJobCreator(ChatJobCreator())
        }

        /**
         * Stop all ChatJobs in progress/scheduled. Could also use jobid to cancel just one job
         */
        fun stopJob() {
            JobManager.instance().cancelAllForTag(tag)
        }
    }

    /**
     * Implements ChatService so we can show the notifications, etc
     * Alternatively, rename getContext and ChatJob can implement the interface directly
     */
    private inner class ServiceImpl : ChatServiceHandler.ChatService {
        override fun onRoIsOVer() {
            // do nothing
        }

        override fun onRollover() {
            // we don't care about RO. The next read by this service will happen way later and RO should be over by then
        }

        override fun stopChatService(id: Int) {
            // can we do more?
            shouldReschedule = false;
        }

        override fun getContext(): Context {
            return context
        }

        override fun getMainActivityIntent(): Intent {
            val mainActivityIntent = Intent()
            val componentName = params.extras.getString(extra_activity_name, "")
            val packageName = params.extras.getString(extra_package_name, "")
            mainActivityIntent.component = ComponentName(packageName, componentName)

            return mainActivityIntent
        }

    }
}

/**
 * JobCreator that handles creating the chat job. App will need to make its own if it wants to handle multiple jobs
 */
class ChatJobCreator : JobCreator {
    override fun create(tag: String): Job? {
        when (tag) {
            ChatJob.tag ->
                return ChatJob()
            else ->
                return null
        }
    }

}
