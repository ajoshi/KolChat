package biz.ajoshi.kolchat

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceScreen
import androidx.navigation.fragment.findNavController

const val KEY_PREF_SEND_USERNAME = "pref_send_username"
const val KEY_PREF_SEND_LOGS = "pref_send_logs"
const val KEY_PREF_TRACK_EVENTs = "pref_track_events"
const val KEY_PREF_ENABLE_POLL = "pref_enable_poll"
const val KEY_PREF_SUPER_FAST_POLL = "pref_super_fast_poll"

private const val ARG_ROOTKEY = "arg_rootkey"

/**
 * Shows the preferences based on the preferences xml
 */
class PreferenceFrag : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val key = arguments?.getString(ARG_ROOTKEY)
        setPreferencesFromResource(R.xml.preferences, key ?: rootKey)
    }

    override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
        // based off of https://stackoverflow.com/a/46179417
        // Turns out prefFragCompat doesn't support nested screens so we create a new instance of this frag and navigate to it.
        val nextPreferenceFrag = PreferenceFrag()
        val args = Bundle()
        args.putString(ARG_ROOTKEY, preferenceScreen.key)
        nextPreferenceFrag.arguments = args
        findNavController().navigate(R.id.nav_preferences, args)
    }
}
