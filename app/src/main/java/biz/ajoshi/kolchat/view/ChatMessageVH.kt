package biz.ajoshi.kolchat.view

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import biz.ajoshi.kolchat.persistence.ChatMessage
import kotlinx.android.synthetic.main.chat_message.*
import kotlinx.android.synthetic.main.chat_message.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ajoshi on 7/22/17.
 */
val timeFormat = SimpleDateFormat.getTimeInstance()
class ChatMessageVH(itemView: View): RecyclerView.ViewHolder(itemView) {
    // TODO this might be doing a findviewbyid each time and not caching. confirm.
    fun bind(message: ChatMessage) {
        // TODO don't call fromhtml on ui thread. maybe use Transform/Map to convert to some ui model
        itemView.text.text = Html.fromHtml(message.text)
        itemView.user_name.text = Html.fromHtml(message.userName)
        itemView.timestamp.text = timeFormat.format(Date(message.localtimeStamp))
        /* allow links to be clicked
        htmlText= This is the only link I need! <a target=_blank href="https://www.kingdomofloathing.com/"><font color=blue>[link]</font></a> https:// www.kingdomofloathin g.com/,
        only the 'link' part is clickable with this. Alt solution is to string replace and then enable autolink
        */
        itemView.text.movementMethod = LinkMovementMethod.getInstance();
        // todo use userid for right click options at some point
    }
}
