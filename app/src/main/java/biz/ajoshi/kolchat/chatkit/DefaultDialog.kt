package biz.ajoshi.kolchat.chatkit

import biz.ajoshi.kolchat.model.ChatChannel
import com.stfalcon.chatkit.commons.models.IDialog
import com.stfalcon.chatkit.commons.models.IUser

/**
 * Created by ajoshi on 7/4/2017.
 */
class DefaultDialog(public val channel: ChatChannel, private var lastMessageInternal: ChatkitMessage): IDialog<ChatkitMessage> {
    override fun setLastMessage(message: ChatkitMessage?) {
        if (message != null) {
            lastMessageInternal = message
        }
    }

    override fun getLastMessage(): ChatkitMessage {
       // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return lastMessageInternal
    }

    override fun getUnreadCount(): Int {
        // TODO how do I get this
      return 7
    }

    override fun getId(): String {
        return channel.id
    }

    override fun getUsers(): MutableList<out IUser> {
        // todo should I even implement this?
        return mutableListOf()
    }

    override fun getDialogPhoto(): String {
       return ""
    }

    override fun getDialogName(): String {
        return channel.name
    }

}