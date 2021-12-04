package biz.ajoshi.kolchat.chat.modifiers

import biz.ajoshi.kolchat.persistence.chat.ChatMessage

/**
 * Removes safari effects from the message. Compensates for SOCKS, but not the 10 other effects
 * that destroy this html
 */
class SafariModifier : Modifier<ChatMessage> {
    // slightly better regex that's easier to read
    private val nicerRegex = Regex("<[ií] t[i(&#237;)í]tle=\\\"(.*?)\\\">[[:graph:]]*<\\/i>")

    // crappy regex because server applies socks after safari
    private val shittyRegex =
        Regex("<(i|&#237;|í) t(i|&#237;|í)tl(e|&#233;|è|é|ë|ê)=\\\"(.*?)\\\">[[:graph:]]*<\\/(i|&#237;|í)>")

    // How many capture groups exist before the *? one
    private val INDEX_OF_REPLACED_TEXT = 4

    override fun modify(originalMessage: ChatMessage): ChatMessage {
        val chatMessage = originalMessage.clone()
        val allMatches2 = shittyRegex.findAll(chatMessage.text)
        for (matchResult in allMatches2) {
            matchResult.groupValues
            chatMessage.text = chatMessage.text.replaceFirst(
                matchResult.groupValues[0],
                matchResult.groupValues[INDEX_OF_REPLACED_TEXT]
            )
        }
        return chatMessage
    }
}
