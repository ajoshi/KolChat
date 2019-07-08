package biz.ajoshi.kolchat.chat.modifiers

/**
+ * Modifies an object so it can be shown in a different manner from what the server wants.
+ * Mostly useful for modifying chat messages to remove effects like Safari
+ */
interface Modifier<T> {
    /**
     * Takes a chat message and returns a modified version
     */
    fun modify(originalMessage: T): T

    /**
     * true when this modifier is active and should be applied
     */
    fun isActive(): Boolean {
        return false
    }
}