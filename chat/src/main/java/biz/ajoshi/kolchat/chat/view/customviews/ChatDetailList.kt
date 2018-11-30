package biz.ajoshi.kolchat.chat.view.customviews

import android.arch.persistence.room.EmptyResultSetException
import android.content.Context
import android.os.AsyncTask
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
    private var lastTimeSeen: Long? = 0
    private lateinit var channelId: String

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
     * @param currentUserId id of the currently logged in user (different users see different chats)
     * @param listener listener to be called when initial chat has been loaded
     */
    fun loadInitialMessages(channelId: String, currentUserId: String, listener: ChatMessagesLoaderView) {
        this.channelId = channelId
        initialChatLoadSubscriber = Observable.fromCallable {
            KolDB.getDb()
                    ?.ChannelDao()
                    ?.getChannel(channelId, currentUserId)
            // get the channel object for this channel
        }?.subscribeOn(Schedulers.io())
                // get the messages for this channel
                ?.map { singleChannel ->
                    // Or should I be using a normal (non-Single) Channel object?
                    try {
                        val channel = singleChannel.blockingGet()
                        lastTimeSeen = channel.lastTimeUserViewedChannel
                    } catch (e: EmptyResultSetException) {
                        // we've never chatted in this room/with this person before. Hardly an issue
                    }
                    getMessagesForChannel(channelId, userId = currentUserId)
                }

                ?.observeOn(Schedulers.computation())
                ?.map { list -> makeFakeRow(list) }  // add the 'new messages row' in the correct place


                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { list ->
                    // we have the list, so set it as the displayed list
                    chatAdapter.setList(list)
                    listener.onInitialMessageListLoaded()
                    initialMessagesLoaded()
                }
    }

    /**
     * TODO Make this do something better where it's not just appending text
     *
     * Changes the list of chat messages to indicate what the last seen message was
     */
    private fun makeFakeRow(list: List<ChatMessage>?): List<ChatMessage>? {
        list?.let {
            if (it.isEmpty()) return list // no point showing the row if the chat is empty
            val indexOfRow = findPlaceFor(lastTimeSeen!!, it)
            if (indexOfRow == -1 || indexOfRow == it.size - 1) return list
            val oldText = it[indexOfRow].text
            it[indexOfRow].text = oldText + "<br><br><font color=\"red\">New messages!</font>"
        }
        return list
    }

    /**
     * Finds a chat message that was made after the passed in timestamp.
     * This lets us know where to show a 'new messages' message after the last read message.
     */
    private fun findPlaceFor(timestamp: Long, list: List<ChatMessage>): Int {
        return list.indexOfLast { it.localtimeStamp < timestamp }
    }

    /**
     * Make a DB query to fetch stored messages for a given chat channel
     */
    private fun getMessagesForChannel(channelId: String, userId: String): List<ChatMessage>? {
        return KolDB.getDb()
                ?.MessageDao()
                ?.getMessagesForChannel(channelId, userId)
    }

    /**
     * Do whatever needs to be done after the inital list is loaded
     */
    private fun initialMessagesLoaded() {
        // get rid of this observer now that we've loaded the initial list
        initialChatLoadSubscriber?.dispose()
        UpdateChatTimeTask(System.currentTimeMillis()).execute(channelId)
    }

    /**
     * Add new messages to the list. Messages are added to the bottom.
     */
    fun addMessages(newMessges: List<ChatMessage>) {
        chatAdapter.addToBottom(newMessges)
        UpdateChatTimeTask(System.currentTimeMillis()).execute(channelId)
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

class UpdateChatTimeTask(val time: Long) : AsyncTask<String, Unit, Unit>() {
    override fun doInBackground(vararg params: String?): Unit {
        params[0]?.let {
            KolDB.getDb()
                    ?.ChannelDao()
                    ?.setChannelLastTime(it, time)
        }
    }

}
