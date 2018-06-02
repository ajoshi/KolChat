package biz.ajoshi.kolchat.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.ajoshi.kolchat.Analytics
import biz.ajoshi.kolchat.EVENT_ATTRIBUTE_TIME_TAKEN
import biz.ajoshi.kolchat.R
import biz.ajoshi.kolchat.chat.ChatMessageViewModel
import biz.ajoshi.kolchat.chat.view.customviews.ChatDetailList
import biz.ajoshi.kolchat.chat.view.customviews.ChatInputView
import biz.ajoshi.kolchat.chat.view.customviews.QuickCommand
import biz.ajoshi.kolchat.chat.view.customviews.QuickCommandView
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.ContentViewEvent

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
    var chatLoadStartTimestamp = 0L

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

        chatLoadStartTimestamp = System.currentTimeMillis()
        chatDetailList?.loadInitialMessages(id, this)

        inputView = activity?.findViewById(R.id.input_view) as ChatInputView
        inputView?.setSubmitListener { input: CharSequence? -> makePost(input, isPrivate, id) }

        val quickCommands = activity?.findViewById(R.id.quick_commands) as QuickCommandView
        quickCommands.setClickListener(this)
        super.onActivityCreated(savedInstanceState)
    }

    /*
     * Listen to new chat commands that are made to this channel and show them. Will also listen for System Announcements
     */
    override fun onInitialMessageListLoaded() {
        val chatLoadEndTimestamp = System.currentTimeMillis()
        val vm: ChatMessageViewModel = ViewModelProviders.of(this).get(ChatMessageViewModel::class.java)
        vm.getChatListObservable(id, System.currentTimeMillis())?.observe(this, Observer
        { message ->
            if (message != null)
            //add this new message to the bottom (will scroll down if we're at the bottom of the list)
                chatDetailList?.addMessages(message)
        })
        Analytics.getAnswers()?.logContentView(ContentViewEvent()
                .putContentName("Channel detail opened")
                .putContentId(if (isPrivate) "PM" else name)
                .putCustomAttribute(EVENT_ATTRIBUTE_TIME_TAKEN, (chatLoadEndTimestamp - chatLoadStartTimestamp)))
    }

    override fun onCommandClicked(command: QuickCommand) {
        inputView?.appendInputText(command.command)
    }

    override fun getTitle(): String {
        return name
    }
}
