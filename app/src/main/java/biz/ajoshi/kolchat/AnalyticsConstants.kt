package biz.ajoshi.kolchat

/**
 * Analytics constants for Crashlytics live here. This ensures we don't have typos messing up our data
 */

// app was launched
const val EVENT_NAME_APP_LAUNCH = "App launch"
// source of the launch
const val EVENT_ATTRIBUTE_SOURCE = "Source"

// Chat message sent
const val EVENT_NAME_CHAT_MESSAGE_SENT = "Message Sent"
// recipient of the message
const val EVENT_ATTRIBUTE_RECIPIENT = "Recipient"
// character count of the message
const val EVENT_ATTRIBUTE_MESSAGE_LENGTH = "Message Length"
