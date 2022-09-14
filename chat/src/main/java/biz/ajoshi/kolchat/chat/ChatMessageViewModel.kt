package biz.ajoshi.kolchat.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.*
import biz.ajoshi.kolchat.chat.arch.ChatRepository
import biz.ajoshi.kolchat.persistence.chat.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * Created by ajoshi on 7/22/17.
 */
class ChatMessageViewModel(application: Application) : AndroidViewModel(application) {
    private var chatMessageObservable: LiveData<ChatMessage>? = null
    private var chatListObservable: LiveData<List<ChatMessage>>? = null

    /**
     * We want a livedata that only gives us data for this channel. We never want to share data streams across channels
     */
    fun getLastChatObservable(
        channelId: String,
        userId: String
    ): LiveData<PagingData<ChatMessage>> {
        return Pager(config = PagingConfig(pageSize = 50, prefetchDistance = 10),
            pagingSourceFactory = { ChatRepository().getLastChatStreamForChannel(channelId = channelId, userId = userId) }
        ).liveData.cachedIn(this)
    }

    fun getLastChatObservable(
        channelId: String,
        userId: String,
        scope: CoroutineScope
    ): Flow<PagingData<ChatMessage>> {
        return Pager(config = PagingConfig(pageSize = 50, prefetchDistance = 10),
            pagingSourceFactory = { ChatRepository().getLastChatStreamForChannel(channelId = channelId, userId = userId) }
        ).flow.cachedIn(scope)
    }

    /**
     * Livedata for this channel starting from the passed in time so we don't get dupes of the newest message
     */
    fun getChatListObservable(
        channelId: String,
        userId: String,
        timeStamp: Long
    ): LiveData<List<ChatMessage>>? {
        if (chatListObservable == null) {
            chatListObservable = ChatRepository().getChatStreamForChannel(
                channelId = channelId,
                userId = userId,
                timestamp = timeStamp
            )
        }
        return chatListObservable
    }
}
