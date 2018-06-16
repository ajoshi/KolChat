package biz.ajoshi.kolnetwork.model

/**
 * Response sent by the server. This class lets UI classes differentiate between Rollover/invalid hash/network failure
 * and success states
 */
class NetworkResponse {
    val response: String
    val status: NetworkStatus

    /**
     * Creates a SUCCESS response with the given responseString
     */
    constructor(responseString: String) {
        response = responseString
        status = NetworkStatus.SUCCESS
    }

    /**
     * Creates a response with an empty responseString. No point using this with success
     */
    constructor(networkStatus: NetworkStatus) {
        response = ""
        status = networkStatus
    }

    /**
     * Creates a response with the given values
     */
    constructor(responseString: String, networkStatus: NetworkStatus) {
        response = responseString
        status = networkStatus
    }

    /**
     * Just an easier way to check that the call succeeded
     */
    fun isSuccessful(): Boolean {
        return status == NetworkStatus.SUCCESS
    }
}

/**
 * Possible network statuses (statii?)
 */
enum class NetworkStatus {
    SUCCESS, ROLLOVER, FAILURE, INVALID_HASH, UNKNOWN
}
