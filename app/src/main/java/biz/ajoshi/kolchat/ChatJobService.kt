package biz.ajoshi.kolchat

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context

/**
 * This job will run one in a while. It will read chat once and then go away.
 * The hope is that the user will be using the foreground service enough to not really need this
 */
class ChatJobService : JobService(), ChatServiceHandler.ChatService {

    private var jobParams: JobParameters? = null

    override fun stopChatService(id: Int) {
       jobFinished(jobParams, false)
    }

    override fun getContext(): Context {
        return this
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        jobParams = params

    }

    override fun onStopJob(params: JobParameters?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
