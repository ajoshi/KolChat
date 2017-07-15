package biz.ajoshi.kolchat

/**
 * Created by ajoshi on 7/13/17.
 */
object ChatSingleton {
    var chatManager: ChatManagerKotlin? = null
    var network: Network? = null

    fun login(username: String, password: String, silent: Boolean): Boolean {
        network = Network(username, password, silent)
        // TODO this assumes success. Handle failure
        network!!.login()
        if (!network!!.isLoggedIn) return false
        chatManager = ChatManagerKotlin(network!!)
        return true
    }

    fun loginIfNeeded(username: String, password: String, silent: Boolean): Boolean {
        if (network == null) {
            return login(username, password, silent)
        }
        return true
    }
}
