package biz.ajoshi.kolchat.chat.detail.customviews

import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import biz.ajoshi.kolchat.chat.R
import biz.ajoshi.kolchat.chat.databinding.DialogSendNewMessageBinding

/**
 * Floating action button that spins up UI flow for starting up a new chat
 * Launches dialogs, etc. Activity/Fragment can still override click behavior if it wants
 */
class NewChatFAB : com.google.android.material.floatingactionbutton.FloatingActionButton,
    View.OnClickListener {

    var chatMessageSender: ChatMessageSender? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
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
            val dialogBinding =
                DialogSendNewMessageBinding.inflate(li, rootView as ViewGroup, false)
            dialogBinding.dialogRecipientName.hint =
                context.getString(R.string.aj_chat_new_chat_message_hint_channel)
            val dialog = AlertDialog.Builder(it)
                .setView(dialogBinding.root)
                .setTitle(R.string.aj_chat_new_chat_message_title_channel)
                .setPositiveButton(
                    R.string.aj_chat_submit_post,
                    DialogInterface.OnClickListener { dialog: DialogInterface?, _ ->
                        if (dialog is AlertDialog) {
                            val recipient = "" + dialogBinding.dialogRecipientName.text
                            val message = "" + dialogBinding.dialogTextInput.text
                            if (recipient.isNotEmpty() && message.isNotEmpty()) {
                                chatMessageSender?.sendChatMessage(
                                    message = message,
                                    isPrivate = false,
                                    id = recipient
                                )
                            }
                        }
                    })
                .setNegativeButton(R.string.aj_chat_cancel, null)
                .create()
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            dialog.show()
        }
    }

    /**
     * Shows dialog that lets the user send a PM to a user
     */
    fun postToUser() {
        context?.let {
            val dialog = AlertDialog.Builder(it)
                .setView(R.layout.dialog_send_new_message)
                .setTitle(R.string.aj_chat_new_chat_message_title_user)
                .setPositiveButton(
                    R.string.aj_chat_submit_post,
                    DialogInterface.OnClickListener { dialog: DialogInterface?, _ ->
                        if (dialog is AlertDialog) {
                            val recipient =
                                "" + dialog.findViewById<EditText>(R.id.dialog_recipient_name)?.text
                            val message =
                                "" + dialog.findViewById<EditText>(R.id.dialog_text_input)?.text
                            if (recipient.isNotEmpty() && message.isNotEmpty()) {
                                chatMessageSender?.sendChatMessage(
                                    message = message,
                                    isPrivate = true,
                                    id = recipient
                                )
                            }
                        }
                    })
                .setNegativeButton(R.string.aj_chat_cancel, null).create()
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            dialog.show()
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
