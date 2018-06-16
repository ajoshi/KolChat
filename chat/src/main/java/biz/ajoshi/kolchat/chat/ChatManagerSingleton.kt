package biz.ajoshi.kolchat.chat

import android.content.Context
import biz.ajoshi.commonutils.Logg
import biz.ajoshi.kolnetwork.model.NetworkStatus
import biz.ajoshi.kolnetwork.model.ServerChatResponse
import java.io.IOException

/**
 * Created by ajoshi on 7/13/17.
 */
object ChatSingleton {
    var chatManager: ChatManager? = null
    var network: biz.ajoshi.kolnetwork.Network? = null
    val tag = "ChatSingleton"

    fun login(username: String, password: String, silent: Boolean, context: Context): NetworkStatus {
        network = biz.ajoshi.kolnetwork.Network(username, password, silent)
        network?.let {
            val status: NetworkStatus
            try {
                status = it.login().status
            } catch (exception: IOException) {
                Logg.logThrowable(tag, exception)
                return NetworkStatus.FAILURE
            }
            // return whatever the failure status was
            if (!it.isLoggedIn) return status
            chatManager = ChatManager(it, context.getSharedPreferences(CHAT_SHARED_PREF_NAME, Context.MODE_PRIVATE))
            return status
        }
        return NetworkStatus.FAILURE
    }

    fun loginIfNeeded(username: String, password: String, silent: Boolean, context: Context): NetworkStatus {
        if (network == null) {
            return login(username, password, silent, context)
        }
        return NetworkStatus.SUCCESS
    }

    fun isLoggedIn(): Boolean {
        return network?.isLoggedIn == true
    }

    @Throws(IOException::class)
    fun readChat(timeStamp: Long): ServerChatResponse? {
        return chatManager?.readChat(timeStamp)
    }

    @Throws(IOException::class)
    fun postChat(message: String): ServerChatResponse? {
        return chatManager?.post(message)
    }
}
