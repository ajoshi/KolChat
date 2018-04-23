package biz.ajoshi.kolchat.accounts

import android.content.Context
import android.support.v4.content.AsyncTaskLoader

/**
 * Loader that will return lists of usernames so the UI can be populated nicely
 */
class AccountLoader(context: Context) : AsyncTaskLoader<List<KolAccount>>(context) {
    override fun loadInBackground(): List<KolAccount>? {
        return KolAccountManager(context).getAllAccounts()
    }

    override fun onStartLoading() {
        // TODO why does this think it's always got cached data?
//        if (takeContentChanged())
        forceLoad()
    }

    override fun onStopLoading() {
        cancelLoad()
    }
}
