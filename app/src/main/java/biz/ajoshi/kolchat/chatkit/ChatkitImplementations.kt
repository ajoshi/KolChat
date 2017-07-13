package biz.ajoshi.kolchat.chatkit

import biz.ajoshi.kolchat.model.ServerChatMessage
import biz.ajoshi.kolchat.model.User
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.IUser
import java.util.*

/**
 * Created by ajoshi on 7/4/2017.
 */
class ChatkitUser (val user: User): IUser {
    /**
     * Returns the user's name

     * @return the user's name
     * *
     */
    override fun getName(): String {
        return user.name
    }

    /**
     * Returns the user's id

     * @return the user's id
     * *
     */
    override fun getId(): String {
        return user.id
    }

    /**
     * Returns the user's avatar image url

     * @return the user's avatar image url
     * *
     */
    override fun getAvatar(): String {
        // TODO look for profile pic at some point in the future
        return ""
    }

}

class ChatkitMessage (val chatmessage: ServerChatMessage): IMessage{
    /**
     * Returns message identifier

     * @return the message id
     */
    override fun getId(): String {
        // TODO maybe have chat have a fake id field as well? timestamp + index in list of received chats
        return chatmessage.author.id + chatmessage.htmlText.hashCode()
    }

    /**
     * Returns message creation date

     * @return the message creation date
     */
    override fun getCreatedAt(): Date {
        return chatmessage.time
    }

    /**
     * Returns message author. See the [IUser] for more details

     * @return the message author
     */
    override fun getUser(): IUser {
        return ChatkitUser(chatmessage.author)
    }

    /**
     * Returns message text

     * @return the message text
     */
    override fun getText(): String {
        return chatmessage.htmlText
    }

}
