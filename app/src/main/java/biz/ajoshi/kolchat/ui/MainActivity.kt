package biz.ajoshi.kolchat.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import biz.ajoshi.commonutils.Logg
import biz.ajoshi.commonutils.StringUtilities
import biz.ajoshi.kolchat.*
import biz.ajoshi.kolchat.R
import biz.ajoshi.kolchat.accounts.KolAccountManager
import biz.ajoshi.kolchat.chat.*
import biz.ajoshi.kolchat.chat.view.ChatChannelAdapter
import biz.ajoshi.kolchat.chat.view.customviews.ChatDetailList
import biz.ajoshi.kolchat.persistence.chat.ChatChannel
import biz.ajoshi.kolchat.persistence.chat.ChatMessage
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.CustomEvent

const val action_navigate_to_chat_detail = "biz.ajoshi.kolchat.ui.MainActivity.ACTION_NAVIGATE_TO_CHAT_DETAIL"

class MainActivity : AppCompatActivity(), ChatChannelAdapter.ChannelClickListener, ChatDetailList.MessageClickListener {
    internal var toolbar: Toolbar? = null
    internal var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // for the first ever launch, set default values for the preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

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
        // set up toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // stop the once-every-15-minutes polling job
        ChatJob.stopJob()

        // set up nav graph
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.llist)
        navController = navHostFragment.findNavController()
        navController?.let {
            it.setGraph(R.navigation.nav_graph)
            it.currentDestination?.label = getString(R.string.app_name)
            NavigationUI.setupActionBarWithNavController(this, it)
            // Nav components don't work correctly (surprise!). Setting labels programmatically seems not to be doable at all
//            it.addOnNavigatedListener(NavController.OnNavigatedListener { _, destination -> toolbar?.title = getPlaintextForHtml(""+ destination.label)})
        }
        // analytics- log the source of the launch intent
        when (intent.action) {
        // launched by os
            Intent.ACTION_MAIN ->
                logLaunchEvent("Launcher")
        // launched by the notification for a chat message
            action_navigate_to_chat_detail ->
                logLaunchEvent("Notification: chat detail")
        // launched by login screen (or something else?)
        // this should tell me if users are logging in more than they should
            else -> logLaunchEvent("Login/Unknown")
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        // launch the background polling service if still logged in
        if (ChatSingleton.isLoggedIn()) {
            Logg.i("MainActivity", "destroying activity and triggering background poll service")
            val preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)

            val shouldEnableChatJob = preferenceManager.getBoolean(KEY_PREF_ENABLE_POLL, true)
            val shouldEnableFrequentBgPoll = preferenceManager.getBoolean(KEY_PREF_SUPER_FAST_POLL, false)

            if (shouldEnableFrequentBgPoll) {
                // this kills battery, but polls every minute. Probably off.
                val increasePollTimeout = Intent(this, ChatBackgroundService::class.java)
                // activity is gone, increase poll interval to 1 minute
                increasePollTimeout.putExtra(EXTRA_POLL_INTERVAL_IN_MS, 60000)
                startService(increasePollTimeout)
            } else {
                // stop the frequent poller
                stopBgChatService()
                if (shouldEnableChatJob) {
                    // if we wanted the slow poller, then enable that
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
        }
    }


    /*************************************************************   General stuff   ************************/

    /**
     * Logs the 'app launched' event for analytics.
     * @param source Source of the launch (Launcher, notification, login page, etc)
     */
    private fun logLaunchEvent(source: String) {
        if (Analytics.shouldTrackEvents) {
            val customEvent = CustomEvent(EVENT_NAME_APP_LAUNCH)
                    .putCustomAttribute(EVENT_ATTRIBUTE_SOURCE, source)
            val preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
            customEvent.putCustomAttribute(EVENT_ATTRIBUTE_IS_POLLING_ENABLED,
                    preferenceManager.getBoolean(KEY_PREF_ENABLE_POLL, true).toString())
            customEvent.putCustomAttribute(EVENT_ATTRIBUTE_IS_FAST_POLLING_ENABLED,
                    preferenceManager.getBoolean(KEY_PREF_SUPER_FAST_POLL, false).toString())
            Analytics.getAnswers()?.logCustom(customEvent)
        }
    }

    /**
     * Stops the background service that runs when the app is running
     */
    private fun stopBgChatService() {
        val serviceIntent = Intent(this, ChatBackgroundService::class.java)
        stopService(serviceIntent)
    }

    /**
     * show dialog to send crashlytics logs
     */
    private fun showSendLogsDialog() {
        FeedbackDialog(context = this, rootView = findViewById<View>(R.id.llist))
                .createDialog()
                .show()
    }

    /*************************************************************   Toolbar menu   ************************/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_action_advanced_settings -> {
            // navigate to settings
            navController?.navigate(R.id.nav_preferences)
            true
        }

        R.id.menu_action_logout -> {            // log out and jump to login page
            logoutAndExit()
            true
        }

        R.id.menu_action_send_logs -> {
            showSendLogsDialog()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    /*************************************************************   Click listeners   ************************/

    /**
     * Called when a channel name is tapped
     *
     * @param channel ChatChannel object describing the channel that was opened
     */
    override fun onChannelClicked(channel: ChatChannel) {
        val b = Bundle()
        val plainTextName = getPlaintextForHtml(channel.name)
        b.putString(EXTRA_CHANNEL_ID, channel.id)
        b.putString(EXTRA_CHANNEL_NAME, plainTextName)
        b.putBoolean(EXTRA_CHANNEL_IS_PRIVATE, channel.isPrivate)
        navController?.navigate(R.id.nav_chat_message, b)
        navController?.currentDestination?.label = plainTextName
        // Go back to this if nav arch is as half baked as it seems
//        val chatDetailFrag = ChatMessageFrag()
//        chatDetailFrag.arguments = b
//        supportFragmentManager.beginTransaction().replace(R.id.llist, chatDetailFrag, TAG_CHAT_DETAIL_FRAG)
//                .addToBackStack(TAG_CHAT_DETAIL_FRAG).commit()
        if (toolbar != null) {
            // should crash on line 66 if this happens, honestly
            toolbar!!.title = getPlaintextForHtml(plainTextName)
        }
        Logg.i("MainActivity", if (channel.isPrivate) {
            "Channel detail opened"
        } else {
            "PM detail opened"
        })
    }

    /**
     * Called when a message is long pressed so we can spin up a chat for this user
     */
    override fun onMessageLongClicked(message: ChatMessage) {
        // trying to open a new chat? Go back to main list first to keep backstack simple
        // needed because nav components have bad support for programmatic label/toolbar title setting
        navController?.popBackStack()
        onChannelClicked(ChatChannel(message.userId, true, message.userName, "", 0, ""))
    }

    /*************************************************************   Handle Navigation   ************************/

    override fun onSupportNavigateUp() = findNavController(R.id.llist).navigateUp()

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

    /*************************************************************   Handle redirect to login page   ************************/

    /**
     * Will log out, kill the service, and exit this activity (and launch the login activity)
     */
    fun logoutAndExit() {
        // stop the service
        stopBgChatService()
        // log out
        ChatSingleton.network?.logout()
        // go to login page
        launchLoginActivityIfLoggedOut()
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

    override fun onResume() {
        super.onResume()
        // check to see if we're still logged in (just in case)
        launchLoginActivityIfLoggedOut()
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
