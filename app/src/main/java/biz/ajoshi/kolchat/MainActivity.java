package biz.ajoshi.kolchat;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return ChatSingleton.INSTANCE.loginIfNeeded("a", "b", true);
            }
        }).subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new Consumer<Boolean>() {
                      @Override
                      public void accept(Boolean success) throws Exception {
                          if (success) {
                              Activity activity = MainActivity.this;
                              Intent serviceIntent = new Intent(activity, ChatService.class);
                              activity.startService(serviceIntent);
                          } else {
                              // notify the ui that we failed to log in
                          }
                      }
                  });
        Bundle b = new Bundle();
        b.putString(ChatMessageFragmentKt.EXTRA_CHANNEL_ID, "games");
        b.putString(ChatMessageFragmentKt.EXTRA_CHANNEL_NAME, "games");
        b.putBoolean(ChatMessageFragmentKt.EXTRA_CHANNEL_IS_PRIVATE, false);
        Fragment chatDetailFrag = new ChatListFrag();
        chatDetailFrag.setArguments(b);
        getSupportFragmentManager().beginTransaction().add(R.id.llist, chatDetailFrag, "list frag").commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        Intent stopService = new Intent(this, ChatService.class);
        stopService(stopService);
    }

    public void getMessages() {
//    Retrofit retrofit = new Retrofit.Builder()
//            .addConverterFactory(JacksonConverterFactory.create())
//            .baseUrl("https://www.kingdomofloathing.com/")
//            .build();
//    ChatEndpoint service = retrofit.create(ChatEndpoint.class);
//
//
//    service.getMessages(System.currentTimeMillis()+"").enqueue(new Callback<List<ServerChatMessage>>() {
//        @Override
//        public void onResponse(Call<List<ServerChatMessage>> call, Response<List<ServerChatMessage>> response) {
//            Log.e("ajoshi", "retorfit sucks");
//        }
//
//        @Override
//        public void onFailure(Call<List<ServerChatMessage>> call, Throwable t) {
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
