package biz.ajoshi.kolchat.view

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import biz.ajoshi.kolchat.R
import biz.ajoshi.kolchat.persistence.ChatMessage

/**
 * Adapter for the list of chat messages (inside a given channel)
 */
class ChatAdapter(val layoutMgr: LinearLayoutManager) : RecyclerView.Adapter<ChatMessageVH>() {
    var messages = mutableListOf<ChatMessage>()
    var idList = mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_message, parent, false)
        return ChatMessageVH(view)
    }

    override fun onBindViewHolder(holder: ChatMessageVH, position: Int) {
        if (messages.size > position) {
            holder.bind(messages[position])
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    /**
     * Sets the backing list to whatever is sent in. Replaces old data.
     * No-op if empty list is sent in
     */
    fun setList(newList: List<ChatMessage>?) {
        newList?.let {
            messages = newList.toMutableList()
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
}


