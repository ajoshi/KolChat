package biz.ajoshi.kolchat.ui

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import biz.ajoshi.commonutils.Logg
import biz.ajoshi.kolchat.R
import biz.ajoshi.kolchat.UserSentLogsEvent

/**
 * Lets us create a dialog for feedback. It will need to be made prettier soon so it's in its own class
 */
class FeedbackDialog(val context: Context, val rootView: View) {
    fun createDialog(): AlertDialog {
        val builder = AlertDialog.Builder(context)
        return builder.setView(R.layout.dialog_send_feedback)
            .setPositiveButton(
                R.string.feedback_dialog_submit,
                DialogInterface.OnClickListener { dialog, _ ->
                    if (dialog is AlertDialog) {
                        val feedback = "" + dialog.findViewById<EditText>(R.id.feedback_text)?.text
                        Logg.i(feedback)
//                        Crashlytics.logException(UserSentLogsEvent("User sent logs " + System.currentTimeMillis()))
                        com.google.android.material.snackbar.Snackbar.make(
                            rootView,
                            "Logs sent!",
                            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                        ).show()
                    }
                })
            .setNegativeButton(R.string.feedback_dialog_cancel, null)
            .create()
    }
}
