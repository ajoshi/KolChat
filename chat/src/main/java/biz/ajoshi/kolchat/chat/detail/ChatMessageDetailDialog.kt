package biz.ajoshi.kolchat.chat.detail


import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import biz.ajoshi.commonutils.StringUtilities
import biz.ajoshi.kolchat.chat.R
import biz.ajoshi.kolchat.chat.modifiers.SafariModifier
import biz.ajoshi.kolchat.persistence.chat.ChatMessage
import java.lang.ref.WeakReference

/**
 * Lets us create a dialog for feedback. It will need to be made prettier soon so it's in its own class
 */
class ChatMessageDetailDialog(val context: Context) : DialogInterface.OnClickListener {
    lateinit var listener: WeakReference<MessageDetailDialogListener>
    lateinit var message: ChatMessage

    fun createDialog(message: ChatMessage, listener: MessageDetailDialogListener): AlertDialog {
        this.listener = WeakReference(listener)
        this.message = message

        val builder = AlertDialog.Builder(context)

        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.dialog_chat_detail, null)
        layout.findViewById<TextView>(R.id.chat_message)?.text = (StringUtilities.getHtml(message.text))
        layout.findViewById<TextView>(R.id.chat_title)?.text = StringUtilities.getHtml(message.userName).toString()
        //TODO too much hardcoding in here
        val options = listOf<CharSequence>("Send PM", "Copy text", "Copy userId", "Remove safari from message")
        return builder
                // show plaintext username as title
                .setCustomTitle(layout)
                .setItems(options.toTypedArray(), this)
                .create()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            //TODO too much hardcoding in here
            0 -> listener.get()?.sendPm(message = message)
            1 -> listener.get()?.copyText(text = StringUtilities.getHtml(message.text))
            2 -> listener.get()?.copyText(text = message.userId)
            3 -> {
                val safariModifier = SafariModifier()
                listener.get()?.showText(StringUtilities.getHtml(safariModifier.modify(message).text))
            }
        }
    }
}

interface MessageDetailDialogListener {
    fun sendPm(message: ChatMessage)
    fun copyText(text: CharSequence)
    fun showText(text: CharSequence)
}