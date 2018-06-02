package biz.ajoshi.kolchat.ui

import android.content.Intent
import android.support.v4.app.Fragment
import biz.ajoshi.kolchat.*
import biz.ajoshi.kolchat.chat.ChatBackgroundService
import biz.ajoshi.kolchat.chat.ChatManager
import biz.ajoshi.kolchat.chat.EXTRA_CHAT_MESSAGE_TO_SEND
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent

/**
 * Defines the behavior of a base fragment. Lets us get titles, etc in a predictable manner
 */
abstract class BaseFragment : Fragment() {
    open fun getTitle(): String {
        return getString(R.string.app_name)
    }

    /**
     * Send a chat message to this channel/user
     */
    fun makePost(post: CharSequence?, isPrivate: Boolean, id: String): Boolean {
        // log to analytics as well
       Analytics.getAnswers()?.logCustom(CustomEvent(EVENT_NAME_CHAT_MESSAGE_SENT)
                .putCustomAttribute(EVENT_ATTRIBUTE_RECIPIENT, if (isPrivate) "PM" else id)
                .putCustomAttribute(EVENT_ATTRIBUTE_MESSAGE_LENGTH, post?.length)
        )
        return sendChatCommand(ChatManager.getChatString(post, id, isPrivate))
    }

    /**
     * Send a chat command to the server. Is not scoped to this channel, so scoping should be
     * done before calling this
     */
    fun sendChatCommand(command: String): Boolean {
        val serviceIntent = Intent(activity, ChatBackgroundService::class.java)
        serviceIntent.putExtra(EXTRA_CHAT_MESSAGE_TO_SEND, command)
        activity?.startService(serviceIntent)
        return true
    }

}
