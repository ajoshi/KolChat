package biz.ajoshi.kolchat.chat.arch

import android.arch.lifecycle.LiveData
import biz.ajoshi.kolchat.persistence.KolDB
import biz.ajoshi.kolchat.persistence.chat.ChatMessage

/**
 * Potentially useless Repository class for livedata learning. I don't really see a point yet since Room seems to serve
 * just fine as a repository
 */
class ChatRepository {
    fun getLastChatStreamForChannel(channelId: String, userId: String): LiveData<ChatMessage>? {
        return KolDB.getDb()?.MessageDao()?.getLastMessageLivedataForChannel(channelId, userId)
    }

    fun getChatStreamForChannel(channelId: String, userId:String, timestamp: Long): LiveData<List<ChatMessage>>? {
        return KolDB.getDb()?.MessageDao()?.getLastMessagesLivedataForChannel(channelId, userId, timestamp)
    }
}
