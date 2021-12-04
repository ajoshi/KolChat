package biz.ajoshi.kolchat.chat.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import biz.ajoshi.kolchat.chat.databinding.ChatMessageBinding
import biz.ajoshi.kolchat.persistence.chat.ChatMessage

/**
 * Adapter for the list of chat messages (inside a given channel)
 */
class ChatAdapter(
    val layoutMgr: androidx.recyclerview.widget.LinearLayoutManager,
    val listener: ChatMessageVH.MessageClickListener,
    val supportsPaging: Boolean = false
) : androidx.recyclerview.widget.RecyclerView.Adapter<ChatMessageVH>() {

    var messages = mutableListOf<ChatMessage>()
    var idList = mutableListOf<Int>()

    private val viewtypeMessage = 1
    private val viewtypeLoadMore = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageVH {
        when (viewType) {
            viewtypeLoadMore -> {
                // TODO replace with more row
                val binding =
                    ChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ChatMessageVH(binding, listener)
            }
            //viewtypeMessage -> // fallthrough
            else -> {
                val binding =
                    ChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ChatMessageVH(binding, listener)
            }
        }
    }

    override fun onBindViewHolder(holder: ChatMessageVH, position: Int) {
        var positionInArray = position
        if (supportsPaging) {
            if (positionInArray == 0) {
                // TODO bind loading row
            }
            // since the 0th row shows the more row, the 1st row will show the 0th item in the backing array
            positionInArray--
        }
        if (messages.size > positionInArray) {
            holder.bind(messages[positionInArray])
        }
    }

    override fun getItemCount(): Int {
        if (supportsPaging) {
            return messages.size + 1
        } else {
            return messages.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (supportsPaging) {
            if (position == 0) {
                return viewtypeLoadMore
            } else {
                return viewtypeMessage
            }
        } else {
            return viewtypeMessage
        }
    }

    /**
     * Sets the backing list to whatever is sent in. Replaces old data.
     * No-op if empty list is sent in
     */
    fun setList(newList: List<ChatMessage>?) {
        newList?.let {
            messages = it.toMutableList()
            notifyDataSetChanged()
            scrollToBottomAlways()
        }
    }

    fun scrollToBottom() {
        // only scroll when user is near the bottom- else you mess up scrolling
        // 6 is arbitrarily close enough to the bottom for me
        scrollToBottom(6)
    }

    /**
     * Scroll to the bottom if we are closer than n elements away from the bottom
     */
    fun scrollToBottom(ifCloserThan: Int) {
        val currentIndex = layoutMgr.findLastCompletelyVisibleItemPosition()
        if (currentIndex + ifCloserThan > messages.size) {
            layoutMgr.scrollToPosition(messages.size - 1)
        }
    }

    /**
     * Unconditionally scroll to the bottom. Does not check to see where the user is before scrolling down and might be
     * disruptive to the user
     */
    fun scrollToBottomAlways() {
        layoutMgr.scrollToPosition(messages.size - 1)
    }

    /**
     * Add this single message to the bottom of the chat list (newest message)
     */
    fun addToBottom(newMessge: ChatMessage) {
        if (!idList.contains(newMessge.id)) {
            messages.add(newMessge)
            idList.add(newMessge.id)
            notifyItemInserted(messages.size - 1)
            scrollToBottom()
        }
    }

    /**
     * Appends a list of messages to the bottom of the chat list (new messages)
     */
    fun addToBottom(newMessges: List<ChatMessage>) {
        var insertedCount = 0
        for (message in newMessges) {
            if (!idList.contains(message.id)) {
                messages.add(message)
                idList.add(message.id)
                insertedCount++
            }
        }
        notifyItemRangeInserted(messages.size - insertedCount, insertedCount)
        scrollToBottom()
    }

    /**
     * Appends a list of messages to the bottom of the chat list (new messages)
     */
    fun addToTop(newMessges: List<ChatMessage>) {
        var insertedCount = 0
        for (message in newMessges) {
            if (!idList.contains(message.id)) {
                messages.add(0, message)
                idList.add(0, message.id)
                insertedCount++
            }
        }
        notifyItemRangeInserted(0, insertedCount)
        scrollToBottom()
    }
}


