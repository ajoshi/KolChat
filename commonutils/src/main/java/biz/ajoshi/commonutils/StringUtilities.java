package biz.ajoshi.commonutils;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.Html;
import android.text.Spanned;

/**
 * Created by ajoshi on 5/22/2017.
 */

public class StringUtilities {
    public static String getBetweenTwoStrings(String body, String first, String second) {
        int firstStartIndex = body.indexOf(first);
        if (firstStartIndex < 0) {
            return null;
        }
        int firstEndIndex =  firstStartIndex + first.length();
        int secondStartIndex = body.indexOf(second, firstEndIndex);
        if (secondStartIndex < 0) {
            // we couldn't find the second string. Just return everything after string1
            secondStartIndex = body.length();
        }
        return body.substring(firstEndIndex, secondStartIndex);
    }

    public static Spanned getHtml(String html) {
        Spanned returnValue;
        if (VERSION.SDK_INT < VERSION_CODES.N) {
            returnValue = Html.fromHtml(html);
        } else {
            returnValue = Html.fromHtml(html, Html.FROM_HTML_OPTION_USE_CSS_COLORS);
        }

        return  returnValue;
    }
}
