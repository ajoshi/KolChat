package biz.ajoshi.kolchat;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import biz.ajoshi.kolchat.model.LoggedInUser;
import biz.ajoshi.kolchat.model.User;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import android.text.TextUtils;

import static biz.ajoshi.kolchat.util.StringUtil.getBetweenTwoStrings;

/**
 * Class that makes network calls to KoL.
 *
 * <b>Does not spawn a thread</b>- that is the caller's responsibility.
 * Created by ajoshi on 5/9/2017.
 */
public class Network {
    private final String username;
    private final String password;
    private String playerid;
    private String pwdHash;
    private String phpSessId;
    private String awsCookie;
    private boolean loggedIn = false;
    private String chatpwd;
    private LoggedInUser currentUser;
    OkHttpClient client;

    public static final String ERROR = "error";
    public static final String BASE_URL = "https://www.kingdomofloathing.com";
    public static final String LOGIN_POSTFIX = "/login.php";
    public static final String MAINT_POSTFIX = "maint.php";
    public static final String IS_QUIET_MODIFIER = "%s/q";

    /**
     * Creates a Network object for the given user
     *
     * @param username
     * @param password
     */
    public Network(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Creates a Network object for the given user
     *
     * @param username
     * @param password
     */
    public Network(String username, String password, boolean isQuiet) {
        this(isQuiet ? String.format(IS_QUIET_MODIFIER, username) : username, password);
    }

    /**
     * Logs in
     *
     * @return true if login succeeded, else false
     *
     * @throws IOException
     *         if an exception occured
     */
    public boolean login() throws IOException {
        client = new OkHttpClient()
                .newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + LOGIN_POSTFIX)
                .build();

        // Apparently there is a challenge id to be extracted. I've never seen it
        //String challenge = "";
        // We need the redirect from login.php to get the loginid
        Response response = client.newCall(request).execute();
//        if (response.body() != null) {
//            String thisSucks = response.body().string();
//            String challengePrecursor = "<input type=hidden name=challenge value=";
//            int indexOfChallenge = thisSucks.indexOf(challengePrecursor);
//            if (indexOfChallenge != -1) {
//                int endOfChallenge = thisSucks.indexOf(" ", indexOfChallenge + challengePrecursor.length());
//                challenge = thisSucks.substring(indexOfChallenge + challengePrecursor.length(), endOfChallenge);
//            }
//        }

        String redirectLocation = response.header("location");
        if (redirectLocation != null && !redirectLocation.startsWith("/")) {
            // this seems to only happen during RO
            redirectLocation = '/' + redirectLocation;
            loggedIn = false;
            return false;
        }

        HttpUrl loginUrl = HttpUrl.parse(BASE_URL + redirectLocation);

        if (loginUrl == null) {
            return false;
        }
        loginUrl = loginUrl.newBuilder()
                           .addQueryParameter("loggingin", "Yup.")
                           .addQueryParameter("promo", "")
                           .addQueryParameter("mrstore", "")
                           .addQueryParameter("secure", "1")
                           .addQueryParameter("loginname", username)
                           .addQueryParameter("password", password)
                           .addQueryParameter("submitbutton", "Log+In")
                           .build();

        Request loginrequest = new Request.Builder()
                .url(loginUrl)
                .build();

        Response loginResponse = client.newCall(loginrequest).execute();

        if (loginResponse.networkResponse() == null) {
            return false;
        } else {
            List<String> cookies = loginResponse.networkResponse().headers("set-cookie");
            int cookieCount = cookies.size();
            for (int i = 0; i < cookieCount; i++) {
                String cookie = cookies.get(i);
                if (!TextUtils.isEmpty(cookie)) {
                    if (cookie.startsWith("PHPSESSID")) {
                        // we have the sessid!
                        phpSessId = getBetweenTwoStrings(cookie, "=", ";");
                    }
                    if (cookie.startsWith("AWSALB")) {
                        awsCookie = getBetweenTwoStrings(cookie, "=", ";");
                    }
                }
            }
            if (awsCookie != null && phpSessId != null) {
                String playerData = fetchPlayerData();
                if (playerData != null && pwdHash != null && playerid != null) {
                    // I guess we must have logged in!
                    loggedIn = true;
                }
            }
            return loggedIn;
        }
    }

    public void logout() {
        // right now just clear local state
        loggedIn = false;
        awsCookie = null;
        phpSessId = null;
        playerid = null;
        pwdHash = null;
    }

    /**
     * Fetches the passwordhash and the playerid for the current user. Maybe current channel as well?
     *
     * @return
     *
     * @throws IOException
     */
    public String fetchPlayerData() throws IOException {
        Request readchatRequest = new Request.Builder()
                .url(BASE_URL + "/mchat.php?for=ajoshiChatApp")
                .header("cookie", String.format("PHPSESSID=%s; AWSALB=%s", phpSessId, awsCookie))
                .header("referer", "https://www.kingdomofloathing.com/lchat.php")
                .build();
        Response chatResponse = client.newCall(readchatRequest).execute();
        ResponseBody body = chatResponse.body();
        if (body == null) {
            return null;
        } else {
            String postResponse = body.string();
            playerid = getBetweenTwoStrings(postResponse, "playerid = ", ",");
            pwdHash = getBetweenTwoStrings(postResponse, "pwdhash = \"", "\"");
            String mainChannel = getBetweenTwoStrings(postResponse, "active: \"", "\"");
            chatpwd = getBetweenTwoStrings(postResponse, "setCookie('chatpwd', winW, ", ",");
            currentUser = new LoggedInUser(new User(playerid, username), pwdHash, mainChannel);
       /* $cw,
                $inp,
                $tabs,
                CHMAX = 20,         // ah, max displayable channel size
                chpointer = -1,
                chcurrent = null,
                delay = 3000,       requery time
                lastdelay = 0,
                chistory = [],
        payingattention = true,
                timer,
                lastrequest,
                KEYS = {
                        0: 'shiftKey',
                1: 'ctrlKey',
                2: 'altKey',
                3: 'notAKey'
        },
        opts = {
                event: 'active',
        public: 'own',
        private: 'own',
                updatetitle: 0,
                showtimes: 1,     Show timestamps
                marklast: true,
                active: "games",
                modifier: 0,
                unreadbadges: 1,   Show unread badges on tabs
                colortabs: 0,   Turn tabs pink when they have unread commands
                channeltag: 0,   maybe Combine all channels in the main tab (only split out private commands)
                alwayswho: 0,    Auto-Perform /who when a channel tab opens
                hmcsplit: 1,
                tabsonbottom: 0,
                buffer: 500,
                foo: 1

        },
        todo = [],
        playerid = 2239681,
                pwdhash = "8060f6a";
                */
            return postResponse;
        }
    }

    public LoggedInUser getCurrentUser() {
        return currentUser;
    }

    /**
     * URL encodes and posts a message to chat
     *
     * @param message
     *         Message to be posted
     *
     * @return server response in string form
     *
     * @throws IOException
     */
    public String postChat(String message) throws IOException {
        Request readchatRequest = new Request.Builder().get()
                                                       .url(String.format(BASE_URL +
                                                                          "/submitnewchat.php?for=ajoshiChatApp&playerid=%s&pwd=%s&graf=%s&j=1&format=php",
                                                                          playerid,
                                                                          pwdHash,
                                                                          URLEncoder.encode(message, "UTF-8")))
                                                       .addHeader("cookie",
                                                                  String.format("PHPSESSID=%s; AWSALB=%s; chatpwd=%s",
                                                                                phpSessId,
                                                                                awsCookie,
                                                                                chatpwd))
                                                       .addHeader("referer",
                                                                  "https://www.kingdomofloathing.com/mchat.php")
                                                       .addHeader("Connection", "close")
                                                       .addHeader("X-Requested-With", "XMLHttpRequest")
                                                       .build();
        Call call = client.newCall(readchatRequest);
        Response chatResponse = call.execute();
        String redirectLocation = chatResponse.header("location");
        if (redirectLocation != null &&
            (redirectLocation.contains(MAINT_POSTFIX) || (redirectLocation.contains("login.php")))) {
            // RO time or we got logged out
            return null;
        }
        String response = null;
        ResponseBody body = chatResponse.body();
        if (body != null) {
            response = body.source().readUtf8();
        }
        chatResponse.close();
        return response;
    }

    /**
     * Requests new chat events since the last time. We can't use retrofit at all for chat.
     *
     * @param timeStamp
     *
     * @return String response
     *
     * @throws IOException
     */
    public String readChat(long timeStamp) throws IOException {
        // chat response is always html
        // chat command response (which is returned from the GET) is json containing html
        Request readchatRequest = new Request.Builder()
                .url(String.format(Locale.US,
                                   // <a target=mainpane href="showplayer.php?who=2129446">
                                   // <font color=blue><b>ajoshi (private):</b></font></a>
                                   // <font color="blue">yolo</font><br><!--lastseen:1442257857-->
                                   "https://www.kingdomofloathing.com/newchatmessages.php?lasttime=%d&j=1&aa=0.5901808745871704&format=json",
                                   timeStamp))
                .header("cookie", String.format("PHPSESSID=%s; AWSALB=%s", phpSessId, awsCookie))
                .header("referer", "https://www.kingdomofloathing.com/mchat.php")
                .build();
        Response chatResponse = client.newCall(readchatRequest).execute();
                    /*
                    Request{method=GET, url=https://www.kingdomofloathing.com/newchatmessages.php?afk=0&lasttime=1448571291&playerid=2239681&pwd=a2f587e8468f7b81657ecaadcbf1cdd3, tag=null}
:/newchatmessages.php?aa=0.3231651053251192&j=1&lasttime=1448571209
                    /submitnewchat.php?playerid=2129446&pwd=919e0c2b6de6c757c9ac45291e8eeb34&graf=%2Fdread+b&j=1
                    /submitnewchat.php?playerid=2239681&pwd=2fec7beacb07e4c832e06a78cb4593a0&graf=%2Fgames+Hi+Butts&j=1
                    maybe https://www.kingdomofloathing.com/lchat.php
                    can help us get the chatpwd?

                    PHPSESSID=n810qdbjnosr6lstg1nrlu50j7;
                     _ga=GA1.2.905624454.1494297907;
                      _gid=GA1.2.654466491.1494306989;
                      chatpwd=171;
                       AWSALB=60M5Fjdcg/jgLRbdoZ3DHM+g7b1v2AFBQtp2PpptlDfAS2+qeHiRYzkpyiQDzBjvpWeIaXpJzDqtlmbHyIo6df+6l+JHPMjKjqP2mzQUsH7rIxLVCAVZ0lSRZzfb
                     */

        String redirectLocation = chatResponse.header("location");
        if (redirectLocation != null && redirectLocation.contains(MAINT_POSTFIX)) {
            // RO time
            return null;
        }
        ResponseBody body = chatResponse.body();
        if (body != null) {
            return body.string();
        }
        return null;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
}
