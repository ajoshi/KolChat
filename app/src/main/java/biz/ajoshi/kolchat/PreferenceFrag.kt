package biz.ajoshi.kolchat

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat

const val KEY_PREF_SEND_LOGS = "pref_send_logs"
const val KEY_PREF_TRACK_EVENTs = "pref_track_events"

class PreferenceFrag : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
