package biz.ajoshi.kolchat

/**
 * Created by ajoshi on 7/13/17.
 */
object ChatSingleton {
    var chatManager: ChatManagerKotlin? = null
    var network: Network? = null

    fun login(username: String, password: String, silent: Boolean) {
        network = Network(username, password, silent)
        network!!.login()
        chatManager = ChatManagerKotlin(network!!)
    }
}
