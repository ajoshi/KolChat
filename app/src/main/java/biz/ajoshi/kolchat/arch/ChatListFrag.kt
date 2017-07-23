package biz.ajoshi.kolchat.arch

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.ajoshi.kolchat.ChatSingleton
import biz.ajoshi.kolchat.R
import biz.ajoshi.kolchat.chatkit.ChatkitMessage
import biz.ajoshi.kolchat.view.ChatAdapter
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesListAdapter

/**
 * Fragment displaying a conversation in a channel or with a user. Uses the arch components instead of rxjava
 */
// TODO give new values when fully moving to arch components
const val EXTRA_CHANNEL_ID2 = "biz.ajoshi.kolchat.ExtraChannelId"
const val EXTRA_CHANNEL_NAME2 = "biz.ajoshi.kolchat.ExtraChannelName"
const val EXTRA_CHANNEL_IS_PRIVATE2 = "biz.ajoshi.kolchat.ExtraChannelPrivate"

class ChatListFrag : LifecycleFragment(){
    var id = "newbie"
    var name = "newbie"
    var isPrivate = false
    var messagesListAdapter: MessagesListAdapter<ChatkitMessage>? = null

    var myAdapter: ChatAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.chat_detail_custom, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val args = arguments
        if (args != null) {
            id = args.getString(EXTRA_CHANNEL_ID2)
            name = args.getString(EXTRA_CHANNEL_NAME2)
            isPrivate = args.getBoolean((EXTRA_CHANNEL_IS_PRIVATE2))
        }

//        messagesListAdapter = MessagesListAdapter<ChatkitMessage>(id, null)
//        val messagesList = activity?.findViewById(R.id.messagesList) as MessagesList
//        messagesList.setAdapter(messagesListAdapter)

        myAdapter = ChatAdapter()
        val recyclerView = activity?.findViewById(R.id.messagesList) as RecyclerView
        recyclerView.adapter = myAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        val vm : ChatMessageViewModel = ViewModelProviders.of(this).get(ChatMessageViewModel::class.java)
        observeViewModel(vm)

        val inputView = activity?.findViewById(R.id.input) as MessageInput
        inputView.setInputListener { input: CharSequence? -> makePost(input) }

        super.onActivityCreated(savedInstanceState)
    }

    private fun observeViewModel(viewModel: ChatMessageViewModel) {
        viewModel.getChatListObservable(id)?.observe(this, Observer
        {
            message -> if (message!= null)
            myAdapter?.addToBottom(message)
//                messagesListAdapter?.addToStart(ChatkitMessage(message), true)
        })
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
}
