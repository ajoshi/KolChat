package biz.ajoshi.kolchat.chat.list

import android.text.format.DateUtils
import android.view.View
import biz.ajoshi.commonutils.StringUtilities
import biz.ajoshi.kolchat.chat.chatMessageDateFormat
import biz.ajoshi.kolchat.chat.chatMessageTimeFormat
import biz.ajoshi.kolchat.persistence.chat.ChatChannel
import kotlinx.android.synthetic.main.channel_list_item.view.*
import java.util.*

/**
 * Viewholder for an entry in the chat channel list for a public channel
 */
open class ChatChannelVH(itemView: View, val listener: ChannelRowClickListener?) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

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


/**
 * Viewholder for an entry in the chat channel list for a private message
 */
// TODO use composition instead of inheritance?
class ChatUserVH(itemView: View, listener: ChannelRowClickListener?) : ChatChannelVH(itemView = itemView, listener = listener) {
    init {
        // this is a PM, so show the PM icon
        itemView.pm_indicator?.visibility = View.VISIBLE
    }
}
