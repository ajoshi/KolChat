package biz.ajoshi.kolchat.chat.modifiers

import biz.ajoshi.kolchat.persistence.chat.ChatMessage

/**
 * Changes the name of the user posting this message to show a flair (so people can be tagged with
 * little notes)
 */
class UserFlairModifier : Modifier<ChatMessage> {
    override fun modify(originalMessage: ChatMessage): ChatMessage {
        val returnMessage = originalMessage.clone()
        returnMessage.userName = getUserFlair(returnMessage)
        return returnMessage
    }

    private fun getUserFlair(message: ChatMessage): String {
        return message.userName
    }
}