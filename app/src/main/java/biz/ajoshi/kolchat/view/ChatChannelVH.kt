package biz.ajoshi.kolchat.view

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import biz.ajoshi.kolchat.persistence.ChatChannel
import kotlinx.android.synthetic.main.channel_list_item.view.*
import java.util.*

/**
 * Created by a.joshi on 10/7/17.
 */

class ChatChannelVH(itemView: View, val listener: ChannelRowClickListener?) : RecyclerView.ViewHolder(itemView) {

    interface ChannelRowClickListener {
        fun OnChannelRowClicked(channel: ChatChannel)
    }

    var channelForThisRow: ChatChannel? = null

    // TODO this might be doing a findviewbyid each time and not caching. confirm.
    fun bind(channel: ChatChannel) {
        val spannable = Html.fromHtml(channel.name)
        itemView.name.setText(spannable)
        itemView.last_message_time.text = timeFormat.format(Date(channel.lastMessageTime))
        // todo use userid for right click options at some point
        channelForThisRow = channel

        itemView.setOnClickListener{ view: View -> listener?.OnChannelRowClicked(channelForThisRow!!)}
    }
}
