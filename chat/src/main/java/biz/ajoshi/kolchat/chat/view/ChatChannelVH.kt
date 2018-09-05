package biz.ajoshi.kolchat.chat.view

import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.View
import biz.ajoshi.commonutils.StringUtilities
import biz.ajoshi.kolchat.persistence.chat.ChatChannel
import kotlinx.android.synthetic.main.channel_list_item.view.*
import java.util.*

/**
 * Viewholder for an entry in the chat channel list
 */
class ChatChannelVH(itemView: View, val listener: ChannelRowClickListener?) : RecyclerView.ViewHolder(itemView) {

    /**
     * Called when a channel is tapped on. Implementor gets the channel data and can do whatever it wants
     */
    interface ChannelRowClickListener {
        fun onChannelRowClicked(channel: ChatChannel)
    }

    private val date = Date()

    private lateinit var channelForThisRow: ChatChannel

    private val nameView = itemView.name
    private val dateView = itemView.last_message_time

    init {
        itemView.setOnClickListener { _ -> listener?.onChannelRowClicked(channelForThisRow) }
    }

    fun bind(channel: ChatChannel) {
        val spannable = StringUtilities.getHtml(channel.name)
        nameView.text = spannable
        date.time = channel.lastMessageTime
        dateView.text = if (DateUtils.isToday(channel.lastMessageTime)) {
            // if this is today then no need to show date
            chatMessageTimeFormat.format(date)
        } else {
            // this isn't from today, so show only the date
            chatMessageDateFormat.format(date)
        }
        channelForThisRow = channel
    }
}
