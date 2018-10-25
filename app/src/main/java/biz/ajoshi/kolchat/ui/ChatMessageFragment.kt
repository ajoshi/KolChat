package biz.ajoshi.kolchat.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import biz.ajoshi.kolchat.Analytics
import biz.ajoshi.kolchat.EVENT_ATTRIBUTE_TIME_TAKEN
import biz.ajoshi.kolchat.R
import biz.ajoshi.kolchat.chat.ChatMessageViewModel
import biz.ajoshi.kolchat.chat.view.customviews.ChatDetailList
import biz.ajoshi.kolchat.chat.view.customviews.ChatInputView
import biz.ajoshi.kolchat.chat.view.customviews.QuickCommand
import biz.ajoshi.kolchat.chat.view.customviews.QuickCommandView
import com.crashlytics.android.answers.ContentViewEvent
import kotlinx.android.synthetic.main.chat_detail.*

/**
 * Fragment displaying a conversation in a channel or with a user. Uses the arch components instead of rxjava
 */
// TODO give new values when fully moving to arch components
const val EXTRA_CHANNEL_ID = "biz.ajoshi.kolchat.ExtraChannelId"
const val EXTRA_CHANNEL_NAME = "biz.ajoshi.kolchat.ExtraChannelName"
const val EXTRA_CHANNEL_IS_PRIVATE = "biz.ajoshi.kolchat.ExtraChannelPrivate"
// lets the app disable the input view if it wants. doesn't let it disable when the fragment is already up, but nbd
const val EXTRA_CHANNEL_IS_COMPOSER_DISABLED = "biz.ajoshi.kolchat.ExtraChannelIsComposerDisabled"

class ChatMessageFragment : BaseFragment(), QuickCommandView.CommandClickListener, ChatDetailList.ChatMessagesLoaderView {
    var id = "newbie"
    var name = "newbie"
    var isPrivate = false
    var chatLoadStartTimestamp = 0L
    var isComposerDisabled = true

    var chatDetailList: ChatDetailList? = null
    var inputView: ChatInputView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.chat_detail, container, false)
    }

    override fun onResume() {
        super.onResume()
        // only set the label if the destination is a chat list as well
        if (findNavController().currentDestination?.id == R.id.nav_chat_message) {
            findNavController().currentDestination?.label = getTitle()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val args = arguments
        if (args != null) {
            id = args.getString(EXTRA_CHANNEL_ID)!!
            name = args.getString(EXTRA_CHANNEL_NAME)!!
            isPrivate = args.getBoolean((EXTRA_CHANNEL_IS_PRIVATE))
            isComposerDisabled = args.getBoolean(EXTRA_CHANNEL_IS_COMPOSER_DISABLED)
        }

        chatDetailList = messagesList
        inputView = input_view

        activity?.let {
            if (!isPrivate) {
                // right now all we do is open up chat, so disable when in PMs
                chatDetailList?.setClickListener(it as ChatDetailList.MessageClickListener)
            }
        }

        chatLoadStartTimestamp = System.currentTimeMillis()
        chatDetailList?.loadInitialMessages(id, this)

        inputView?.isEnabled = !isComposerDisabled
        inputView?.setSubmitListener { input: CharSequence? -> makePost(input, isPrivate, id) }

        val quickCommands = quick_commands
        quickCommands.setClickListener(this)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context !is ChatDetailList.MessageClickListener) {
            // maybe this should check parent fragments as well?
            throw ClassCastException("Activity must implement ChatDetailList.MessageClickListener")
        }
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
