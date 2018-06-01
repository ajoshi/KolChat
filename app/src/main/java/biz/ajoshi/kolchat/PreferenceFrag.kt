package biz.ajoshi.kolchat

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat

class PreferenceFrag : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
