package biz.ajoshi.kolchat.util;

/**
 * Created by ajoshi on 5/22/2017.
 */

public class StringUtil {
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

}
