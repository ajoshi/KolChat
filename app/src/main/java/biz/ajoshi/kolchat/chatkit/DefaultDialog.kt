package biz.ajoshi.kolchat.chatkit

import biz.ajoshi.kolchat.model.ServerChatChannel
import com.stfalcon.chatkit.commons.models.IDialog
import com.stfalcon.chatkit.commons.models.IUser

/**
 * Created by ajoshi on 7/4/2017.
 */
class DefaultDialog(public val channelServer: ServerChatChannel, private var lastMessageInternal: ChatkitMessage): IDialog<ChatkitMessage> {
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
        return channelServer.id
    }

    override fun getUsers(): MutableList<out IUser> {
        // todo should I even implement this?
        return mutableListOf()
    }

    override fun getDialogPhoto(): String {
       return ""
    }

    override fun getDialogName(): String {
        return channelServer.name
    }

}
