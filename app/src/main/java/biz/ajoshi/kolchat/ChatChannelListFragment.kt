package biz.ajoshi.kolchat

import android.content.Context
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
import biz.ajoshi.kolchat.chat.view.ChatChannelList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Shows a list of all active chats/groups
 * Created by ajoshi on 7/4/2017.
 */
class ChatChannelListFragment : BaseFragment() {
    var channelList: ChatChannelList? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.channel_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        channelList = activity?.findViewById(R.id.channel_list) as ChatChannelList
        // right now just send it all to the activity. We might want to intercept it later on, but probably not
        channelList?.setChatChannelClickListener(activity as ChatChannelAdapter.ChannelClickListener)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onDestroy() {
        channelList?.onDestroy()
        super.onDestroy()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if(context !is ChatChannelAdapter.ChannelClickListener) {
            throw ClassCastException("Activity must implement ChatChannelAdapter.ChannelClickListener")
        }
    }
}
