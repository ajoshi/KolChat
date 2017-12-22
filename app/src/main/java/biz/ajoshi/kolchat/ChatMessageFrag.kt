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
            // get n messages for this channel (n is a limit we set in the data source (100 right now))
        }?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { list ->
                    // we have the list, so set it as the displayed list
                    chatAdapter?.setList(list)
                    onInitialListLoad()
                    // subscribe to future messages for this channel + System updates
                    observeViewModel(vm)
                }


        val inputView = activity?.findViewById<ChatInputView>(R.id.input_view) as ChatInputView
        inputView.setSubmitListener { input: CharSequence? -> makePost(input) }

        super.onActivityCreated(savedInstanceState)
    }

    private fun onInitialListLoad() {
        // get rid of this observer now that we've loaded the initial list
        initialChatLoadSubscriber?.dispose()
    }

    /**
     * Listen to new chat messages that are made to this channel and show them. Will also listen for System Announcements
     */
    private fun observeViewModel(viewModel: ChatMessageViewModel) {
        viewModel.getChatListObservable(id, System.currentTimeMillis())?.observe(this, Observer
        { message ->
            if (message != null)
                //ad this new message to the bottom (will scroll down if we're at the bottom of the list)
                chatAdapter?.addToBottom(message)
        })
    }

    /**
     * Send a chat message to this channel/user
     */
    fun makePost(post: CharSequence?): Boolean {
        if (post != null) {
            if (post[0] == '/') {
                return sendChatCommand(post.toString())
            }
            when (isPrivate) {
                true -> return sendChatCommand("/w ${id} ${post}")
                false -> return sendChatCommand("/${id} ${post}")
            }
        }
        return false
    }

    /**
     * Send a chat command to the server. Is not scoped to this channel, so scoping should be done before calling this
     */
    fun sendChatCommand(command: String): Boolean {
        val serviceIntent = Intent(activity, ChatService::class.java)
        serviceIntent.putExtra(EXTRA_CHAT_MESSAGE_TO_SEND, command)
        activity.startService(serviceIntent)
        return true
    }
}
