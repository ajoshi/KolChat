package biz.ajoshi.kolchat.chat.view.customviews

import android.content.Context
import android.content.DialogInterface
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import biz.ajoshi.kolchat.chat.R

/**
 * Floating action button that spins up UI flow for starting up a new chat
 * Launches dialogs, etc. Activity/Fragment can still override click behavior if it wants
 */
class NewChatFAB : FloatingActionButton, View.OnClickListener {

    var chatMessageSender: ChatMessageSender? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initializeViews()
    }

    /**
     * Set up the view and everything that can be set up without being configured
     */
    private fun initializeViews() {
        setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        showNewChatDialog()
    }

    /**
     * Gives the user a choice to send a new PM or send a new message in a channel
     */
    fun showNewChatDialog() {
        context?.let {
            AlertDialog.Builder(it)
                    .setTitle("Send new message")
                    .setItems(R.array.new_message_options, { _, which ->
                        when (which) {
                        // This assumes a specific order in the resource xml
                            0 -> postToUser()
                            1 -> postToChannel()
                        }
                    })
                    .show()
        }
    }

    /**
     * Dialog that lets the user post to a channel
     * // TODO should this auto-listen to the channel as well (by sending /l command)? Right now I say no
     */
    fun postToChannel() {
        context?.let {
            val li = LayoutInflater.from(context)
            val view = li.inflate(R.layout.dialog_send_new_message, rootView as ViewGroup, false)
            view.findViewById<TextView>(R.id.dialog_recipient_name).hint = context.getString(R.string.new_chat_message_hint_channel)
            AlertDialog.Builder(it)
                    .setView(view)
                    .setTitle(R.string.new_chat_message_title_channel)
                    .setPositiveButton(R.string.submit_post, DialogInterface.OnClickListener { dialog: DialogInterface?, _ ->
                        if (dialog is AlertDialog) {
                            val recipient = "" + dialog.findViewById<EditText>(R.id.dialog_recipient_name)?.text
                            val message = "" + dialog.findViewById<EditText>(R.id.dialog_text_input)?.text
                            if (recipient.isNotEmpty() && message.isNotEmpty()) {
                                chatMessageSender?.sendChatMessage(message = message, isPrivate = false, id = recipient)
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show()
        }
    }

    /**
     * Shows dialog that lets the user send a PM to a user
     */
    fun postToUser() {
        context?.let {
            AlertDialog.Builder(it)
                    .setView(R.layout.dialog_send_new_message)
                    .setTitle(R.string.new_chat_message_title_user)
                    .setPositiveButton(R.string.submit_post, DialogInterface.OnClickListener { dialog: DialogInterface?, _ ->
                        if (dialog is AlertDialog) {
                            val recipient = "" + dialog.findViewById<EditText>(R.id.dialog_recipient_name)?.text
                            val message = "" + dialog.findViewById<EditText>(R.id.dialog_text_input)?.text
                            if (recipient.isNotEmpty() && message.isNotEmpty()) {
                                chatMessageSender?.sendChatMessage(message = message, isPrivate = true, id = recipient)
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show()
        }
    }

    /**
     * A class that is able to send chat messages to the server (or have someone else do it)
     */
    interface ChatMessageSender {
        /**
         * Send a message to the given user
         * @param message the text message to send
         * @param isPrivate true if this is a PM
         * @param id ID of the recipient
         */
        fun sendChatMessage(message: CharSequence?, isPrivate: Boolean, id: String)
    }
}