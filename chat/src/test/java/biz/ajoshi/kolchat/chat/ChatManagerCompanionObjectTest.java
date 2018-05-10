package biz.ajoshi.kolchat.chat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChatManagerCompanionObjectTest {
    @Test
    public void replaceChatEffectImagesWithEmojis_skullImage_replacedByEmoji() {
        // skull image should be replaced by skull emoji
        String stringWithImages = "<img src=\"https://s3.amazonaws.com/images.kingdomofloathing.com/otherimages/12x12skull.gif\" height=\"12\" width=\"12\" />";
        assertEquals("☠", ChatManager.Companion.replaceChatEffectImagesWithEmojis(stringWithImages));
    }

    @Test
    public void replaceChatEffectImagesWithEmojis_heartImage_replacedByEmoji() {
        // heart image should be replaced by heart emoji
        String stringWithImages = "a<img src=\"https://s3.amazonaws.com/images.kingdomofloathing.com/otherimages/12x12heart.gif\" height=\"12\" width=\"12\" />a";
        assertEquals("a❤a", ChatManager.Companion.replaceChatEffectImagesWithEmojis(stringWithImages));
    }

    @Test
    public void replaceChatEffectImagesWithEmojis_otherImage_notreplacedByEmoji() {
        // some other image should not be replaced by anything
        String stringWithImages = "a<img src=\"https://s3.amazonaws.com/images.kingdomofloathing.com/otherimages/12x12something.gif\" height=\"12\" width=\"12\" />a";
        assertEquals(stringWithImages, ChatManager.Companion.replaceChatEffectImagesWithEmojis(stringWithImages));
    }

    @Test
    public void getChatString_pmMessage_formattedCorrectly() {
        String userEnteredString = "input";
        String username = "ajoshi";
        assertEquals(String.format("/w %s %s", username, userEnteredString),
                     ChatManager.Companion.getChatString(userEnteredString, username, true));
    }

    @Test
    public void getChatString_groupMessage_startsWithChannelName() {
        String userEnteredString = "input";
        String channelName = "clan";
        assertEquals(String.format("/%s %s", channelName, userEnteredString),
                     ChatManager.Companion.getChatString(userEnteredString, channelName, false));
    }

    @Test
    public void getChatString_chatCommands_shouldBeUnchangedInGroupChat() {
        // chat messages should normally go through untouched by us because hopefully the user knows what they're doing
        String userEnteredString = "/input";
        String channelName = "clan";
        assertEquals(userEnteredString, ChatManager.Companion.getChatString(userEnteredString, channelName, false));
    }

    @Test
    public void getChatString_chatCommands_shouldBeUnchangedInPm() {
        // chat messages should normally go through untouched by us because hopefully the user knows what they're doing
        String userEnteredString = "/input";
        String channelName = "clan";
        assertEquals(userEnteredString, ChatManager.Companion.getChatString(userEnteredString, channelName, true));
    }

    @Test
    public void getChatString_meMessageInPm_isIgnored() {
        // can't use /me in PMs
        String userEnteredString = "/em hi";
        String username = "ajoshi";
        assertEquals(String.format("/w %s %s", username, userEnteredString),
                     ChatManager.Companion.getChatString(userEnteredString, username, true));
    }

    @Test
    public void getChatString_emMessageInGroupChat_formattedCorrectly() {
        // /em should NOT be untouched by us else it will only ever post to default chat room
        String userEnteredString = "/em hi";
        String channelName = "clan";
        assertEquals(String.format("/%s %s", channelName, userEnteredString),
                     ChatManager.Companion.getChatString(userEnteredString, channelName, false));
    }

    @Test
    public void getChatString_meMessageInGroupChat_formattedCorrectly() {
        // /me should NOT be untouched by us else it will only ever post to default chat room
        String userEnteredString = "/me hi";
        String channelName = "clan";
        assertEquals(String.format("/%s %s", channelName, userEnteredString),
                     ChatManager.Companion.getChatString(userEnteredString, channelName, false));
    }

    @Test
    public void getChatString_whoInGroupChat_formattedCorrectly() {
        // /who should be changed to "/who channelname"
        String userEnteredString = "/who";
        String channelName = "clan";
        assertEquals(String.format("%s %s", userEnteredString, channelName),
                     ChatManager.Companion.getChatString(userEnteredString, channelName, false));
    }

    @Test
    public void getChatString_whoInPm_formattedCorrectly() {
        // /who should be changed to "/who username" EVEN THOUGH that doesn't work since that's how the web chat does it
        String userEnteredString = "/who";
        String channelName = "ajoshi";
        assertEquals(String.format("%s %s", userEnteredString, channelName),
                     ChatManager.Companion.getChatString(userEnteredString, channelName, true));
    }
}
