package biz.ajoshi.kolchat

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.ajoshi.kolchat.arch.ChatMessageViewModel
import biz.ajoshi.kolchat.persistence.ChatMessage
import biz.ajoshi.kolchat.view.ChatAdapter
import biz.ajoshi.kolchat.view.ChatInputView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Fragment displaying a conversation in a channel or with a user. Uses the arch components instead of rxjava
 */
// TODO give new values when fully moving to arch components
const val EXTRA_CHANNEL_ID = "biz.ajoshi.kolchat.ExtraChannelId"
const val EXTRA_CHANNEL_NAME = "biz.ajoshi.kolchat.ExtraChannelName"
const val EXTRA_CHANNEL_IS_PRIVATE = "biz.ajoshi.kolchat.ExtraChannelPrivate"

class ChatMessageFrag : LifecycleFragment() {
    var id = "newbie"
    var name = "newbie"
    var isPrivate = false
    var initialChatLoadSubscriber: Disposable? = null

    var chatAdapter: ChatAdapter? = null
    var recyclerView: RecyclerView? = null

    var lastMessage: ChatMessage? = null;

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.chat_detail_custom, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val args = arguments
        if (args != null) {
            id = args.getString(EXTRA_CHANNEL_ID)
            name = args.getString(EXTRA_CHANNEL_NAME)
            isPrivate = args.getBoolean((EXTRA_CHANNEL_IS_PRIVATE))
        }

//        messagesListAdapter = MessagesListAdapter<ChatkitMessage>(id, null)
//        val messagesList = activity?.findViewById(R.id.messagesList) as MessagesList
//        messagesList.setAdapter(messagesListAdapter)

        val layoutMgr = LinearLayoutManager(activity)
        chatAdapter = ChatAdapter(layoutMgr)
        recyclerView = activity?.findViewById<RecyclerView>(R.id.messagesList) as RecyclerView
        recyclerView?.adapter = chatAdapter
        recyclerView?.layoutManager = layoutMgr

        val vm: ChatMessageViewModel = ViewModelProviders.of(this).get(ChatMessageViewModel::class.java)

        initialChatLoadSubscriber = Observable.fromCallable {
            KolChatApp.database
                    ?.MessageDao()
                    ?.getMessagesForChannel(id)
        }?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { list ->
                    chatAdapter?.setList(list)
                    lastMessage = list.last()
                    onInitialListLoad()
                    observeViewModel(vm)
                }


        val inputView = activity?.findViewById<ChatInputView>(R.id.input_view) as ChatInputView
        inputView.setSubmitListener { input: CharSequence? -> makePost(input) }

        super.onActivityCreated(savedInstanceState)
    }

    private fun onInitialListLoad() {
        // get rid of this observer now that we;ve loaded the initial list
        initialChatLoadSubscriber?.dispose()
    }

    private fun observeViewModel(viewModel: ChatMessageViewModel) {
        viewModel.getChatListObservable(id, lastMessage?.timeStamp ?: 0)?.observe(this, Observer
        { message ->
            if (message != null)
                chatAdapter?.addToBottom(message)
        })
    }

    fun makePost(post: CharSequence?): Boolean {

        if (post != null) {
            val serviceIntent = Intent(activity, ChatService::class.java)
            when (isPrivate) {
                true -> serviceIntent.putExtra(EXTRA_CHAT_MESSAGE_TO_SEND, "/w ${id} ${post}")
                false -> serviceIntent.putExtra(EXTRA_CHAT_MESSAGE_TO_SEND, "/${id} ${post}")
            }
            activity.startService(serviceIntent)
        }
        return true
    }
}
