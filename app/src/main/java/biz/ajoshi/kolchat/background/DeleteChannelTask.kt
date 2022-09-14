package biz.ajoshi.kolchat.background

import android.os.AsyncTask
import biz.ajoshi.kolchat.persistence.KolDB
import biz.ajoshi.kolchat.persistence.chat.ChatChannel

/**
 * Deletes a channel (or more) from the internal database
 */
class DeleteChannelTask : AsyncTask<ChatChannel, Unit, Unit>() {
    override fun doInBackground(vararg params: ChatChannel?) {
        for (channel in params) {
            // I'll never send more than one (I think) but whatever
            channel?.let {
                // delete the messages in this channel
                KolDB.getDb()?.MessageDao()?.deleteAllForChannelId(it.id)
                // delete this channel as well
                KolDB.getDb()?.ChannelDao()?.delete(it)
            }
        }
    }
}