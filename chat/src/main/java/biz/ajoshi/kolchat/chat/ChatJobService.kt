package biz.ajoshi.kolchat.chat

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent

/**
 * This job will run one in a while. It will read chat once and then go away.
 * The hope is that the user will be using the foreground service enough to not really need this
 * TODO https://developers.google.com/cloud-messaging/network-manager use that instead
 */
class ChatJobService : JobService(), ChatServiceHandler.ChatService {
    override fun getMainActivityIntent(): Intent {
        TODO("not implemented")
    }

    private var jobParams: JobParameters? = null

    override fun stopChatService(id: Int) {
        // jobFinished(jobParams, false)
    }

    override fun getContext(): Context {
        return this
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        jobParams = params


        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        TODO("not implemented")
    }

}
