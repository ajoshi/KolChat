package biz.ajoshi.kolchat.ui

import android.content.ComponentName
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import biz.ajoshi.commonutils.Logg
import biz.ajoshi.commonutils.StringUtilities
import biz.ajoshi.kolchat.*
import biz.ajoshi.kolchat.R
import biz.ajoshi.kolchat.accounts.KolAccountManager
import biz.ajoshi.kolchat.chat.*
import biz.ajoshi.kolchat.chat.view.customviews.ChatChannelList
import biz.ajoshi.kolchat.chat.view.customviews.ChatDetailList
import biz.ajoshi.kolchat.persistence.KolDB
import biz.ajoshi.kolchat.persistence.chat.ChatChannel
import biz.ajoshi.kolchat.persistence.chat.ChatMessage
import biz.ajoshi.kolnetwork.model.User
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.CustomEvent

const val action_navigate_to_chat_detail = "biz.ajoshi.kolchat.ui.MainActivity.ACTION_NAVIGATE_TO_CHAT_DETAIL"

class MainActivity : AppCompatActivity(), ChatChannelList.ChatChannelInteractionListener, ChatDetailList.MessageClickListener, NavController.OnNavigatedListener {
    private var toolbar: Toolbar? = null
    private var navController: NavController? = null
    private val rolloverBroadcastReceiver = biz.ajoshi.kolchat.accounts.RolloverBroadcastReceiver()

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
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(KEY_PREF_SEND_USERNAME, true)) {
                // dont' log username unconditionally, but if the user has allowed it, then set it
                Crashlytics.setUserIdentifier(getCurrentUser()?.name)
            }
            // we're logged in
            val serviceIntent = Intent(this, ChatBackgroundService::class.java)
            serviceIntent.putExtra(EXTRA_POLL_INTERVAL_IN_MS, 2000L)
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
        navController = navHostFragment?.findNavController()
        navController?.let {
            it.setGraph(R.navigation.nav_graph)
            it.currentDestination?.label = getString(R.string.app_name)
            //         NavigationUI.setupActionBarWithNavController(this, it)

            // Nav components don't work correctly (surprise!). Setting labels programmatically seems not to be doable at all
            it.addOnNavigatedListener(this)
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
        rolloverBroadcastReceiver.register(this, findViewById(R.id.llist))
    }

    override fun onNavigated(controller: NavController, destination: NavDestination) {
        toolbar?.title = getPlaintextForHtml("" + destination.label)
    }

    public override fun onDestroy() {
        super.onDestroy()
        rolloverBroadcastReceiver.unregister(this)
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
                increasePollTimeout.putExtra(EXTRA_POLL_INTERVAL_IN_MS, 60000L)
                startService(increasePollTimeout)
            } else {
                // stop the frequent poller
                stopBgChatService()
                if (shouldEnableChatJob) {
                    // if we wanted the slow poller, then enable that if the user is logged in
                    val currentUserName = getCurrentUser()?.id
                    currentUserName?.let {
                        val accountMgr = KolAccountManager(this)
                        val account = accountMgr.getAccount(it)
                        account?.let { currentAccount ->
                            ChatJob.scheduleJob(
                                    ComponentName(this, javaClass),
                                    currentAccount.username,
                                    currentAccount.password)
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
     * Stops the background service that runs when the app is running (but do not log out)
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

    private fun getCurrentUser(): User? {
        return ChatSingleton.network?.currentUser?.player
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
        val plainTextName = getPlaintextForHtml(channel.name)
        getCurrentUser()?.let { user ->
            val b = ChatMessageFragment.getBundleForChatMessageFragment(
                    currentUserId = user.id,
                    channelName = plainTextName,
                    channelId = channel.id,
                    isPrivate = channel.isPrivate,
                    isComposerDisabled = rolloverBroadcastReceiver.isRollover)
            navController?.let { controller ->
                controller.navigate(R.id.nav_chat_message, b)
                // not setting the label here because 'currentDestination' is still the old page
                //     controller.currentDestination?.label = plainTextName

                // Go back to this if nav arch is as half baked as it seems
//        val chatDetailFrag = ChatMessageFragment()
//        chatDetailFrag.arguments = b
//        supportFragmentManager.beginTransaction().replace(R.id.llist, chatDetailFrag, TAG_CHAT_DETAIL_FRAG)
//                .addToBackStack(TAG_CHAT_DETAIL_FRAG).commit()
                toolbar?.let { toolbar ->
                    toolbar.title = getPlaintextForHtml(plainTextName)
                }
                Logg.i("MainActivity", if (channel.isPrivate) {
                    "Channel detail opened"
                } else {
                    "PM detail opened"
                })
            }
        }
    }

    /**
     * Called when the user swipes left on a channel. Delete it locally
     */
    override fun onChannelSwiped(channel: ChatChannel) {
        // throw up yet another dialog to confirm that the user wants to delete channel
        val removeChatMessage = String.format(if (channel.isPrivate) getString(R.string.remove_chat_dialog_message_pm) else getString(R.string.remove_chat_dialog_message_group), channel.name)
        AlertDialog.Builder(this)
                .setTitle(R.string.remove_chat_dialog_title)
                .setMessage(removeChatMessage)
                .setPositiveButton(R.string.remove_chat_dialog_option_yes, { _, _ ->
                    // delete the channel
                    val deleteTask = DeleteChannelTask()
                    deleteTask.execute(channel)
                })
                .setNegativeButton(R.string.remove_chat_dialog_option_no, null).show()
    }

    /**
     * Called when a message is long pressed so we can spin up a chat for this user
     */
    override fun onMessageLongClicked(message: ChatMessage) {
        // trying to open a new chat? Go back to main list first to keep backstack simple
        // needed because nav components have bad support for programmatic label/toolbar title setting
        //    navController?.popBackStack()
        onChannelClicked(ChatChannel(message.userId, true, message.userName, "", 0, "", 0))
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
        KolAccountManager(this).logout()
        // go to login page
        launchLoginActivityIfLoggedOut()
    }

    /**
     * Launches the login activity if the user is logged out
     * @return true if logged in, else false
     */
    private fun launchLoginActivityIfLoggedOut(): Boolean {
        if (ChatSingleton.network == null) {
            Logg.e("network is null- app was killed?")
        } else if (ChatSingleton.network != null && !ChatSingleton.network!!.isLoggedIn) {
            Logg.e("network not null, but logged out. something happened")
        }
//        Crashlytics.logException(UserSentLogsEvent("why am i logged out?"))
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

class DeleteChannelTask() : AsyncTask<ChatChannel, Unit, Unit>() {
    override fun doInBackground(vararg params: ChatChannel?) {
        for (channel in params) {
            // I'll never send more than one (I think) but whatever
            channel?.let {
                // delete the messages in this channel
                KolDB.getDb()?.MessageDao()?.deleteAllForChannelId(it.id)
                // delete this channel as well
                KolDB.getDb()?.ChannelDao()?.delete(it)
            }
        }
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
