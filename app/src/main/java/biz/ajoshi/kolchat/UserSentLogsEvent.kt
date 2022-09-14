package biz.ajoshi.kolchat

/**
 * Class used to send logs to Fabric. We could use this to capture user feedback as well
 */
class UserSentLogsEvent(s: String?) : IllegalStateException(s)
