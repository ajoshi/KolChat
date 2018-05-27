package biz.ajoshi.kolchat

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.ajoshi.kolchat.chat.ChatBackgroundService
import biz.ajoshi.kolchat.chat.ChatManager
import biz.ajoshi.kolchat.chat.ChatMessageViewModel
import biz.ajoshi.kolchat.chat.EXTRA_CHAT_MESSAGE_TO_SEND
import biz.ajoshi.kolchat.chat.view.ChatDetailList
import biz.ajoshi.kolchat.chat.view.ChatInputView
import biz.ajoshi.kolchat.chat.view.QuickCommand
import biz.ajoshi.kolchat.chat.view.QuickCommandView
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent

/**
 * Fragment displaying a conversation in a channel or with a user. Uses the arch components instead of rxjava
 */
// TODO give new values when fully moving to arch components
const val EXTRA_CHANNEL_ID = "biz.ajoshi.kolchat.ExtraChannelId"
const val EXTRA_CHANNEL_NAME = "biz.ajoshi.kolchat.ExtraChannelName"
const val EXTRA_CHANNEL_IS_PRIVATE = "biz.ajoshi.kolchat.ExtraChannelPrivate"

class ChatMessageFrag : BaseFragment(), QuickCommandView.CommandClickListener, ChatDetailList.ChatMessagesLoaderView {
    var id = "newbie"
    var name = "newbie"
    var isPrivate = false

    var chatDetailList: ChatDetailList? = null
    var inputView: ChatInputView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.chat_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val args = arguments
        if (args != null) {
            id = args.getString(EXTRA_CHANNEL_ID)
            name = args.getString(EXTRA_CHANNEL_NAME)
            isPrivate = args.getBoolean((EXTRA_CHANNEL_IS_PRIVATE))
        }

        chatDetailList = activity?.findViewById(R.id.messagesList) as ChatDetailList
        chatDetailList?.loadInitialMessages(id, this)

        inputView = activity?.findViewById(R.id.input_view) as ChatInputView
        inputView?.setSubmitListener { input: CharSequence? -> makePost(input) }

        val quickCommands = activity?.findViewById(R.id.quick_commands) as QuickCommandView
        quickCommands.setClickListener(this)
        super.onActivityCreated(savedInstanceState)
    }

    /*
     * Listen to new chat commands that are made to this channel and show them. Will also listen for System Announcements
     */
    override fun onInitialMessageListLoaded() {
        val vm: ChatMessageViewModel = ViewModelProviders.of(this).get(ChatMessageViewModel::class.java)
        vm.getChatListObservable(id, System.currentTimeMillis())?.observe(this, Observer
        { message ->
            if (message != null)
            //add this new message to the bottom (will scroll down if we're at the bottom of the list)
                chatDetailList?.addMessages(message)
        })
    }

    /**
     * Send a chat message to this channel/user
     */
    fun makePost(post: CharSequence?): Boolean {
        // log to analytics as well
        Answers.getInstance().logCustom(CustomEvent(EVENT_NAME_CHAT_MESSAGE_SENT)
                .putCustomAttribute(EVENT_ATTRIBUTE_RECIPIENT, if (isPrivate) "PM" else id)
                .putCustomAttribute(EVENT_ATTRIBUTE_MESSAGE_LENGTH, post?.length)
        )
        return sendChatCommand(ChatManager.getChatString(post, id, isPrivate))
    }

    /**
     * Send a chat command to the server. Is not scoped to this channel, so scoping should be
     * done before calling this
     */
    fun sendChatCommand(command: String): Boolean {
        val serviceIntent = Intent(activity, ChatBackgroundService::class.java)
        serviceIntent.putExtra(EXTRA_CHAT_MESSAGE_TO_SEND, command)
        activity?.startService(serviceIntent)
        return true
    }

    override fun onCommandClicked(command: QuickCommand) {
        inputView?.appendInputText(command.command)
    }

    override fun getTitle(): String {
        return name
    }
}
