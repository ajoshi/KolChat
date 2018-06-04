package biz.ajoshi.kolchat.chat.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import biz.ajoshi.kolchat.chat.R
import biz.ajoshi.kolchat.persistence.chat.ChatChannel

/**
 * Created by a.joshi on 10/7/17.
 */
class ChatChannelAdapter : RecyclerView.Adapter<ChatChannelVH>() {

    val rowTypeGroup = 1
    val rowTypePm = 2

    /**
     * Defines the different callbacks for click/long press and other touch interactions for a chat channel
     */
    interface ChannelClickListener {
        /**
         * Called when a Channel name has been tapped on by the user
         *
         * @param channel ChatChannel object describing the channel that was opened
         */
        fun onChannelClicked(channel: ChatChannel)
    }

    private var groups = listOf<ChatChannel>()
    private var pms = listOf<ChatChannel>()
    private var clickListener: ChannelClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatChannelVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.channel_list_item, parent, false)
        return ChatChannelVH(view, object : ChatChannelVH.ChannelRowClickListener {
            override fun onChannelRowClicked(channel: ChatChannel) {
                clickListener?.onChannelClicked(channel)
            }
        })
    }

    // We want Groups and PMs to have slightly different UI so distinguishing them is easier
    override fun getItemViewType(position: Int): Int {
        return if (isGroup(position)) rowTypeGroup else rowTypePm
    }

    override fun onBindViewHolder(holder: ChatChannelVH, position: Int) {
        holder.bind(getChannelAt(position))
    }

    override fun getItemCount(): Int {
        return groups.size + pms.count()
    }

    fun setGroupList(newList: List<ChatChannel>) {
        groups = newList
        notifyDataSetChanged()
    }

    fun setPmsList(newList: List<ChatChannel>) {
        pms = newList
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: ChannelClickListener) {
        clickListener = listener
    }

    fun getChannelAt(index: Int): ChatChannel {
        // pms come first, then groups
        if (index < pms.size) {
            return pms[index]
        }
        return groups[index - pms.size]
    }

    fun isGroup(position: Int): Boolean {
        // pms come first, then groups
        return position >= pms.size
    }
}
