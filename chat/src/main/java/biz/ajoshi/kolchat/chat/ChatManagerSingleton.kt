package biz.ajoshi.kolchat.chat

import android.content.Context
import biz.ajoshi.commonutils.Logg
import biz.ajoshi.kolnetwork.model.ServerChatMessage
import biz.ajoshi.kolnetwork.model.ServerChatCommandResponse
import java.io.IOException

/**
 * Created by ajoshi on 7/13/17.
 */
object ChatSingleton {
    var chatManager: ChatManager? = null
    var network: biz.ajoshi.kolnetwork.Network? = null
    val tag = "ChatSingleton"

    fun login(username: String, password: String, silent: Boolean, context: Context): Boolean {
        Logg.i(tag, "logging in as $username") // TODO remove this- no point logging username
        network = biz.ajoshi.kolnetwork.Network(username, password, silent)
        try {
            if (!network!!.login()) {
                Logg.i(tag, "couldn't log in to $username")
                return false
            }
        } catch (exception: IOException) {
            Logg.logThrowable(tag, exception)
            return false
        }
        if (!network!!.isLoggedIn) return false
        chatManager = ChatManager(network!!, context.getSharedPreferences(CHAT_SHARED_PREF_NAME, Context.MODE_PRIVATE))
        return true
    }

    fun loginIfNeeded(username: String, password: String, silent: Boolean, context: Context): Boolean {
        if (network == null) {
            return login(username, password, silent, context)
        }
        return true
    }

    fun isLoggedIn(): Boolean {
        return network?.isLoggedIn == true
    }

    @Throws(IOException::class)
    fun readChat(timeStamp: Long): List<ServerChatMessage>? {
        return chatManager?.readChat(timeStamp)
    }

    @Throws(IOException::class)
    fun postChat(message: String): ServerChatCommandResponse? {
        return chatManager?.post(message)
    }
}
