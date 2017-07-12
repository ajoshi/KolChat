package biz.ajoshi.kolchat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
    public static Network network;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AsyncTask<Void, Void, Void> fml = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    if (network == null) {
                        network = new Network("corman", "cartoonskol1", true);
                    }

                    ChatManagerKotlin chatMgr = new ChatManagerKotlin(network);
                    chatMgr.readChat();
                    if (network.isLoggedIn()) {
                        chatMgr.post("/clan hey");
                    } else {
                        if (network.login()) {
                            chatMgr.post("/clan hey");
                        }
                    }
                   // network.postChat("/clan hey");
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    getSupportFragmentManager().beginTransaction().add(R.id.llist, new ChatListFragment(), "list frag").commit();
  //      fml.execute();
    }

public void getMessages() {
//    Retrofit retrofit = new Retrofit.Builder()
//            .addConverterFactory(JacksonConverterFactory.create())
//            .baseUrl("https://www.kingdomofloathing.com/")
//            .build();
//    ChatEndpoint service = retrofit.create(ChatEndpoint.class);
//
//
//    service.getMessages(System.currentTimeMillis()+"").enqueue(new Callback<List<ChatMessage>>() {
//        @Override
//        public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
//            Log.e("ajoshi", "retorfit sucks");
//        }
//
//        @Override
//        public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
//            Log.e("ajoshi", "retorfit still sucks");
//        }
//    });

}
}

/*

Maxlength is 200 as per web
//https://www.kingdomofloathing.com/submitnewchat.php?playerid=2129446&pwd=1da299f55ff15230c431e9816730af5d&graf=%2Fclan+hi
 String.format("submitnewchat.php?playerid=%s&pwd=%s&graf=%s&j=1", base.playerid, base.pwd, encodeChatMessage(message));


	public static final String getRightClickMenu()
	{
		if ( ChatPoller.rightClickMenu.equals( "" ) )
		{
			GenericRequest request = new GenericRequest( "lchat.php" );
			RequestThread.postRequest( request );
			int actionIndex = request.responseText.indexOf( "actions = {" );
			if ( actionIndex != -1 )
			{
				ChatPoller.rightClickMenu =
					request.responseText.substring( actionIndex, request.responseText.indexOf( ";", actionIndex ) + 1 );
			}
		}
		return ChatPoller.rightClickMenu;
	}


{"type":"event","msg":"Welcome back!  Away mode disabled.","time":1411683893}


See parseNewChat in ChatPoller.java


 lchat.php  for old chat



for tabbed chat, it seems

mchat.php
or
newchatmessages.php





newchatmessages.php?
if tabbed, j=1&
lasttime=timestampOfLastSeenMessage (unix?)

		if ( !tabbedChat )
		{
			newURLString.append( "&afk=" );
			newURLString.append( afk ? "1" : "0" );
		}


Honestly, only support tabbed chat
 */