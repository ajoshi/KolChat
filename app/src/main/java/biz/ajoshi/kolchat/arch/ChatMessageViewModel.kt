package biz.ajoshi.kolchat.arch

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import biz.ajoshi.kolchat.persistence.ChatMessage

/**
 * Created by ajoshi on 7/22/17.
 */
class ChatMessageViewModel(application: Application) : AndroidViewModel(application) {
    private var chatListObservable: LiveData<ChatMessage>? = null

    /**
     * We want a livedata that only gives us data for this channel. We never want to share data streams across channels
     */
    fun getChatListObservable(channelId: String) : LiveData<ChatMessage>? {
        if (chatListObservable == null) {
            chatListObservable = ChatRepository().getChatStreamForChannel(channelId = channelId)
        }
        return chatListObservable
    }
}
