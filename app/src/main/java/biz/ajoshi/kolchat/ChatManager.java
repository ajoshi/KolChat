package biz.ajoshi.kolchat;

import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import biz.ajoshi.kolchat.model.ServerChatMessage;
import biz.ajoshi.kolchat.util.StringUtil;

/**
 * Performs chat operations and maintains chat state. Meybe make singleton?
 */
public class ChatManager {
    // Used to make network calls
    private final Network network;
    // when the last chat message was seen
    long lastSeen = 0;
    public ChatManager(Network network) {
        this.network = network;
    }

    public void start() throws IOException {
        if(!network.isLoggedIn()) {
            if (!network.login()) {
                throw new IOException("Couldn't log in for magic reason");
            }
        }
        // now we're logged in
        read();

        List<ServerChatMessage> chatList = read();
        for (ServerChatMessage a : chatList) {
            Log.e("ajoshi", a.toString());
        }
                /*
        <a target=mainpane href="showplayer.php?who=2129446"><font color=blue><b>ajoshi (private):</b></font></a>
        <font color="blue">one</font><br>
        <a target=mainpane href="showplayer.php?who=2129446"><font color=blue><b>ajoshi (private):</b></font></a>
        <font color="blue">two</font><br>
        <!--lastseen:1442257967-->

        <font color=green>[newbie]</font>
        <b><a target=mainpane href="showplayer.php?who=2262317"><font color=black>ShanniBearStar</font></b></a>
        : Like how I could drink as I pleased in Scotland<br>
        <b><a target=mainpane href="showplayer.php?who=606649"><font color=black>Lexicon</font></b></a>
        : <a target=_blank href="http://kol.coldfront.net/thekolwiki/index.php/Category:Elves"><font color=blue>[link]</font></a> http:// kol.coldfront.net/ thekolwiki/ index.php/Category: Elves <i>brb, getting meat from my multis.</i><br>

        <!--lastseen:1442257988-->

         */

    }

    public List<ServerChatMessage> read() throws IOException {
        String chatResponse = network.readChat(lastSeen);
//        String commentText = xmlPullParser.getText();
//        if (commentText.startsWith("lastseen:")) {
//            String nextTimeStamp = commentText.substring(9);
//            lastSeen = Long.valueOf(nextTimeStamp);
//        }
        // kol returns each line delimimted by a <br>. So each <br> denotes a chat
        String[] chats = chatResponse.split("<br>");
        LinkedList<ServerChatMessage> chatList = new LinkedList<>();
        for (String chat : chats) {
            if (chat.charAt(1) == '!') {
                if (chat.startsWith("<!--lastseen:")) {
                    String nextTimeStamp = StringUtil.getBetweenTwoStrings(chat, "lastseen:", "-->");
                    lastSeen = Long.valueOf(nextTimeStamp);
                }
                continue;
            }

            // iterate through each chat message and create a ChatObject
            // low budget parsing
            String channel = null;
            int channelNameBeingIndex = chat.indexOf('[');
            if (channelNameBeingIndex < 20) { // else it's too late to be channelServer name
                channel = StringUtil.getBetweenTwoStrings(chat, "[", "]");
            }
            int indexOfIdstart = chat.indexOf("showplayer.php?who=");
            String userId = getHtmlWordAfter(chat, indexOfIdstart+19);

            int userIdIndex = chat.indexOf(userId);
            int endOfUserIdTag = chat.indexOf('>', userIdIndex);

            if (endOfUserIdTag == -1) {
                Log.e(",","");
                //<a target=mainpane href="showplayer.php?who=2129446"><font color=blue><b>ajoshi (private):</b></font></a> <font color="blue">sd</font>
            }
            String smallerChat = chat.substring(endOfUserIdTag);

            // this works great to get usernames for regualr chat
            //String userName = StringUtil.getBetweenTwoStrings(chat, userId+"\">", "</b>");

            // this also gets usernames for tp throwers, but includes extra </b>
            String userName = smallerChat.substring(1, smallerChat.indexOf("</a>"));
            if (userName.endsWith("</b>")) {
                userName = userName.substring(0, userName.length() - 4);
            }

            int colonIndex = smallerChat.indexOf(": ");
            String text = "";
            if (colonIndex == -1) {
                // it's a specail chat message like tp or snowball
                //<font color=green>
                // <a href='showplayer.php?who=2129446' target=mainpane class=nounder>
                // <font color=green>ajoshi</font></a>
                // <a href='campground.php' target=mainpane class=nounder>
                // <font color=green>has covered your Newbiesport&trade; tent with toilet paper.</font></a></font>
                int userNameIndex = smallerChat.indexOf(userName);
                int start = smallerChat.indexOf("<font", userNameIndex + userName.length());
                // +7 includs the closing </font> tag
                int end = smallerChat.indexOf("</font", userNameIndex + userName.length())+7;

                text = smallerChat.substring(start, end);
            } else {
                 text = smallerChat.substring(colonIndex+1);
            }

//            ServerChatMessage chatMessage = new ServerChatMessage(new User(userId, userName), new Channel(channelServer, channelServer), text);
//            chatList.add(chatMessage);
        }
        return chatList;
    }

    /**
     * Returns the string from startIndex to when a space, single, or double quote is encountered
     * will throw indexoutofboundsexception if startindex is bad. perform checks beforehand
     * @param fullString
     * @param startIndex
     * @return
     */
    public String getHtmlWordAfter(String fullString, int startIndex) {
        StringBuilder sb = new StringBuilder();
        int length = fullString.length();
        for (int c = startIndex; c < length; c++) {
            char currentChar = fullString.charAt(c);
            if (currentChar == ' ' || currentChar == '\'' || currentChar == '\"'){
                return sb.toString();
            }
            sb.append(currentChar);
        }
        return sb.toString();
    }

    /*
    <a target=mainpane href="showplayer.php?who=2129446">
    <font color=blue><b>ajoshi (private):</b></font></a> <font color="blue">sd</font>

    <font color=#66CC99>[hardcore]</font> <b><a target=mainpane href="showplayer.php?who=1775888"><font color=black><font color="#bc2">M</font><font color="#500">a</font><font color="#3ca">i</font><font color="#508">l</font><font color="#7b0">s</font> <font color="#632">B</font><font color="#66b">o</font><font color="#a92">w</font> <font color="#477">J</font><font color="#4ab">o</font><font color="#c0b">y</font></font></b></a>: it's kind of like gemelli's star thing in that it's a chat thing.<br><!--lastseen:1442778847-->
<font color=green><a href='showplayer.php?who=2129446' target=mainpane class=nounder><font color=green>ajoshi</font></a> <a href='campground.php' target=mainpane class=nounder><font color=green>has covered your Newbiesport&trade; tent with toilet paper.</font></a></font>
     <font color=green>[newbie]</font>
     <b><a target=mainpane href="showplayer.php?who=2363017">
     <font color=black>Pigrat</font></b></a>: New item of the month... full set of gear... (7 slot) NO TAT .... full set gives +1 stat per fight
     <br><b><a target=mainpane href="showplayer.php?who=2129446"><font color=#9999FF>ajoshi</font></b></a>: Hello all! <i>Ask me how to get FREE MR. AS!</i>
     <br><b><a target=mainpane href="showplayer.php?who=2491477"><font color=black>Jag2k2</font></b></a>: howdy <i>Mak me mod pleez?!</i>
     <br><!--lastseen:1442777192-->
     */

    /*
    <font color=green>[newbie]</font>
     <b><a target=mainpane href="showplayer.php?who=1544619">
     <font color=black>GroovyJ</font></b></a>: Hard to see how it could work well if you aren't to level 13 pretty quickly, though.<br>
     <b><a target=mainpane href="showplayer.php?who=2129446"><font color=#9999FF>ajoshi</font></b></a>: nobody appreciated my amazing joke :(<br>
     <b><a target=mainpane href="showplayer.php?who=2129446"><font color=#9999FF>ajoshi</font></b></a>: it was about the movie Predator <i>brb, getting meat from my multis.</i><br>
     <b><i><a target=mainpane href="showplayer.php?who=2129446"><font color="#9999FF">ajoshi</b></font></a> explains <i>All of my bans were unfair.</i></i><br>
     <!--lastseen:1442777887-->
     */
    public void post(String message) throws IOException {
        network.postChat(message);
    }

    public void post(String channel, String message) throws IOException {
        network.postChat(channel + " " + message);
    }

}
