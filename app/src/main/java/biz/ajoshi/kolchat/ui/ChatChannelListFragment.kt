package biz.ajoshi.kolchat.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.ajoshi.commonutils.StringUtilities
import biz.ajoshi.kolchat.chat.ACTION_CHAT_COMMAND_FAILED
import biz.ajoshi.kolchat.chat.ChatSingleton
import biz.ajoshi.kolchat.chat.EXTRA_FAILED_CHAT_MESSAGE
import biz.ajoshi.kolchat.chat.detail.customviews.NewChatFAB
import biz.ajoshi.kolchat.chat.list.ChatChannelList
import biz.ajoshi.kolchat.databinding.ChannelListBinding
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG

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

    lateinit var binding: ChannelListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChannelListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        binding.buttonComposeNewChat.chatMessageSender = this
        // TODO set this in args somehow (this is currently created by nav fragment so I can't set args dynamically)
        binding.channelList.setUserId(ChatSingleton.network?.currentUser?.player?.id!!)
        // right now just send it all to the activity. We might want to intercept it later on, but probably not
        binding.channelList.setChatchannelInteractionListener(activity as ChatChannelList.ChatChannelInteractionListener)
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

    override fun onAttach(context: Context) {
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
            if (message?.isNotEmpty() == true) {
                view?.let {
                    com.google.android.material.snackbar.Snackbar.make(
                        it,
                        StringUtilities.getHtml(message),
                        LENGTH_LONG
                    ).show()
                }
            }
        }

        /**
         * Unregister the listener
         */
        fun unregister(context: Context?) {
            context?.let {
                androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(it)
                    .unregisterReceiver(
                        this
                    );
            }
        }

        /**
         * Register the listener for the local broadcast
         */
        fun register(context: Context?) {
            context?.let {
                androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(it)
                    .registerReceiver(
                        this, IntentFilter(ACTION_CHAT_COMMAND_FAILED)
                    );
            }
        }
    }
}

