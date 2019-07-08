package biz.ajoshi.kolchat.chat.modifiers

import biz.ajoshi.kolchat.chat.detail.customviews.NewChatFAB
import biz.ajoshi.kolchat.persistence.chat.ChatMessage

/**
 * Replaces a message with quacks or something (removes it entirely?) to force a local baleet
 */
class LocalBaleetModifier : Modifier<ChatMessage> {
    override fun modify(originalMessage: ChatMessage): ChatMessage {
        if (isUserInBaleetList(originalMessage.userId)) {
            return quackify(originalMessage)
        }
        return originalMessage
    }

    private fun isUserInBaleetList(userId: String): Boolean {
        return false
    }

    private fun quackify(originalMessage: ChatMessage): ChatMessage {
        val quack = "quack "
        var fakeChatMessage = originalMessage.clone()
        val count = (fakeChatMessage.text.length / quack.length) + 1
        val sb = StringBuffer()
        for (i in 1..count) {
            sb.append(quack)
        }
        fakeChatMessage.text = sb.toString()
        return fakeChatMessage
    }
}
