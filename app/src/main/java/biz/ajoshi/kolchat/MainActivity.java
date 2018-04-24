package biz.ajoshi.kolchat;

import java.util.concurrent.Callable;

import com.crashlytics.android.Crashlytics;

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
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    public static final String TAG_CHAT_DETAIL_FRAG = "chat frag";
    public static final String TAG_CHAT_LIST_FRAG = "list frag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Change the theme if needed. Must be done before setContentView
        Themer themer = new Themer();
        setTheme(themer.getThemeId());

        setContentView(R.layout.activity_main);
        if (ChatSingleton.INSTANCE.isLoggedIn()) {
            // getnetwork can not return null if logged in
            Crashlytics.setUserIdentifier(ChatSingleton.INSTANCE.getNetwork().getCurrentUser().getPlayer().getName());
            // we're logged in
            Activity activity = MainActivity.this;
            Intent serviceIntent = new Intent(activity, ChatBackgroundService.class);
            serviceIntent.putExtra(ChatServiceKt.EXTRA_POLL_INTERVAL_IN_MS, 2000);
            activity.startService(serviceIntent);
        } else {
            // we're not logged in, so just open up the login activity
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        if(ChatSingleton.INSTANCE.isLoggedIn()) {
            Intent increasePollTimeout = new Intent(this, ChatBackgroundService.class);
            // activity is gone, increase poll interval to 1 minute
            increasePollTimeout.putExtra(ChatServiceKt.EXTRA_POLL_INTERVAL_IN_MS, 60000);
            startService(increasePollTimeout);
        }
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
