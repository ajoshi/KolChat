package biz.ajoshi.kolchat.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import biz.ajoshi.kolchat.R

/**
 * Auth creds of a user logged in to kol
 */
data class KolAccount(val username: String, val password: String)

class KolAccountManager(val ctx: Context) {
    val USER_TYPE_KOL = ctx.getString(R.string.account_type)

    fun addAccount(username: String, password: String) {
        // TODO insert credentials into acctmgr or smartlock(?), maybe both?
        val acctMgr = AccountManager.get(ctx)
        acctMgr.addAccountExplicitly(Account(username, USER_TYPE_KOL), password, null)
    }

    fun getAccount(username: String): KolAccount? {
        val acctMgr = AccountManager.get(ctx)
        val accountList = acctMgr.getAccountsByType(USER_TYPE_KOL)
        for (account in accountList) {
            if (account.name.equals(username)) {
                return KolAccount(username = account.name, password = acctMgr.getPassword(account))
            }
        }
        return null
    }

    fun getAllAccounts(): List<KolAccount> {
        val accountList = mutableListOf<KolAccount>()
        val acctMgr = AccountManager.get(ctx)
        val accountArray = acctMgr.getAccountsByType(USER_TYPE_KOL)
        for (account in accountArray) {
            accountList.add(KolAccount(username = account.name, password = acctMgr.getPassword(account)))
        }
        return accountList
    }
}
