package biz.ajoshi.kolchat.chatkit

import biz.ajoshi.kolchat.model.User
import biz.ajoshi.kolchat.persistence.ChatMessage
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.IUser
import java.util.*

/**
 * Created by ajoshi on 7/4/2017.
 */
class ChatkitUser(val user: User) : IUser {
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

class ChatkitMessage(val chatmessage: ChatMessage) : IMessage {
    /**
     * Returns message identifier

     * @return the message id
     */
    override fun getId(): String {
        return chatmessage.id.toString()
    }

    /**
     * Returns message creation date

     * @return the message creation date
     */
    override fun getCreatedAt(): Date {
        return Date(chatmessage.timeStamp)
    }

    /**
     * Returns message author. See the [IUser] for more details

     * @return the message author
     */
    override fun getUser(): IUser {
        return ChatkitUser(User(id = chatmessage.userId, name = chatmessage.userName))
    }

    /**
     * Returns message text

     * @return the message text
     */
    override fun getText(): String {
        return chatmessage.text
    }

}
