package biz.ajoshi.kolchat.chat

import java.text.SimpleDateFormat

// SimpleDateFormat isn't thread safe. Maybe use DateTimeFormatter if we ever edit these instances
val chatMessageDateFormat = SimpleDateFormat.getDateInstance()
val chatMessageTimeFormat = SimpleDateFormat.getTimeInstance()
val chatMessageDateTimeFormat = SimpleDateFormat.getDateTimeInstance()
