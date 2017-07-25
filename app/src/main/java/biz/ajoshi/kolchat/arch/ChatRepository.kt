package biz.ajoshi.kolchat.arch

import android.arch.lifecycle.LiveData
import biz.ajoshi.kolchat.KolChatApp
import biz.ajoshi.kolchat.persistence.ChatMessage

/**
 * Potentially useless Repository class for livedata learning. I don't really see a point yet since Room seems to serve
 * just fine as a repository
 */
class ChatRepository {
    fun getLastChatStreamForChannel(channelId : String) : LiveData<ChatMessage>? {
        return KolChatApp.database?.MessageDao()?.getLastMessageLivedataForChannel(channelId)
    }

    fun getChatStreamForChannel(channelId : String) : LiveData<List<ChatMessage>>? {
        return KolChatApp.database?.MessageDao()?.getLastMessagesLivedataForChannel(channelId)
    }
}
