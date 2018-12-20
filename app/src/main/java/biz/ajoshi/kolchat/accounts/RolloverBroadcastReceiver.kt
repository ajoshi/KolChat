package biz.ajoshi.kolchat.accounts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import biz.ajoshi.commonutils.Logg
import biz.ajoshi.kolchat.chat.ACTION_CHAT_ROLLOVER
import biz.ajoshi.kolchat.chat.ACTION_CHAT_ROLLOVER_OVER

/**
 * Makes snackbars and holds state (uh oh?) for the activity's model of rollover
 */
class RolloverBroadcastReceiver : BroadcastReceiver() {
    private var view: View? = null
    private var snackbar: com.google.android.material.snackbar.Snackbar? = null

    var isRollover = false

    override fun onReceive(context: Context, intent: Intent) {
        view?.let {
            when(intent.action) {
                ACTION_CHAT_ROLLOVER -> {
                    isRollover = true
                    // showing a snackbar now, but I'd imagine we should blank out the UI or something
                    snackbar = com.google.android.material.snackbar.Snackbar.make(it, "Rollover in progress", com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE)
                    snackbar?.show()
                }
                ACTION_CHAT_ROLLOVER_OVER ->  {
                    isRollover = false
                    snackbar?.dismiss()
                }
                else -> {
                    Logg.i("RO Receiver got unexpected action: " + intent.action)
                }
            }
        }

    }

    /**
     * Unregister the listener
     */
    fun unregister(context: Context?) {
        context?.let {
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(it).unregisterReceiver(
                    this);
        }
        view = null
        // maybe we shouldn't dismiss?
        snackbar?.dismiss()
        snackbar = null
    }

    /**
     * Register the listener for the local broadcast
     */
    fun register(context: Context?, snackbarRootView: View) {
        context?.let {
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(it).registerReceiver(
                    this, IntentFilter(ACTION_CHAT_ROLLOVER));
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(it).registerReceiver(
                    this, IntentFilter(ACTION_CHAT_ROLLOVER_OVER));
        }
        view = snackbarRootView
    }
}
