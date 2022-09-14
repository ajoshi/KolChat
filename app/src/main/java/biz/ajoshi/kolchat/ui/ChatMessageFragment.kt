package biz.ajoshi.kolchat.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import biz.ajoshi.commonutils.Logg
import biz.ajoshi.kolchat.R
import biz.ajoshi.kolchat.chat.ChatMessageViewModel
import biz.ajoshi.kolchat.chat.detail.ChatDetailList
import biz.ajoshi.kolchat.chat.detail.ChatMessageVH
import biz.ajoshi.kolchat.chat.detail.PagingChatAdapter
import biz.ajoshi.kolchat.chat.detail.PagingChatDataObserver
import biz.ajoshi.kolchat.chat.detail.customviews.QuickCommand
import biz.ajoshi.kolchat.chat.detail.customviews.QuickCommandView
import biz.ajoshi.kolchat.databinding.ChatDetailBinding
import biz.ajoshi.kolchat.persistence.chat.ChatMessage
import kotlinx.coroutines.*


/**
 * Fragment displaying a conversation in a channel or with a user. Uses the arch components instead of rxjava
 */
// TODO give new values when fully moving to arch components
class ChatMessageFragment : BaseFragment(), QuickCommandView.CommandClickListener,
    ChatDetailList.ChatMessagesLoaderView {
    private var id = "newbie"
    private var name = "newbie"
    private var isPrivate = false
    private var chatLoadStartTimestamp = 0L
    private var isComposerDisabled = true
    private lateinit var currentUserId: String
    private var shouldUseAndroidxPaging: Boolean = false
    val job = Job()

    var adapter : PagingChatAdapter? = null
    lateinit var binding: ChatDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val args = arguments
        if (args != null) {
            id = args.getString(EXTRA_CHANNEL_ID)!!
            name = args.getString(EXTRA_CHANNEL_NAME)!!
            isPrivate = args.getBoolean((EXTRA_CHANNEL_IS_PRIVATE))
            isComposerDisabled = args.getBoolean(EXTRA_CHANNEL_IS_COMPOSER_DISABLED)
            currentUserId = args.getString(EXTRA_CURRENT_USER_ID)!!
            shouldUseAndroidxPaging = args.getBoolean(EXTRA_USE_ANDROIDX_PAGING, false)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChatDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // only set the label if the destination is a chat list as well
        view?.let {
            if (findNavController(it).currentDestination?.id == R.id.nav_chat_message) {
                findNavController(it).currentDestination?.label = getTitle()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        activity?.let {
            if (!isPrivate) {
                // right now all we do is open up chat, so disable when in PMs
                binding.messagesList.setClickListener(it as ChatDetailList.MessageClickListener)
            }
        }

        chatLoadStartTimestamp = System.currentTimeMillis()
        if (shouldUseAndroidxPaging) {
            // the paginglist needs the parent fragment to get the vm, so it can't load data on its own. So we trigger it here
            loadPagedList()
        } else {
            // paginglist will load its messages on its own
            // TODO if paginglist actually becomes stable and usable, this should be revisited
            binding.messagesList.loadInitialMessages(id, currentUserId, this)
        }

        binding.inputView.isEnabled = !isComposerDisabled
        binding.inputView.setSubmitListener { input: CharSequence? ->
            makePost(
                input,
                isPrivate,
                id
            )
        }

        binding.quickCommands.setClickListener(this)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context !is ChatDetailList.MessageClickListener) {
            // maybe this should check parent fragments as well?
            throw ClassCastException("Activity must implement ChatDetailList.MessageClickListener")
        }
    }

    /**
     * Loads the paged list of chats for this channel. Sets the adapter to the paged adapter which seems buggy
     */
    private fun loadPagedList() {
        val vm: ChatMessageViewModel =
            ViewModelProviders.of(this).get(ChatMessageViewModel::class.java)

        adapter = PagingChatAdapter(
            binding.messagesList.chatAdapter.layoutMgr,
            object : ChatMessageVH.MessageClickListener {
                override fun onMessageLongClicked(message: ChatMessage) {
                    (activity as ChatDetailList.MessageClickListener).onMessageLongClicked(message)
                }
            })

        CoroutineScope(Dispatchers.IO + job).launch {
            vm.getLastChatObservable(id, currentUserId, this)
                .collect {
                    withContext(Dispatchers.Main + job) {
                        adapter?.submitData(lifecycle = lifecycle, pagingData = it)
                    }
                }
        }
        binding.messagesList.adapter = adapter

        // how do we log perf metrics in this? can we?
    }

    override fun onDestroy(){
        job.cancel()
        adapter?.cleanup()
        super.onDestroy()
    }

    /*
     * Listen to new chat commands that are made to this channel and show them. Will also listen for System Announcements
     */
    override fun onInitialMessageListLoaded() {
        val chatLoadEndTimestamp = System.currentTimeMillis()
        val vm: ChatMessageViewModel =
            ViewModelProviders.of(this).get(ChatMessageViewModel::class.java)
        vm.getChatListObservable(id, currentUserId, System.currentTimeMillis())
            ?.observe(this, Observer
            { message ->
                if (message != null)
                //add this new message to the bottom (will scroll down if we're at the bottom of the list)
                    binding.messagesList.addMessages(message)
            })
//        Analytics.getAnswers()?.logContentView(
//            ContentViewEvent()
//                .putContentName("Channel detail opened")
//                .putContentId(if (isPrivate) "PM" else name)
//                .putCustomAttribute(
//                    EVENT_ATTRIBUTE_TIME_TAKEN,
//                    (chatLoadEndTimestamp - chatLoadStartTimestamp)
//                )
//        )
    }

    override fun onCommandClicked(command: QuickCommand) {
        binding.inputView.appendInputText(command.command)
    }

    override fun getTitle(): String {
        return name
    }

    companion object {
        val EXTRA_CHANNEL_ID = "biz.ajoshi.kolchat.ExtraChannelId"
        val EXTRA_CHANNEL_NAME = "biz.ajoshi.kolchat.ExtraChannelName"
        val EXTRA_CURRENT_USER_ID = "biz.ajoshi.kolchat.ExtraCurrentUserId"
        val EXTRA_CHANNEL_IS_PRIVATE = "biz.ajoshi.kolchat.ExtraChannelPrivate"

        // lets the app disable the input view if it wants. doesn't let it disable when the fragment is already up, but nbd
        val EXTRA_CHANNEL_IS_COMPOSER_DISABLED = "biz.ajoshi.kolchat.ExtraChannelIsComposerDisabled"

        // lets the app enable or disable androidx paging. Not fully done
        val EXTRA_USE_ANDROIDX_PAGING = "biz.ajoshi.kolchat.ExtraUseAndroidxPaging"

        /**
         * Creates a bundle for a Chat Message Fragment with the passed in properties.
         */
        fun getBundleForChatMessageFragment(
            currentUserId: String, channelName: String, channelId: String, isPrivate: Boolean,
            isComposerDisabled: Boolean, shouldUseAndroidxPaging: Boolean
        ): Bundle {
            val b = Bundle()
            b.putString(EXTRA_CHANNEL_NAME, channelName)
            b.putString(EXTRA_CHANNEL_ID, channelId)
            b.putString(EXTRA_CURRENT_USER_ID, currentUserId)
            b.putBoolean(EXTRA_CHANNEL_IS_PRIVATE, isPrivate)
            b.putBoolean(EXTRA_CHANNEL_IS_COMPOSER_DISABLED, isComposerDisabled)
            b.putBoolean(EXTRA_USE_ANDROIDX_PAGING, shouldUseAndroidxPaging)
            return b
        }
    }
}
