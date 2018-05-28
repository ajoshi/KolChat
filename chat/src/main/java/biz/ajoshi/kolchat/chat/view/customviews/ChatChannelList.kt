package biz.ajoshi.kolchat.chat.view.customviews

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import biz.ajoshi.kolchat.chat.ChatSingleton
import biz.ajoshi.kolchat.chat.view.ChatChannelAdapter
import biz.ajoshi.kolchat.persistence.KolDB
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Shows and updates the list of chat channels with messages. Updates automatically since it uses RxJava
 */
class ChatChannelList : RecyclerView {
    var chatChannelAdapter: ChatChannelAdapter? = null
    var groupChatUpdateSubscriber: Disposable? = null
    var pmChatUpdateSubscriber: Disposable? = null


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
    }

    /**
     * Sets the click listener for each element in the list of channels
     */
    fun setChatChannelClickListener(channelClickListener: ChatChannelAdapter.ChannelClickListener) {
        chatChannelAdapter?.clickListener = channelClickListener
    }

    /**
     * Must be called by the Activity/Fragment when it is being destroyed so this can dispose of its subscribers
     */
    fun onDestroy() {
        pmChatUpdateSubscriber?.takeIf { it.isDisposed }?.dispose()
        groupChatUpdateSubscriber?.takeIf { it.isDisposed }?.dispose()
    }
}
