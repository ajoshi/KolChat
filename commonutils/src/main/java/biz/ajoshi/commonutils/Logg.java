package biz.ajoshi.commonutils;

import android.util.Log;

/*
 * Centralizes logging in one place so it can be added without fear of leaving log lines in prod code
 *
 * //TODO send in logger and have it use fabric instead of using statics
 */
public class Logg {
    public static void e(String message) {
        e("ajoshi", message);
    }

    public static void e(String tag, String message) {
        Log.e(tag, message);
    }

    public static void w(String message) {
        w("ajoshi", message);
    }

    public static void w(String tag, String message) {
        Log.w(tag, message);
    }

    public static void i(String message) {
        i("ajoshi", message);
    }

    public static void i(String tag, String message) {
        Log.i(tag, message);
    }
}
