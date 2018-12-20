package biz.ajoshi.kolnetwork;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import biz.ajoshi.commonutils.Logg;
import biz.ajoshi.commonutils.StringUtilities;
import biz.ajoshi.kolnetwork.model.LoggedInUser;
import biz.ajoshi.kolnetwork.model.NetworkResponse;
import biz.ajoshi.kolnetwork.model.NetworkStatus;
import biz.ajoshi.kolnetwork.model.User;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import android.text.TextUtils;

/**
 * Class that makes network calls to KoL.
 *
 * <b>Does not spawn a thread</b>- that is the caller's responsibility.
 * Created by ajoshi on 5/9/2017.
 */
public class Network {
    public static final String APP_NAME = "ajoshiChatApp";
    private static final String APP_NAME_QUERYPARAM = "for="+APP_NAME;

    private final String username;
    private final String password;
    private final boolean isQuiet;
    private String playerid;
    private String pwdHash;
    private String phpSessId;
    private String awsCookie;
    private boolean loggedIn = false;
    private boolean isRollover = false;
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
        this(username, password, true);
    }

    /**
     * Creates a Network object for the given user
     *
     * @param username
     * @param password
     */
    public Network(String username, String password, boolean isQuiet) {
        this.username = username;
        this.password = password;
        this.isQuiet = isQuiet;
    }

    /**
     * Logs in
     *
     * @return true if login succeeded, else false
     *
     * @throws IOException
     *         if a exception occured
     */
    public NetworkResponse login() throws IOException {
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
            //  redirectLocation = '/' + redirectLocation;
            return onRollover();
        }

        HttpUrl loginUrl = HttpUrl.parse(BASE_URL + redirectLocation);

        if (loginUrl == null) {
            return new NetworkResponse("Redirected to invalid url", NetworkStatus.FAILURE);
        }
        loginUrl = loginUrl.newBuilder()
                           .addQueryParameter("loggingin", "Yup.")
                           .addQueryParameter("promo", "")
                           .addQueryParameter("mrstore", "")
                           .addQueryParameter("secure", "1")
                           .addQueryParameter("loginname",
                                              isQuiet ? String.format(IS_QUIET_MODIFIER, username) : username)
                           .addQueryParameter("password", password)
                           .addQueryParameter("submitbutton", "Log+In")
                           .build();

        Request loginrequest = new Request.Builder()
                .url(loginUrl)
                .build();

        Response loginResponse = client.newCall(loginrequest).execute();

        if (loginResponse.networkResponse() == null) {
            return new NetworkResponse("Maybe RO, not sure. login response null", NetworkStatus.FAILURE);
        } else {
            List<String> cookies = loginResponse.networkResponse().headers("set-cookie");
            int cookieCount = cookies.size();
            for (int i = 0; i < cookieCount; i++) {
                String cookie = cookies.get(i);
                if (!TextUtils.isEmpty(cookie)) {
                    if (cookie.startsWith("PHPSESSID")) {
                        // we have the sessid!
                        phpSessId = StringUtilities.getBetweenTwoStrings(cookie, "=", ";");
                    }
                    if (cookie.startsWith("AWSALB")) {
                        awsCookie = StringUtilities.getBetweenTwoStrings(cookie, "=", ";");
                    }
                }
            }
            if (awsCookie != null && phpSessId != null) {
                String playerData = fetchPlayerData();
                if (playerData != null && pwdHash != null && playerid != null) {
                    // I guess we must have logged in!
                    loggedIn = true;
                    isRollover = false;
                }
            }
            return loggedIn ? new NetworkResponse("") : new NetworkResponse(NetworkStatus.FAILURE);
        }
    }

    public void logout() {
        // right now just clear local state
        loggedIn = false;
        playerid = null;
        halfLogout();
    }

    /**
     * Resets some local state so it can be refetched if stale
     */
    private void halfLogout() {
        awsCookie = null;
        phpSessId = null;
        pwdHash = null;
    }

    private void loginIfNeeded() throws IOException {
        if (phpSessId == null) {
            login();
        }
    }

    /**
     * Fetches the passwordhash and the playerid for the current user. Maybe current channel as well?
     *
     * @return
     *
     * @throws IOException
     */
    private String fetchPlayerData() throws IOException {
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
            playerid = StringUtilities.getBetweenTwoStrings(postResponse, "playerid = ", ",");
            pwdHash = StringUtilities.getBetweenTwoStrings(postResponse, "pwdhash = \"", "\"");
            String mainChannel = StringUtilities.getBetweenTwoStrings(postResponse, "active: \"", "\"");
            chatpwd = StringUtilities.getBetweenTwoStrings(postResponse, "setCookie('chatpwd', winW, ", ",");
            if (postResponse.isEmpty() || playerid == null) {
                // TODO chat response comes back as null sometimes. concurrent logins are causing issues
                if (login().isSuccessful()) {
                    // if we can log in again to refresh the hash then do so and try again
                    return fetchPlayerData();
                }
                onExpiredHash();
                return null;
            }
                /*
                2018-06-03 21:52:28.427 14205-14364/biz.ajoshi.kolchat I/ChatSingleton: logging in as corman
2018-06-03 21:52:28.707 14205-14320/biz.ajoshi.kolchat I/JobManager: Found pending job request{id=108, tag=chat_job_tag, transient=false}, canceling
2018-06-03 21:52:28.899 14205-14694/biz.ajoshi.kolchat I/JobExecutor: Executing request{id=108, tag=chat_job_tag, transient=false}, context PlatformJobService
2018-06-03 21:52:28.915 14205-14695/biz.ajoshi.kolchat I/ChatSingleton: logging in as corman
2018-06-03 21:52:29.721 14205-14364/biz.ajoshi.kolchat E/Network: failed call
                 */

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
            isRollover = false;
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
    public NetworkResponse postChat(String message) throws IOException {
        return postUrl(BASE_URL +
                       "/submitnewchat.php?playerid=%s&pwd=%s&graf=%s&j=1&format=php", message);
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
    public NetworkResponse readChat(long timeStamp) throws IOException {
        return getUrl(String.format(Locale.US,
                                    // <a target=mainpane href="showplayer.php?who=2129446">
                                    // <font color=blue><b>ajoshi (private):</b></font></a>
                                    // <font color="blue">yolo</font><br><!--lastseen:1442257857-->
                                    BASE_URL +
                                    "/newchatmessages.php?lasttime=%d&j=1&aa=0.5901808745871704&format=json",
                                    timeStamp));
    }

    private NetworkResponse onExpiredHash() {
        // pwdhash is no longer valid. Either web login happened or RO
        Logg.w("Network", "Pwdhash expired, logging out");
        halfLogout();
        return new NetworkResponse(NetworkStatus.INVALID_HASH);
    }


    private NetworkResponse onRollover() {
        Logg.w("Network", "Rollover time!");
        // semi-logout so we can log back in automatically
        halfLogout();
        isRollover = true;
        return new NetworkResponse(NetworkStatus.ROLLOVER);
    }


    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Makes an arbitrary KoL related network call (GET) and returns the response
     * @param url full url (https) to get data from
     * @return the server response
     */
    public NetworkResponse getUrl(@NotNull String url) throws IOException {
        loginIfNeeded();
        // chat response is always html
        // chat command response (which is returned from the GET) is json containing html
        Request readchatRequest = new Request.Builder()
                .url(sanitizeUrlForKol(url))
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
        if (redirectLocation != null) {
            return onRollover();
        }
        ResponseBody body = chatResponse.body();
        if (body != null) {
            String bodyString = body.string();
            if (bodyString != null && !bodyString.isEmpty()) {
                isRollover = false;
                return new NetworkResponse(bodyString);
            }
        }
        if (login().isSuccessful()) {
            Logg.w("Network", "Pwdhash expired; reloggedin");
            // if we can log in again to refresh the hash then do so and try again
            return getUrl(url);
        }
        // we always get a response, even when chat is empty. So if response is empty, something has gone awry
        return onExpiredHash();
    }

    /**
     * Makes an arbitrary kol related POST and returns the response
     * @param url url to make the post to
     * @param body post needs a body (non null)
     * @return network response resulting from this call
     * @throws IOException
     */
    public NetworkResponse postUrl(String url, @NonNull String body) throws IOException {
        loginIfNeeded();
        Request sendChatRequest = new Request.Builder().get()
                                                       .url(String.format(sanitizeUrlForKol(url),
                                                                          playerid,
                                                                          pwdHash,
                                                                          URLEncoder.encode(body, "UTF-8")))
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
        Call call = client.newCall(sendChatRequest);
        Response chatResponse = call.execute();
        String redirectLocation = chatResponse.header("location");
        if (redirectLocation != null &&
            (redirectLocation.contains(MAINT_POSTFIX) || (redirectLocation.contains("login.php")))) {
            // RO time or we got logged out
            return onRollover();
        }
        String response = null;
        ResponseBody responseBody = chatResponse.body();
        if (responseBody != null) {
            response = responseBody.source().readUtf8();
        }
        chatResponse.close();
        if (response == null) {
            if (login().isSuccessful()) {
                // if we can log in again to refresh the hash then do so and try again
                return postUrl(url, body);
            }
            return onExpiredHash();
        }
        isRollover = false;
        return new NetworkResponse(response);
    }

    /**
     * Adds the AjoshiChatApp param to all network calls so it's easy to isolate
     * @param url url we're making the network call for
     * @return url with "for=ajoshichatapp" added to the end
     */
    private String addAppIdToUrl(String url) {
        if (url.contains("?")) {
            url = url + "?" + APP_NAME_QUERYPARAM;
        } else {
            url = url + "&" + APP_NAME_QUERYPARAM;
        }
        return url;
    }

    /**
     * Ensures that urls sent in are absolute URLs (will convert relative to absolute) and they have the app Id appended
     * @param url url to send in to kol servers (relative or absolute)
     * @return absolute url that's absolutely correct
     */
    private String sanitizeUrlForKol(String url) {
        url = addAppIdToUrl(url);
        if (!url.startsWith("http")){
            if (url.startsWith("/")) {
                url = BASE_URL + url;
            } else {
                url = BASE_URL + "/" + url;
            }
        }
        return url;
    }
}
