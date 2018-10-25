package biz.ajoshi.kolchat.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import biz.ajoshi.kolchat.R

/**
 * Auth creds of a user logged in to kol
 */
data class KolAccount(val username: String, val password: String, val isActive: Boolean)

class KolAccountManager(val ctx: Context) {
    val USER_TYPE_KOL = ctx.getString(R.string.account_type)
    val USER_TYPE_SHAREDPREF = "KolCurrentAccount"
    val SHARED_PREF_USERNAME = "currentUserName"
    val sharedPreferences = ctx.getSharedPreferences(USER_TYPE_SHAREDPREF, Context.MODE_PRIVATE)
    var currentUsername = sharedPreferences.getString(SHARED_PREF_USERNAME, "")

    fun addAccount(username: String, password: String) {
        // TODO insert credentials into acctmgr or smartlock(?), maybe both?
        val acctMgr = AccountManager.get(ctx)
        acctMgr.addAccountExplicitly(Account(username, USER_TYPE_KOL), password, null)
        setCurrentAccount(username)
    }

    fun getAccount(username: String): KolAccount? {
        val acctMgr = AccountManager.get(ctx)
        val accountList = acctMgr.getAccountsByType(USER_TYPE_KOL)
        for (account in accountList) {
            if (account.name == username) {
                return KolAccount(
                        username = account.name,
                        password = acctMgr.getPassword(account),
                        isActive = currentUsername == account.name)
            }
        }
        return null
    }

    fun getAllAccounts(): List<KolAccount> {
        val accountList = mutableListOf<KolAccount>()
        val acctMgr = AccountManager.get(ctx)
        val accountArray = acctMgr.getAccountsByType(USER_TYPE_KOL)
        for (account in accountArray) {
            accountList.add(KolAccount(
                    username = account.name,
                    password = acctMgr.getPassword(account),
                    isActive = currentUsername == account.name))
        }
        return accountList
    }

    /**
     * Some amount of 'login-ness' needs to be persisted across app death/phone restart. Since we can have multiple
     * accounts, we need to know which one is currently active.
     *
     * This returns the active KolAccount if it exists
     */
    fun getCurrentAccount(): KolAccount? {
        if (currentUsername.isNullOrEmpty()) {
            return null
        }
        return getAllAccounts().single { acct -> acct.username == currentUsername }
    }

    /**
     * Logs out the 'current' account
     */
    fun logout() {
        setCurrentAccount("")
    }

    /**
     * Sets the account with this username to be 'current'. does not validate anything
     */
    fun setCurrentAccount(username: String) {
        currentUsername = username
        sharedPreferences.edit().putString(SHARED_PREF_USERNAME, username).apply()
    }
}
