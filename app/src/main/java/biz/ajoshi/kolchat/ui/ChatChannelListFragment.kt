package biz.ajoshi.kolchat.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_LONG
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.ajoshi.commonutils.StringUtilities
import biz.ajoshi.kolchat.R
import biz.ajoshi.kolchat.chat.ACTION_CHAT_COMMAND_FAILED
import biz.ajoshi.kolchat.chat.EXTRA_FAILED_CHAT_MESSAGE
import biz.ajoshi.kolchat.chat.view.customviews.ChatChannelList
import biz.ajoshi.kolchat.chat.view.customviews.NewChatFAB
import kotlinx.android.synthetic.main.channel_list.*


/**
 * Shows a list of all active chats/groups
 * Created by ajoshi on 7/4/2017.
 */
class ChatChannelListFragment : BaseFragment(), NewChatFAB.ChatMessageSender {
    override fun sendChatMessage(message: CharSequence?, isPrivate: Boolean, id: String) {
        makePost(post = message, isPrivate = isPrivate, id = id)
    }

    // local broadcast receiver that will show a snackbar when a chat command fails.
    // Needed because the fab allows no place for this error message to be shown
    private val failedMessageReceiver = FailedMessageReceiver()
    // list of the channels
    var channelList: ChatChannelList? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.channel_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val newMessageFab = button_compose_new_chat
        newMessageFab?.chatMessageSender = this
        channelList = channel_list
        // right now just send it all to the activity. We might want to intercept it later on, but probably not
        channelList?.setChatchannelInteractionListener(activity as ChatChannelList.ChatChannelInteractionListener)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onDestroy() {
        channelList?.onDestroy()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        failedMessageReceiver.register(context)
    }

    override fun onPause() {
        super.onPause()
        failedMessageReceiver.unregister(context)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context !is ChatChannelList.ChatChannelInteractionListener) {
            // maybe this should check parent fragments as well?
            throw ClassCastException("Activity must implement ChatChannelList.ChatChannelInteractionListener")
        }
    }

    /**
     * Broadcast receiver that listens for the ACTION_CHAT_COMMAND_FAILED action. If moving this out of ChatChannel, we
     * need to refactor to send in some sort of view provider
     */
    inner class FailedMessageReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val message = intent.getStringExtra(EXTRA_FAILED_CHAT_MESSAGE)
            if (!message.isEmpty()) {
                view?.let {
                    Snackbar.make(it, StringUtilities.getHtml(message), LENGTH_LONG).show()
                }
            }
        }

        /**
         * Unregister the listener
         */
        fun unregister(context: Context?) {
            context?.let {
                LocalBroadcastManager.getInstance(it).unregisterReceiver(
                        this);
            }
        }

        /**
         * Register the listener for the local broadcast
         */
        fun register(context: Context?) {
            context?.let {
                LocalBroadcastManager.getInstance(it).registerReceiver(
                        this, IntentFilter(ACTION_CHAT_COMMAND_FAILED));
            }
        }
    }

}

