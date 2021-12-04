package biz.ajoshi.kolchat.chat.list

import android.text.format.DateUtils
import android.view.View
import biz.ajoshi.commonutils.StringUtilities
import biz.ajoshi.kolchat.chat.chatMessageDateFormat
import biz.ajoshi.kolchat.chat.chatMessageTimeFormat
import biz.ajoshi.kolchat.chat.databinding.ChannelListItemBinding
import biz.ajoshi.kolchat.persistence.chat.ChatChannel
import java.util.*

/**
 * Viewholder for an entry in the chat channel list for a public channel
 */
open class ChatChannelVH(binding: ChannelListItemBinding, val listener: ChannelRowClickListener?) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

    /**
     * Called when a channel is tapped on. Implementor gets the channel data and can do whatever it wants
     */
    interface ChannelRowClickListener {
        fun onChannelRowClicked(channel: ChatChannel)
    }

    private val date = Date()

    private lateinit var channelForThisRow: ChatChannel

    private val nameView = binding.name
    private val dateView = binding.lastMessageTime

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
class ChatUserVH(binding: ChannelListItemBinding, listener: ChannelRowClickListener?) : ChatChannelVH(binding = binding, listener = listener) {
    init {
        // this is a PM, so show the PM icon
        binding.pmIndicator.visibility = View.VISIBLE
    }
}
