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

    interface ChannelRowClickListener {
        fun onChannelRowClicked(channel: ChatChannel)
    }

    val date = Date()

    var channelForThisRow: ChatChannel? = null

    // TODO this might be doing a findviewbyid each time and not caching. confirm.
    // Confirmed- this is ineffecient. Irrelevant for a small row like this, but potential issue later on
    fun bind(channel: ChatChannel) {
        val spannable = StringUtilities.getHtml(channel.name)
        itemView.name.text = spannable
        date.time = channel.lastMessageTime
        itemView.last_message_time.text = if (DateUtils.isToday(channel.lastMessageTime)) {
            // if this is today then no need to show date
            chatMessageTimeFormat.format(date)
        } else {
            // this isn't from today, so show only the date
            chatMessageDateFormat.format(date)
        }
        //itemView.last_message_time.text = chatMessageTimeFormat.format(Date(channel.lastMessageTime))
        // todo use userid for right click options at some point
        channelForThisRow = channel

        // TODO we should set the click listener once, and not each time
        itemView.setOnClickListener { _ -> listener?.onChannelRowClicked(channelForThisRow!!) }
    }
}
