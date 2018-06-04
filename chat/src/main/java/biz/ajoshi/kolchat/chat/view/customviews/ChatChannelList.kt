package biz.ajoshi.kolchat.chat.view.customviews

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.helper.ItemTouchHelper.LEFT
import android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback
import android.util.AttributeSet
import biz.ajoshi.kolchat.chat.ChatSingleton
import biz.ajoshi.kolchat.chat.view.ChatChannelAdapter
import biz.ajoshi.kolchat.chat.view.ChatChannelAdapter.ChannelClickListener
import biz.ajoshi.kolchat.persistence.KolDB
import biz.ajoshi.kolchat.persistence.chat.ChatChannel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Shows and updates the list of chat channels with messages. Updates automatically since it uses RxJava
 */
class ChatChannelList : RecyclerView, ChannelClickListener {
    var chatChannelAdapter: ChatChannelAdapter? = null
    var groupChatUpdateSubscriber: Disposable? = null
    var pmChatUpdateSubscriber: Disposable? = null
    var interactionListener: ChatChannelInteractionListener? = null

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
        chatChannelAdapter = ChatChannelAdapter()
        adapter = chatChannelAdapter
        layoutManager = layoutMgr

        groupChatUpdateSubscriber = KolDB.getDb()
                ?.ChannelDao()
                // TODO pass in the username instead of directly accessing from the singleton
                ?.getAllChatChannels(ChatSingleton.network?.currentUser?.player?.name)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { channels ->
                    chatChannelAdapter!!.setGroupList(channels)
                }
        pmChatUpdateSubscriber = KolDB.getDb()
                ?.ChannelDao()
                // TODO pass in the username instead of directly accessing from the singleton
                ?.getAllPMChannels(ChatSingleton.network?.currentUser?.player?.name)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { channels ->
                    chatChannelAdapter!!.setPmsList(channels)
                }
        ItemTouchHelper(ChannelSwipeCallback()).attachToRecyclerView(this)
        chatChannelAdapter?.setOnClickListener(this)
    }

    /**
     * We want people to delete channels by swiping on them. We do not want them to reorder them yet
     */
    inner class ChannelSwipeCallback() : SimpleCallback(0, LEFT) {
        override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
            val position = viewHolder?.adapterPosition
            position?.let {
                val channel = chatChannelAdapter?.getChannelAt(position)
                channel?.let {
                    interactionListener?.onChannelSwiped(channel = channel)
                }
                // We reset the swipe state so the user doesn't see a white space in here if they don't delete
                // if they delete, then this goes away anyway
                chatChannelAdapter?.notifyItemChanged(it)
            }
        }
    }

    /**
     * Sets the click and swipe listener for each element in the list of channels
     */
    fun setChatchannelInteractionListener(channelInteractionListener: ChatChannelInteractionListener) {
        interactionListener = channelInteractionListener
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
