package biz.ajoshi.kolchat.retrofit;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by ajoshi on 4/20/2017.
 */
public interface ChatEndpoint {
    @GET("newchatmessages.php")
    Call<List<ChatMessage>> getMessages(@Query("j") int isTabbedChat, @Query("afk") int isAfk,
                                        @Query("lasttime") String timeStamp);

    @GET("newchatmessages.php?j=1")
    Call<List<ChatMessage>> getMessages(@Query("lasttime") String timeStamp);

    @GET("newchatmessages.php?j=1")
    Call getMessagesBody(@Query("lasttime") String timeStamp);


    @GET("submitnewchat.php")
    Call<List<ChatMessage>> post(@Query("playerid") String playerId,
                                 @Query("pwd") String passwordHash,
                                 @Query("graf") String encodedMessage);
    @GET("login.php")
    Call<ChatMessage> login();

    @GET("login.php")
    Call<ChatMessage> login(@Query("loginid") String loginid,
               @Query("loginname") String username,
               @Query("password") String password);
}
