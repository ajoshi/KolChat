package biz.ajoshi.kolchat

import android.content.Context
import biz.ajoshi.kolchat.model.ServerChatMessage
import java.io.IOException

/**
 * Created by ajoshi on 7/13/17.
 */
object ChatSingleton {
    var chatManager: ChatManagerKotlin? = null
    var network: Network? = null

    const val SHARED_PREF_CHAT = "chatPreference"

    fun login(username: String, password: String, silent: Boolean, context: Context): Boolean {
        network = Network(username, password, silent)
        // TODO this assumes success. Handle failure
        network!!.login()
        if (!network!!.isLoggedIn) return false
        chatManager = ChatManagerKotlin(network!!, context.getSharedPreferences(SHARED_PREF_CHAT, Context.MODE_PRIVATE))
        return true
    }

    fun loginIfNeeded(username: String, password: String, silent: Boolean, context: Context): Boolean {
        if (network == null) {
            return login(username, password, silent, context)
        }
        return true
    }

    fun isLoggedIn(): Boolean {
        return network?.isLoggedIn() == true
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
