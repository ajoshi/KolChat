package biz.ajoshi.kolchat.util

import biz.ajoshi.kolchat.ChatManagerKotlin
import biz.ajoshi.kolchat.Network

/**
 * Performs chat operations and also persists them in db
 */
class PersistantChatManager(val network: Network) {
    // performs the chat operations
    val chatManager = ChatManagerKotlin(network)


}