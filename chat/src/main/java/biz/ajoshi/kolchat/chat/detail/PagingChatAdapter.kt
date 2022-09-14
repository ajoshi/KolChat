package biz.ajoshi.kolchat.chat.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import biz.ajoshi.kolchat.chat.databinding.ChatMessageBinding
import biz.ajoshi.kolchat.persistence.chat.ChatMessage


/**
 * Adapter that uses AndroidX Paging library for paging up and down (so it auto-fetches new messages).
 * This doesn't seem to work super well, when new messages are added to the bottom- it doesn't always scroll down.
 * [PagingChatDataObserver] attempts to resolve this
 */
class PagingChatAdapter(
    val layoutMgr: androidx.recyclerview.widget.LinearLayoutManager,
    val listener: ChatMessageVH.MessageClickListener
) :
    PagingDataAdapter<ChatMessage, ChatMessageVH>(DIFF_CALLBACK) {
    private var started = false
    private val observer = PagingChatDataObserverImpl(this)

    init {
        registerAdapterDataObserver(observer)
    }

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
            /// as far as I can tell, this is what is shown when no data is loaded.
            // ie. Paging placeholder default behavior is an empty row

            // Null defines a placeholder item - PagedListAdapter automatically
            // invalidates this row when the actual object is loaded from the
            // database.
            //    holder.bind(null)
        }
    }

    private fun scrollToBottomAlways() {
        // actually scrolls to top implementation wise, but the UX is still "scroll to bottom"
        layoutMgr.scrollToPosition(0)
    }

    private fun scrollToBottom() {
        scrollToBottom(8)
    }

    fun scrollToBottom(ifCloserThan: Int) {
        val currentIndex = layoutMgr.findFirstCompletelyVisibleItemPosition()
        if (currentIndex < ifCloserThan) {
            scrollToBottomAlways()
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

    fun cleanup() {
        unregisterAdapterDataObserver(observer)
    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<ChatMessage>() {
            // Chat message details may have changed if reloaded from the database,
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
                oldMessage.text == newMessage.text
                        &&
                        oldMessage.channelId == newMessage.channelId
                        &&
                        oldMessage.userId == newMessage.userId
                        &&
                        oldMessage.timeStamp == newMessage.timeStamp
        }
    }
}

internal class PagingChatDataObserverImpl(adapter: PagingChatAdapter?) :
    PagingChatDataObserver(adapter) {
    override fun doOnChange() {
        adapter?.scrollToBottomOnceAndThenThreshold()
    }
}