package biz.ajoshi.kolchat;

import java.util.concurrent.Callable;

import biz.ajoshi.kolchat.persistence.ChatChannel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static final String TAG_CHAT_DETAIL_FRAG = "chat frag";
    public static final String TAG_CHAT_LIST_FRAG = "list frag";

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

        Fragment listFrag = getSupportFragmentManager().findFragmentByTag(TAG_CHAT_LIST_FRAG);
        if (listFrag == null) {
            Fragment chatMessageFrag = new ChatChannelListFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.llist, chatMessageFrag, TAG_CHAT_LIST_FRAG)
                                       .commit();
        }
    }

    public void onChannelNameClicked(ChatChannel channel) {
        Fragment chatDetailFrag = new ChatMessageFrag();
        Bundle b = new Bundle();
        b.putString(ChatMessageFragKt.EXTRA_CHANNEL_ID, channel.getId());
        b.putString(ChatMessageFragKt.EXTRA_CHANNEL_NAME, channel.getName());
        b.putBoolean(ChatMessageFragKt.EXTRA_CHANNEL_IS_PRIVATE, channel.isPrivate());
        chatDetailFrag.setArguments(b);
        getSupportFragmentManager().beginTransaction().replace(R.id.llist, chatDetailFrag, TAG_CHAT_DETAIL_FRAG).addToBackStack(TAG_CHAT_DETAIL_FRAG).commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
 //       Intent stopService = new Intent(this, ChatService.class);
//        stopService(stopService);
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
