package biz.ajoshi.kolchat.arch

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import biz.ajoshi.kolchat.persistence.chat.ChatMessage

/**
 * Created by ajoshi on 7/22/17.
 */
class ChatMessageViewModel(application: Application) : AndroidViewModel(application) {
    private var chatMessageObservable: LiveData<ChatMessage>? = null
    private var chatListObservable: LiveData<List<ChatMessage>>? = null

    /**
     * We want a livedata that only gives us data for this channel. We never want to share data streams across channels
     */
    fun getLastChatObservable(channelId: String): LiveData<ChatMessage>? {
        if (chatMessageObservable == null) {
            chatMessageObservable = ChatRepository().getLastChatStreamForChannel(channelId = channelId)
        }
        return chatMessageObservable
    }

    /**
     * Livedata for this channel starting from the passed in time so we don't get dupes of the newest message
     */
    fun getChatListObservable(channelId: String, timeStamp: Long): LiveData<List<ChatMessage>>? {
        if (chatListObservable == null) {
            chatListObservable = ChatRepository().getChatStreamForChannel(channelId = channelId, timestamp = timeStamp)
        }
        return chatListObservable
    }
}
