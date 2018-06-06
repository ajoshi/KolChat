package biz.ajoshi.kolchat.chat.view.customviews

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import biz.ajoshi.kolchat.chat.view.ChatAdapter
import biz.ajoshi.kolchat.chat.view.ChatMessageVH
import biz.ajoshi.kolchat.persistence.KolDB
import biz.ajoshi.kolchat.persistence.chat.ChatMessage
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Recyclerview that loads all the chat messages for a chat channel/PM. Does not update to show new data by itself.
 * Apps using LiveData should handle this themselves
 */
class ChatDetailList : RecyclerView {
    private lateinit var chatAdapter: ChatAdapter
    private var initialChatLoadSubscriber: Disposable? = null
    private var clickListener: MessageClickListener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initializeViews(context)
    }

    /**
     * Set up the view and everything that can be set up without being configured
     */
    private fun initializeViews(context: Context) {
        val layoutMgr = LinearLayoutManager(context)
        chatAdapter = ChatAdapter(layoutMgr, object : ChatMessageVH.MessageClickListener {
            override fun onMessageLongClicked(message: ChatMessage) {
                clickListener?.onMessageLongClicked(message = message)
            }
        })
        adapter = chatAdapter
        layoutManager = layoutMgr
        // scroll when the keyboard comes up
        addOnLayoutChangeListener({ _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                post({ chatAdapter.scrollToBottom(16) })
            }
        })
    }

    /**
     * Loads the initial chat list for the channel/PM list with the given id
     * @param channelId id of the user or name of the channel
     * @param listener listener to be called when initial chat has been loaded
     */
    fun loadInitialMessages(channelId: String, listener: ChatMessagesLoaderView) {
        initialChatLoadSubscriber = Observable.fromCallable {
            KolDB.getDb()
                    ?.MessageDao()
                    ?.getMessagesForChannel(channelId)
            // get n commands for this channel (n is a limit we set in the data source (100 right now))
        }?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { list ->
                    // we have the list, so set it as the displayed list
                    chatAdapter.setList(list)
                    listener.onInitialMessageListLoaded()
                    initialMessagesLoaded()
                }
    }

    /**
     * Do whatever needs to be done after the inital list is loaded
     */
    private fun initialMessagesLoaded() {
        // get rid of this observer now that we've loaded the initial list
        initialChatLoadSubscriber?.dispose()
    }

    /**
     * Add new messages to the list. Messages are added to the bottom.
     */
    fun addMessages(newMessges: List<ChatMessage>) {
        chatAdapter.addToBottom(newMessges)
    }

    /**
     * Sets the clicklistener for the chat detail list. Tapping/Longpressing a chat post will trigger this
     */
    fun setClickListener(listener: MessageClickListener) {
        clickListener = listener
    }

    /**
     * A View (as in MVP) like a Fragment/Activity that actually displays this list
     */
    interface ChatMessagesLoaderView {
        fun onInitialMessageListLoaded()
    }

    /**
     * Used when a chat message has been clicked/long pressed
     */
    interface MessageClickListener {
        fun onMessageLongClicked(message: ChatMessage)
    }
}
