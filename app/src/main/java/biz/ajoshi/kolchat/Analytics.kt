package biz.ajoshi.kolchat

import com.crashlytics.android.answers.Answers

/**
 * Analytics constants for Crashlytics live here. This ensures we don't have typos messing up our data
 */

// app was launched
const val EVENT_NAME_APP_LAUNCH = "App launch"
// source of the launch
const val EVENT_ATTRIBUTE_SOURCE = "Source"
const val EVENT_ATTRIBUTE_IS_POLLING_ENABLED = "Polling enabled"
const val EVENT_ATTRIBUTE_IS_FAST_POLLING_ENABLED = "Fast polling enabled"

// Chat message sent
const val EVENT_NAME_CHAT_MESSAGE_SENT = "Message Sent"
// recipient of the message
const val EVENT_ATTRIBUTE_RECIPIENT = "Recipient"
// character count of the message
const val EVENT_ATTRIBUTE_MESSAGE_LENGTH = "Message Length"

// time taken to load data (for perf measurement)
const val EVENT_ATTRIBUTE_TIME_TAKEN = "Time to load"

/**
 * singleton that lets us get an Answers object if tracking is allowed. Else it returns null
 */
object Analytics {
    var shouldTrackEvents = true

    /**
     * Returns a valid Answers object if the user has allowed it, else null
     */
    fun getAnswers(): Answers? {
        return if (shouldTrackEvents) {
            Answers.getInstance()
        } else {
            null
        }
    }
}
