package biz.ajoshi.kolchat

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.ajoshi.kolchat.chatkit.ChatkitMessage
import biz.ajoshi.kolchat.persistence.ChatMessage
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Shows a conversation between two people or in a chatroom
 */
const val EXTRA_CHANNEL_ID = "biz.ajoshi.kolchat.ExtraChannelId"
const val EXTRA_CHANNEL_NAME = "biz.ajoshi.kolchat.ExtraChannelName"
const val EXTRA_CHANNEL_IS_PRIVATE = "biz.ajoshi.kolchat.ExtraChannelPrivate"

class ChatMessageFragment() : Fragment() {

    var messagesListAdapter: MessagesListAdapter<ChatkitMessage>? = null
    var id = "newbie"
    var name = "newbie"
    var isPrivate = false
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.chat_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val args = arguments
        if (args != null) {
            id = args.getString(EXTRA_CHANNEL_ID)
            name = args.getString(EXTRA_CHANNEL_NAME)
            isPrivate = args.getBoolean((EXTRA_CHANNEL_IS_PRIVATE))
        }
        val messagesList = activity?.findViewById(R.id.messagesList) as MessagesList
        val messagesListAdapter = MessagesListAdapter<ChatkitMessage>(id, null)
        messagesList.setAdapter(messagesListAdapter)

        KolChatApp.database
                ?.MessageDao()
                ?.getMessagesForChannel(id)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.map {
                    messageList ->
                    makeDialogs(messageList)
                }
                ?.subscribe {
                    dialogs ->
                    messagesListAdapter.addToEnd(dialogs, false)
                }

        val inputView = activity?.findViewById(R.id.input) as MessageInput
        inputView.setInputListener { input: CharSequence? -> makePost(input) }
        super.onActivityCreated(savedInstanceState)
    }

    fun makePost(post: CharSequence?): Boolean {
        if (post != null) {
            val groupChatFormat = "/{1} {2}"
            val privateChatFormat = "/w {1} {2}"
            when (isPrivate) {
                // TODO make this call off the ui thread
                true -> ChatSingleton.chatManager?.post(String.format(privateChatFormat, post))
                false -> ChatSingleton.chatManager?.post(String.format(groupChatFormat, post))
            }

        }
        return true
    }

    fun makeDialogs(messages: List<ChatMessage>): List<ChatkitMessage> {
        val chatKitMessages = mutableListOf<ChatkitMessage>()
        for (message in messages) chatKitMessages.add(ChatkitMessage(message))
        return chatKitMessages
    }
}
