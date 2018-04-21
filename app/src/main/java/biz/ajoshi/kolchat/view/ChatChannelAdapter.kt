package biz.ajoshi.kolchat.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import biz.ajoshi.kolchat.R
import biz.ajoshi.kolchat.persistence.ChatChannel

/**
 * Created by a.joshi on 10/7/17.
 */
class ChatChannelAdapter() : RecyclerView.Adapter<ChatChannelVH>() {

    /**
     * Called when a Channel name has been tapped on by the user
     */
    interface ChannelClickListener {
        fun onChannelClicked(channel: ChatChannel)
    }

    var channels = mutableListOf<ChatChannel>()
    var clickListener: ChannelClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatChannelVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.channel_list_item, parent, false)
        return ChatChannelVH(view, object : ChatChannelVH.ChannelRowClickListener {
            override fun OnChannelRowClicked(channel: ChatChannel) {
                clickListener?.onChannelClicked(channel)
            }
        })
    }


    override fun onBindViewHolder(holder: ChatChannelVH, position: Int) {
        if (channels.size > position) {
            holder.bind(channels.get(position))
        }
    }

    override fun getItemCount(): Int {
        return channels.size
    }

    fun setList(newList: List<ChatChannel>) {
        channels = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: ChannelClickListener) {
        clickListener = listener
    }


}
