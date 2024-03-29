package biz.ajoshi.kolchat

/**
 * Enumeration of the theme types that are supported
 */
enum class Theme {
    KOL, CHILL, FRUITS
}

/**
 * Allows us to store and fetch the theme of the app. This does not apply the theme- that must be done by the activity
 */
class Themer {
    companion object {
        var currentTheme = Theme.KOL
    }

    fun getThemeId(): Int {
        return when (currentTheme) {
            Theme.CHILL -> R.style.KoLTheme_ChillOut
            Theme.FRUITS -> R.style.KoLTheme_Fruit
            Theme.KOL -> R.style.KoLTheme
        }
    }

    fun setTheme(newTheme: Theme) {
        currentTheme = newTheme
    }
}
