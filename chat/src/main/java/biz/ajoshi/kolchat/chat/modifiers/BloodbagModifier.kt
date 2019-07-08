package biz.ajoshi.kolchat.chat.modifiers

import biz.ajoshi.kolchat.persistence.chat.ChatMessage

/**
 * Makes the bloodbag messages red like a fine wine or something
 */
class BloodbagModifier : Modifier<ChatMessage> {
    override fun modify(originalMessage: ChatMessage): ChatMessage {
        return originalMessage
    }
}