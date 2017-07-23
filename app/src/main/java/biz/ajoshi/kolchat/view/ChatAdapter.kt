package biz.ajoshi.kolchat.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ListAdapter
import biz.ajoshi.kolchat.R
import biz.ajoshi.kolchat.persistence.ChatMessage

/**
 * Created by ajoshi on 7/22/17.
 */
class ChatAdapter : RecyclerView.Adapter<ChatMessageVH>() {
    var messages = mutableListOf<ChatMessage>()
    var idList = mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ChatMessageVH {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.chat_message, parent, false)
        return ChatMessageVH(view)
    }

    override fun onBindViewHolder(holder: ChatMessageVH?, position: Int) {
        if (messages.size > position) {
            holder?.bind(messages.get(position))
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun setList(newList : List<ChatMessage>) {
        messages = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun addToBottom(newMessge: ChatMessage) {
        if (!idList.contains(newMessge.id)) {
            messages.add(newMessge)
            idList.add(newMessge.id)
            notifyItemInserted(messages.size - 1)
        }
        // todo scroll to bottom?
    }
}


