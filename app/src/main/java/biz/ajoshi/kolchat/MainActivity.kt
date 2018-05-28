package biz.ajoshi.kolchat

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import biz.ajoshi.commonutils.Logg
import biz.ajoshi.commonutils.StringUtilities
import biz.ajoshi.commonutils.getDefaultColor
import biz.ajoshi.kolchat.accounts.KolAccountManager
import biz.ajoshi.kolchat.chat.*
import biz.ajoshi.kolchat.chat.view.ChatChannelAdapter
import biz.ajoshi.kolchat.persistence.chat.ChatChannel
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent

const val action_navigate_to_chat_detail = "biz.ajoshi.kolchat.MainActivity.ACTION_NAVIGATE_TO_CHAT_DETAIL"

class MainActivity : AppCompatActivity(), ChatChannelAdapter.ChannelClickListener {
    internal var toolbar: Toolbar? = null
    internal var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Change the theme if needed. Must be done before setContentView
        val themer = Themer()
        setTheme(themer.getThemeId())

        setContentView(R.layout.activity_main)
        if (launchLoginActivityIfLoggedOut()) {
            // getnetwork can not return null if logged in so ignore bad static analysis
            Crashlytics.setUserIdentifier(ChatSingleton.network!!.currentUser.player.name)
            // we're logged in
            val serviceIntent = Intent(this, ChatBackgroundService::class.java)
            serviceIntent.putExtra(EXTRA_POLL_INTERVAL_IN_MS, 2000)
            serviceIntent.putExtra(EXTRA_MAIN_ACTIVITY_COMPONENTNAME, ComponentName(this, javaClass))
            startService(serviceIntent)
        } else {
            return
        }
        // analytics- log the source of the launch intent
        when (intent.action) {
        // launched by os
            Intent.ACTION_MAIN ->
                Answers.getInstance().logCustom(CustomEvent(EVENT_NAME_APP_LAUNCH)
                        .putCustomAttribute(EVENT_ATTRIBUTE_SOURCE, "Launcher"))
        // launched by the notification for a chat message
            action_navigate_to_chat_detail ->
                Answers.getInstance().logCustom(CustomEvent(EVENT_NAME_APP_LAUNCH)
                        .putCustomAttribute(EVENT_ATTRIBUTE_SOURCE, "Notification: chat detail"))
        // launched by login screen (or something else?)
        // this should tell me if users are logging in more than they should
            else -> Answers.getInstance().logCustom(CustomEvent(EVENT_NAME_APP_LAUNCH)
                    .putCustomAttribute(EVENT_ATTRIBUTE_SOURCE, "Login/Unknown"))
        }
        // set up toolbar
        toolbar = findViewById(R.id.toolbar)
        toolbar?.setTitleTextColor(getDefaultColor(android.R.color.white))
        setSupportActionBar(toolbar)

        // stop the once-every-15-minutes polling job
        ChatJob.stopJob()

        // set up nav graph
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.llist)
        navController = navHostFragment.findNavController()
        navController?.setGraph(R.navigation.nav_graph)
        navController?.currentDestination?.label = getString(R.string.app_name)
    }

    override fun onResume() {
        super.onResume()
        // check to see if we're still logged in (just in case)
        launchLoginActivityIfLoggedOut()
    }

    override fun onSupportNavigateUp() = findNavController(R.id.llist).navigateUp()

    override fun onBackPressed() {
        super.onBackPressed()
        // val fragment = supportFragmentManager.findFragmentById(R.id.llist) as NavHostFragment
        toolbar?.title = getPlaintextForHtml("" + navController?.currentDestination?.label)
    }

    public override fun onDestroy() {
        super.onDestroy()
        // launch the background polling service if still logged in
        if (ChatSingleton.isLoggedIn()) {
            Logg.i("MainActivity", "destroying activity and triggering background poll service")
            val increasePollTimeout = Intent(this, ChatBackgroundService::class.java)
            // activity is gone, increase poll interval to 1 minute
            increasePollTimeout.putExtra(EXTRA_POLL_INTERVAL_IN_MS, 60000)
//            startService(increasePollTimeout)  right now we just stop the service. Play around with options later
            stopService(increasePollTimeout)

            val currentUserName = ChatSingleton.network?.currentUser?.player?.name
            currentUserName?.let {
                val account = KolAccountManager(this)
                val currentUserAcct = account.getAccount(currentUserName)
                currentUserAcct?.let {
                    ChatJob.scheduleJob(
                            ComponentName(this, javaClass),
                            it.username,
                            it.password)
                }
            }
        }
    }

    /**
     * Called when a channel name is tapped
     *
     * @param channel ChatChannel object describing the channel that was opened
     */
    override fun onChannelClicked(channel: ChatChannel) {
        val b = Bundle()
        b.putString(EXTRA_CHANNEL_ID, channel.id)
        b.putString(EXTRA_CHANNEL_NAME, channel.name)
        b.putBoolean(EXTRA_CHANNEL_IS_PRIVATE, channel.isPrivate)
        navController?.navigate(R.id.nav_chat_message, b)
        navController?.currentDestination?.label = channel.name

        // Go back to this if nav arch is as half baked as it seems
//        val chatDetailFrag = ChatMessageFrag()
//        chatDetailFrag.arguments = b
//        supportFragmentManager.beginTransaction().replace(R.id.llist, chatDetailFrag, TAG_CHAT_DETAIL_FRAG)
//                .addToBackStack(TAG_CHAT_DETAIL_FRAG).commit()
        if (toolbar != null) {
            // should crash on line 66 if this happens, honestly
            toolbar!!.title = getPlaintextForHtml(channel.name)
        }
        Logg.i("MainActivity", if (channel.isPrivate) {
            "Channel detail opened"
        } else {
            "PM detail opened"
        })
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

    /**
     * Launches the login activity if the user is logged out
     * @return true if logged in, else false
     */
    private fun launchLoginActivityIfLoggedOut(): Boolean {
        if (!ChatSingleton.isLoggedIn()) {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
            return false
        }
        return true
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
