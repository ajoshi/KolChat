package biz.ajoshi.kolchat.chat.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import biz.ajoshi.kolchat.chat.databinding.ChatMessageBinding
import biz.ajoshi.kolchat.persistence.chat.ChatMessage

/**
 * Adapter that uses AndroidX Paging library for paging up and down (so it auto-fetches new messages).
 * This doesn't seem to work super well, when new messages are added to the bottom- it doesn't always scroll down.
 */
class PagingChatAdapter(
    val layoutMgr: androidx.recyclerview.widget.LinearLayoutManager,
    val listener: ChatMessageVH.MessageClickListener
) :
    PagedListAdapter<ChatMessage, ChatMessageVH>(DIFF_CALLBACK) {
    private var started = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageVH {
        val chatMessageBinding =
            ChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatMessageVH(chatMessageBinding, listener)
    }

    override fun onBindViewHolder(holder: ChatMessageVH, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        } else {
            // Null defines a placeholder item - PagedListAdapter automatically
            // invalidates this row when the actual object is loaded from the
            // database.
            //    holder.bind(null)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<ChatMessage>() {
            // Concert details may have changed if reloaded from the database,
            // but ID is fixed.
            override fun areItemsTheSame(
                oldMessage: ChatMessage,
                newMessage: ChatMessage
            ): Boolean =
                oldMessage.id == newMessage.id

            override fun areContentsTheSame(
                oldMessage: ChatMessage,
                newMessage: ChatMessage
            ): Boolean =
                oldMessage == newMessage
        }
    }

    fun scrollToBottomAlways() {
        layoutMgr.scrollToPosition(itemCount - 1)
    }

    fun scrollToBottom() {
        scrollToBottom(8)
    }

    fun scrollToBottom(ifCloserThan: Int) {
        val currentIndex = layoutMgr.findLastCompletelyVisibleItemPosition()
        if (currentIndex + ifCloserThan > itemCount) {
            layoutMgr.scrollToPosition(itemCount - 1)
        }
    }

    /**
     * Scrolls to the bottom ASAP and then only scrolls down if the user has scrolled up beyond the threshold
     */
    fun scrollToBottomOnceAndThenThreshold() {
        if (!started) {
            scrollToBottomAlways()
            started = true
        } else {
            scrollToBottom()
        }
    }

}
