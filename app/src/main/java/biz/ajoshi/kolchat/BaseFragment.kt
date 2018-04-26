package biz.ajoshi.kolchat

import android.support.v4.app.Fragment

/**
 * Defines the behavior of a base fragment. Lets us get titles, etc in a predictable manner
 */
abstract class BaseFragment() : Fragment() {
    open fun getTitle(): String {
        return getString(R.string.app_name)
    }
}
