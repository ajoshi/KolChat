package biz.ajoshi.kolchat.chat.arch

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import biz.ajoshi.kolchat.persistence.KolDB
import biz.ajoshi.kolchat.persistence.chat.ChatMessage

/**
 * Potentially useless Repository class for livedata learning. I don't really see a point yet since Room seems to serve
 * just fine as a repository
 */
class ChatRepository {
    fun getLastChatStreamForChannel(
        channelId: String,
        userId: String
    ): PagingSource<Int, ChatMessage> {
        return KolDB.getDb()?.MessageDao()?.getLastMessageLivedataForChannel(channelId, userId)
            ?: (object : PagingSource<Int, ChatMessage>() {
                override fun getRefreshKey(state: PagingState<Int, ChatMessage>): Int? {
                    return null
                }

                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChatMessage> {
                    return LoadResult.Error(IllegalAccessError("fuck idk"))
                }
            })
    }

    fun getChatStreamForChannel(
        channelId: String,
        userId: String,
        timestamp: Long
    ): LiveData<List<ChatMessage>>? {
        return KolDB.getDb()?.MessageDao()
            ?.getLastMessagesLivedataForChannel(channelId, userId, timestamp)
    }
}
