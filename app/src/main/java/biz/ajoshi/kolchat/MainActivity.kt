package biz.ajoshi.kolchat

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import biz.ajoshi.commonutils.StringUtilities
import biz.ajoshi.kolchat.chat.*
import biz.ajoshi.kolchat.persistence.chat.ChatChannel
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.ContentViewEvent

class MainActivity : AppCompatActivity() {

    internal var toolbar: Toolbar? = null
    internal val navController = NavController(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Change the theme if needed. Must be done before setContentView
        val themer = Themer()
        setTheme(themer.getThemeId())

        setContentView(R.layout.activity_main)
        if (ChatSingleton.isLoggedIn()) {
            // getnetwork can not return null if logged in so ignore bad static analysis
            Crashlytics.setUserIdentifier(ChatSingleton.network!!.currentUser.player.name)
            // we're logged in
            val activity = this
            val serviceIntent = Intent(activity, ChatBackgroundService::class.java)
            serviceIntent.putExtra(EXTRA_POLL_INTERVAL_IN_MS, 2000)
            serviceIntent.putExtra(EXTRA_MAIN_ACTIVITY_COMPONENTNAME, ComponentName(this, javaClass))
            activity.startService(serviceIntent)
        } else {
            // we're not logged in, so just open up the login activity
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val listFrag = supportFragmentManager.findFragmentByTag(TAG_CHAT_LIST_FRAG)
        if (listFrag == null) {
            val chatMessageFrag = ChatChannelListFragment()
            supportFragmentManager.beginTransaction().add(R.id.llist, chatMessageFrag, TAG_CHAT_LIST_FRAG)
                    .commit()
        }
//        navController.setGraph(R.navigation.nav_graph)
    }

    override fun onSupportNavigateUp()
            = findNavController(R.id.llist).navigateUp()

    /**
     * Called when a channel name is tapped
     * TODO extract to an interface
     *
     * @param channel ChatChannel object describing the channel that was opened
     */
    fun onChannelNameClicked(channel: ChatChannel) {
        val b = Bundle()
        b.putString(EXTRA_CHANNEL_ID, channel.id)
        b.putString(EXTRA_CHANNEL_NAME, channel.name)
        b.putBoolean(EXTRA_CHANNEL_IS_PRIVATE, channel.isPrivate)
//        navController.navigate(R.id.nav_chat_message, b)

       // Go back to this if nav arch is as half baked as it seems
        val chatDetailFrag = ChatMessageFrag()
        chatDetailFrag.arguments = b
        supportFragmentManager.beginTransaction().replace(R.id.llist, chatDetailFrag, TAG_CHAT_DETAIL_FRAG)
                .addToBackStack(TAG_CHAT_DETAIL_FRAG).commit()
        if (toolbar != null) {
            // should crash on line 66 if this happens, honestly
            toolbar!!.title = getPlaintextForHtml(channel.name)
        }
        Answers.getInstance().logContentView(ContentViewEvent()
                .putContentName("Channel detail opened")
                .putContentId(if (channel.isPrivate) "PM" else channel.name))

    }

    /**
     * Returns plaintext representation of html. So "**Hi**" would return "Hi"
     *
     * @param html
     * string containing html
     *
     * @return string without html
     */
    private fun getPlaintextForHtml(html: String): String {
        return StringUtilities.getHtml(html).toString()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val fragment = supportFragmentManager.findFragmentById(R.id.llist) as BaseFragment
        if (toolbar != null) {
            toolbar!!.title = getPlaintextForHtml(fragment.getTitle())
        }

    }

    public override fun onDestroy() {
        super.onDestroy()
        if (ChatSingleton.isLoggedIn()) {
            val increasePollTimeout = Intent(this, ChatBackgroundService::class.java)
            // activity is gone, increase poll interval to 1 minute
            increasePollTimeout.putExtra(EXTRA_POLL_INTERVAL_IN_MS, 60000)
//            startService(increasePollTimeout)
            stopService(increasePollTimeout)
            ChatJob.scheduleJob(ComponentName(this, javaClass))
        }
    }

    fun getMessages() {
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

    companion object {
        val TAG_CHAT_DETAIL_FRAG = "chat frag"
        val TAG_CHAT_LIST_FRAG = "list frag"
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
