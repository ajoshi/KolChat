package biz.ajoshi.kolchat.chat.persistence

import android.arch.persistence.room.EmptyResultSetException
import biz.ajoshi.kolchat.persistence.KolDB
import biz.ajoshi.kolchat.persistence.chat.ChatChannel
import biz.ajoshi.kolchat.persistence.chat.ChatMessage
import biz.ajoshi.kolnetwork.model.ServerChatMessage

/**
 * Converts Networkmodels into Room's models and inserts into db
 */
class RoomInserter {

    fun insertMessage(serverMessage: ServerChatMessage, currentUserName: String) {
        val dbMessage = ChatMessage(0,
                serverMessage.author.id,
                serverMessage.author.name,
                serverMessage.htmlText,
                serverMessage.channelNameServer.id,
                serverMessage.time,
                serverMessage.localTime,
                serverMessage.hideAuthorName,
                currentUserName)
        val dbChannel = ChatChannel(serverMessage.channelNameServer.id,
                serverMessage.channelNameServer.isPrivate,
                serverMessage.channelNameServer.name,
                dbMessage.text,
                dbMessage.localtimeStamp, currentUserName, 0L)
        KolDB.getDb()?.ChannelDao()?.insert(dbChannel)
        KolDB.getDb()?.MessageDao()?.insert(dbMessage)
    }

    fun insertAllMessages(messages: List<ServerChatMessage>, currentUserName: String) {
        val listOfMessages = mutableListOf<ChatMessage>()
        val channels = mutableSetOf<ChatChannel>()

        for (serverMessage in messages) {
            val dbMessage = ChatMessage(0,
                    serverMessage.author.id,
                    serverMessage.author.name,
                    serverMessage.htmlText,
                    serverMessage.channelNameServer.id,
                    serverMessage.time,
                    serverMessage.localTime,
                    serverMessage.hideAuthorName,
                    currentUserName)
            val dbChannel = ChatChannel(serverMessage.channelNameServer.id,
                    serverMessage.channelNameServer.isPrivate,
                    serverMessage.channelNameServer.name,
                    dbMessage.text,
                    dbMessage.localtimeStamp, currentUserName, 0L)
            listOfMessages.add(dbMessage)
            // maybe we can use a set here so we don't try to insert the same channel too many times?
            channels.add(dbChannel)
            //   KolDB.getDb()?.ChannelDao()?.insert(dbChannel)
        }

        for (chatChannel in channels) {
            try {
                // TODO use this rxjava-ish way or just have getChannel return a nullable? The latter seems more reasonable
                KolDB.getDb().ChannelDao().getChannel(chatChannel.id).blockingGet()
                KolDB.getDb()?.ChannelDao()?.updateChannelWithNewMessage(chatChannel.id, chatChannel.lastMessage, chatChannel.lastMessageTime)
            } catch (e: EmptyResultSetException) {
                // the channel wasn't in the db, but this seems to never get thrown?
                KolDB.getDb()?.ChannelDao()?.insert(chatChannel)
            }
        }

        // hopefully bulk insert is faster than one by one insertion
        KolDB.getDb()?.MessageDao()?.insertAll(*listOfMessages.toTypedArray())
    }
}
