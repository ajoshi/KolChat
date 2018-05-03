package biz.ajoshi.kolchat.view

import android.support.v7.widget.RecyclerView
import android.view.View
import biz.ajoshi.commonutils.StringUtilities
import biz.ajoshi.kolchat.persistence.ChatChannel
import kotlinx.android.synthetic.main.channel_list_item.view.*
import java.util.*

/**
 * Viewholder for an entry in the chat channel list
 */

class ChatChannelVH(itemView: View, val listener: ChannelRowClickListener?) : RecyclerView.ViewHolder(itemView) {

    interface ChannelRowClickListener {
        fun onChannelRowClicked(channel: ChatChannel)
    }

    var channelForThisRow: ChatChannel? = null

    // TODO this might be doing a findviewbyid each time and not caching. confirm.
    // Confirmed- this is ineffecient. Irrelevant for a small row like this, but potential issue later on
    fun bind(channel: ChatChannel) {
        val spannable = StringUtilities.getHtml(channel.name)
        itemView.name.text = spannable
        itemView.last_message_time.text = timeFormat.format(Date(channel.lastMessageTime))
        // todo use userid for right click options at some point
        channelForThisRow = channel

        // TODO we should set the click listener once, and not each time
        itemView.setOnClickListener{ _ -> listener?.onChannelRowClicked(channelForThisRow!!)}
    }
}
