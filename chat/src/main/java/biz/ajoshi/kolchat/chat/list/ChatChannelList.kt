package biz.ajoshi.kolchat.chat.list

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import biz.ajoshi.kolchat.chat.list.ChatChannelAdapter.ChannelClickListener
import biz.ajoshi.kolchat.persistence.KolDB
import biz.ajoshi.kolchat.persistence.chat.ChatChannel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Shows and updates the list of chat channels with messages. Updates automatically since it uses RxJava
 */
class ChatChannelList : androidx.recyclerview.widget.RecyclerView, ChannelClickListener {
    private lateinit var chatChannelAdapter: ChatChannelAdapter
    private var groupChatUpdateSubscriber: Disposable? = null
    private var pmChatUpdateSubscriber: Disposable? = null
    private var interactionListener: ChatChannelInteractionListener? = null
    private lateinit var userId: String

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initializeViews(context)
    }

    /**
     * Set up the view and everything that can be set up without being configured
     */
    private fun initializeViews(context: Context) {
        val layoutMgr = androidx.recyclerview.widget.LinearLayoutManager(context)
        chatChannelAdapter = ChatChannelAdapter()
        adapter = chatChannelAdapter
        layoutManager = layoutMgr
        ItemTouchHelper(ChannelSwipeCallback()).attachToRecyclerView(this)
        chatChannelAdapter.setOnClickListener(this)
    }

    /**
     * We want people to delete channels by swiping on them. We do not want them to reorder them yet
     */
    inner class ChannelSwipeCallback() : SimpleCallback(0, LEFT) {
        override fun onMove(
            recyclerView: androidx.recyclerview.widget.RecyclerView,
            viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
            target: androidx.recyclerview.widget.RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(
            viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
            direction: Int
        ) {
            val position = viewHolder.adapterPosition
            val channel = chatChannelAdapter.getChannelAt(position)
            interactionListener?.onChannelSwiped(channel = channel)
            // We reset the swipe state so the user doesn't see a white space in here if they don't delete
            // if they delete, then this goes away anyway
            chatChannelAdapter.notifyItemChanged(position)

        }
    }

    /**
     * Sets the click and swipe listener for each element in the list of channels
     */
    fun setChatchannelInteractionListener(channelInteractionListener: ChatChannelInteractionListener) {
        interactionListener = channelInteractionListener
    }

    /**
     * Sets the click and swipe listener for each element in the list of channels
     */
    fun setUserId(userId: String) {
        this.userId = userId
        groupChatUpdateSubscriber = KolDB.getDb()
            ?.ChannelDao()
            // TODO pass in the username instead of directly accessing from the singleton
            ?.getAllChatChannels(userId)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { channels ->
                chatChannelAdapter.setGroupList(channels)
            }
        pmChatUpdateSubscriber = KolDB.getDb()
            ?.ChannelDao()
            // TODO pass in the username instead of directly accessing from the singleton
            ?.getAllPMChannels(userId)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { channels ->
                chatChannelAdapter.setPmsList(channels)
            }
    }

    override fun onChannelClicked(channel: ChatChannel) {
        interactionListener?.onChannelClicked(channel = channel)
    }

    /**

     * Must be called by the Activity/Fragment when it is being destroyed so this can dispose of its subscribers
     */
    fun onDestroy() {
        pmChatUpdateSubscriber?.takeIf { it.isDisposed }?.dispose()
        groupChatUpdateSubscriber?.takeIf { it.isDisposed }?.dispose()
    }

    interface ChatChannelInteractionListener {
        /**
         * Called when a Channel name has been tapped on by the user
         *
         * @param channel ChatChannel object describing the channel that was opened
         */
        fun onChannelClicked(channel: ChatChannel)

        /**
         * Called when a user swipes left on a channel in order to remove it
         *
         * @param channel ChatChannel object describing the channel that was opened
         */
        fun onChannelSwiped(channel: ChatChannel)
    }
}
