package biz.ajoshi.kolchat.persistence

import biz.ajoshi.kolchat.KolChatApp
import biz.ajoshi.kolchat.model.ServerChatMessage

/**
 * Converts Networkmodels into Room's models and inserts into db
 */
class RoomInserter {
    fun insertMessage(serverMessage: ServerChatMessage) {
        val dbMessage = ChatMessage(0, serverMessage.author.id, serverMessage.author.name, serverMessage.htmlText, serverMessage.channelNameServer.id, serverMessage.time, serverMessage.localTime, serverMessage.hideAuthorName)
        val dbChannel = ChatChannel(serverMessage.channelNameServer.id,
                serverMessage.channelNameServer.isPrivate,
                serverMessage.channelNameServer.name,
                dbMessage.text,
                dbMessage.localtimeStamp)
        KolChatApp.database?.ChannelDao()?.insert(dbChannel)
        KolChatApp.database?.MessageDao()?.insert(dbMessage)
    }

    fun insertAllMessages(messages: List<ServerChatMessage>) {
        val listOfMessages = mutableListOf<ChatMessage>()
        for (serverMessage in messages) {
            val dbMessage = ChatMessage(0, serverMessage.author.id, serverMessage.author.name, serverMessage.htmlText, serverMessage.channelNameServer.id, serverMessage.time, serverMessage.localTime, serverMessage.hideAuthorName)
            val dbChannel = ChatChannel(serverMessage.channelNameServer.id,
                    serverMessage.channelNameServer.isPrivate,
                    serverMessage.channelNameServer.name,
                    dbMessage.text,
                    dbMessage.localtimeStamp)
            listOfMessages.add(dbMessage)
            // maybe we can use a set here so we don't try to insert the same channel too many times?
            KolChatApp.database?.ChannelDao()?.insert(dbChannel)
        }

        // hopefully bulk insert is faster than one by one insertion
        KolChatApp.database?.MessageDao()?.insertAll(*listOfMessages.toTypedArray())
    }
}
