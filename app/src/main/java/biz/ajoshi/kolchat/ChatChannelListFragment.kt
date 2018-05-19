package biz.ajoshi.kolchat

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.ajoshi.kolchat.persistence.chat.ChatChannel
import biz.ajoshi.kolchat.persistence.KolDB
import biz.ajoshi.kolchat.chat.view.ChatChannelAdapter
import biz.ajoshi.kolchat.chat.ChatSingleton
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Shows a list of all active chats/groups
 * Created by ajoshi on 7/4/2017.
 */
class ChatChannelListFragment : BaseFragment() { // empty constructor

    var chatChannelAdapter: ChatChannelAdapter? = null
    var groupChatUpdateSubscriber: Disposable? = null
    var pmChatUpdateSubscriber: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.channel_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val channelList = activity?.findViewById<RecyclerView>(R.id.channel_list) as RecyclerView
        val layoutMgr = LinearLayoutManager(activity)
        chatChannelAdapter = ChatChannelAdapter()
        channelList.adapter = chatChannelAdapter
        channelList.layoutManager = layoutMgr

        chatChannelAdapter!!.setOnClickListener(object : ChatChannelAdapter.ChannelClickListener {
            override fun onChannelClicked(channel: ChatChannel) {
                // TODO do soemthing better. Eventbus? have activity implement an interface?
                val mainActivity = activity as MainActivity
                mainActivity.onChannelNameClicked(channel)
            }
        })
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
        super.onActivityCreated(savedInstanceState)
    }

    override fun onDestroy() {
        pmChatUpdateSubscriber?.takeIf { it.isDisposed }?.dispose()
        groupChatUpdateSubscriber?.takeIf { it.isDisposed }?.dispose()
        super.onDestroy()
    }
}
