package biz.ajoshi.commonutils;

import android.util.Log;

/*
 * Centralizes logging in one place so it can be added without fear of leaving log lines in prod code
 *
 * //TODO send in logger and have it use fabric instead of using statics
 */
public class Logg {

    public static CustomLogger logger = null;

    /**
     * Set a custom logger so modules can log to a central location (like Crashlytics)
     *
     * @param customLogger
     */
    public static void setCustomLogger(CustomLogger customLogger) {
        logger = customLogger;
    }

    public static void e(String message) {
        e("ajoshi", message);
    }

    public static void e(String tag, String message) {
        if (logger != null) {
            logger.logE(tag, message);
        } else {
            Log.e(tag, message);
        }
    }

    public static void w(String message) {
        w("ajoshi", message);
    }

    public static void w(String tag, String message) {
        if (logger != null) {
            logger.logW(tag, message);
        } else {
            Log.w(tag, message);
        }
    }

    public static void i(String message) {
        i("ajoshi", message);
    }

    public static void i(String tag, String message) {
        if (logger != null) {
            logger.logI(tag, message);
        } else {
            Log.i(tag, message);
        }
    }

    /**
     * Logs a throwable to the logger or logcat 
     *
     * @param t
     *         throwable to log
     */
    public static void logThrowable(Throwable t) {
        logThrowable("ajoshi", t);
    }

    /**
     * Logs a throwable to the logger or logcat
     *
     * @param tag
     *         tag for the message
     * @param t
     *         throwable to log
     */
    public static void logThrowable(String tag, Throwable t) {
        Log.w(tag, t);
    }

    /**
     * Custom logger that will log to a non-logcat location.
     * This lets the app decide where individual modules log data without having the module include dependencies
     */
    public interface CustomLogger {
        /**
         * Logs an error message
         *
         * @param tag
         *         tag for the message
         * @param message
         *         message to log
         */
        void logE(String tag, String message);

        /**
         * Logs a warning message
         *
         * @param tag
         *         tag for the message
         * @param message
         *         message to log
         */
        void logW(String tag, String message);

        /**
         * Logs an info message
         *
         * @param tag
         *         tag for the message
         * @param message
         *         message to log
         */
        void logI(String tag, String message);

        /**
         * Logs an logThrowable
         *
         * @param tag
         *         tag for the message
         * @param throwable
         *         throwable to log
         */
        void logException(String tag, Throwable throwable);
    }
}
