package biz.ajoshi.kolchat.persistence

import biz.ajoshi.kolchat.KolChatApp
import biz.ajoshi.kolchat.model.ServerChatMessage

/**
 * Converts Networkmodels into Room's models and inserts into db
 */
class RoomInserter {
    fun insertMessage(serverMessage: ServerChatMessage) {
        val dbMessage = ChatMessage(0, serverMessage.author.id, serverMessage.author.name, serverMessage.htmlText, serverMessage.channelNameServer.id, serverMessage.time)
        val dbChannel = ChatChannel(serverMessage.channelNameServer.id,
                serverMessage.channelNameServer.isPrivate,
                serverMessage.channelNameServer.name,
                dbMessage.text,
                dbMessage.timeStamp)
        KolChatApp.database?.ChannelDao()?.insert(dbChannel)
        KolChatApp.database?.MessageDao()?.insert(dbMessage)
    }
}
