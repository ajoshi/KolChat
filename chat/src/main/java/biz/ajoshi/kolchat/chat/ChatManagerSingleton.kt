package biz.ajoshi.kolchat.chat

import android.content.Context
import biz.ajoshi.kolnetwork.model.ServerChatMessage
import java.io.IOException

/**
 * Created by ajoshi on 7/13/17.
 */
object ChatSingleton {
    var chatManager: ChatManager? = null
    var network: biz.ajoshi.kolnetwork.Network? = null

    fun login(username: String, password: String, silent: Boolean, context: Context): Boolean {
        network = biz.ajoshi.kolnetwork.Network(username, password, silent)
        // TODO this assumes success. Handle failure
        network!!.login()
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
    fun postChat(message: String): List<ServerChatMessage>? {
        return chatManager?.post(message)
    }
}
