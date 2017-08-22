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
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Shows a conversation between two people or in a chatroom
 */
const val EXTRA_CHANNEL_ID = "biz.ajoshi.kolchat.ExtraChannelId"
const val EXTRA_CHANNEL_NAME = "biz.ajoshi.kolchat.ExtraChannelName"
const val EXTRA_CHANNEL_IS_PRIVATE = "biz.ajoshi.kolchat.ExtraChannelPrivate"

@Deprecated("Use ChatMessageFrag now")
class ChatMessageFragment() : Fragment() {

    var id = "newbie"
    var name = "newbie"
    var isPrivate = false

    var chatUpdateSubscriber: Disposable? = null
    var initialChatLoadSubscriber: Disposable? = null
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

        initialChatLoadSubscriber = Observable.fromCallable {
            KolChatApp.database
                    ?.MessageDao()
                    ?.getMessagesForChannel(id)
        }?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.map {
                    messages ->
                    makeDialogs(messages)
                }
                ?.subscribe { list ->
                    messagesListAdapter.addToEnd(list, false)
                    subscribeToChatupdates(messagesListAdapter, messagesList)
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


    override fun onDestroy() {
        if (chatUpdateSubscriber != null && !chatUpdateSubscriber!!.isDisposed) {
            chatUpdateSubscriber!!.dispose()
        }
        super.onDestroy()
    }

    /**
     * Subscribes to the 'get me the last message seen in this channel' query so it can insert items into the ui as they
     * come in
     */
    fun subscribeToChatupdates(adapter: MessagesListAdapter<ChatkitMessage>, uiList: MessagesList) {
        initialChatLoadSubscriber?.dispose()
        chatUpdateSubscriber = KolChatApp.database
                ?.MessageDao()
                ?.getLastMessageForChannel(id)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.map {
                    message ->
                    ChatkitMessage(message)
                }
                ?.subscribe {
                    dialog ->
                    // can't tell if we have dupes, so this dupes the last message
                    adapter.addToStart(dialog, false)
                    uiList.scrollToPosition(adapter.itemCount)
                }
    }

    /**
     * Converts a list of ChatMessages to a list of ChatkitMessages
     */
    fun makeDialogs(messages: List<ChatMessage>): List<ChatkitMessage> {
        val chatKitMessages = mutableListOf<ChatkitMessage>()
        for (message in messages) chatKitMessages.add(ChatkitMessage(message))
        return chatKitMessages
    }
}
