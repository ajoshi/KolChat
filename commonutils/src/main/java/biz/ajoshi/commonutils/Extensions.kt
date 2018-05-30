package biz.ajoshi.commonutils

import android.content.Context
import android.os.Build
import android.support.annotation.ColorInt

/**
 * Returns the color for the given color resource id. Uses the default/null theme
 */
@ColorInt
fun Context.getDefaultColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= 23) {
        this.getColor(id)
    } else {
        this.resources.getColor(id)
    }
}
