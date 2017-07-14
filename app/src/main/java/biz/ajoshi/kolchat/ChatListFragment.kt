package biz.ajoshi.kolchat

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.ajoshi.kolchat.chatkit.ChatkitMessage
import biz.ajoshi.kolchat.chatkit.DefaultDialog
import biz.ajoshi.kolchat.model.ServerChatChannel
import biz.ajoshi.kolchat.model.ServerChatMessage
import biz.ajoshi.kolchat.model.User
import com.stfalcon.chatkit.dialogs.DialogsList
import com.stfalcon.chatkit.dialogs.DialogsListAdapter

/**
 * Shows a list of all active chats/channels
 * Created by ajoshi on 7/4/2017.
 */
class ChatListFragment() : Fragment() { // empty constructor

    var dialogsListAdapter: DialogsListAdapter<DefaultDialog>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.chat_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val dialogsList: DialogsList = activity?.findViewById(R.id.dialogsList) as DialogsList
        dialogsListAdapter = DialogsListAdapter(null)//ImagelessDialogsListAdapter()
        dialogsList.setAdapter(dialogsListAdapter)

        val serviceIntent = Intent(activity, ChatService::class.java)
        activity.startService(serviceIntent)

        val dd = DefaultDialog(ServerChatChannel("games", "games", false), ChatkitMessage(ServerChatMessage(User("corman", "bbb"), "omg AR", ServerChatChannel("games", "games", false), (System.currentTimeMillis() - 30000))))
        val aa = DefaultDialog(ServerChatChannel("trade", "trade", false), ChatkitMessage(ServerChatMessage(User("aeshma", "bbcccb"), "selling some shit", ServerChatChannel("trade", "trade", false), 0)))
        val bbd = DefaultDialog(ServerChatChannel("ajoshi", "butts", true), ChatkitMessage(ServerChatMessage(User("ajoshi", "butts"), "something funny and smart", ServerChatChannel("ajoshi", "butts", true), 0)))
        dialogsListAdapter?.addItem(dd)
        dialogsListAdapter?.addItem(aa)
        dialogsListAdapter?.addItem(bbd)

        val c = DefaultDialog(ServerChatChannel("aeshma", "id1", true), ChatkitMessage(ServerChatMessage(User("aeshma", "id1"), "something mean", ServerChatChannel("aeshma", "id1", true), 0)))
        dialogsListAdapter?.addItem(c)
        val d = DefaultDialog(ServerChatChannel("psyche", "id2", true), ChatkitMessage(ServerChatMessage(User("psyche", "id2"), "linguistic stuff", ServerChatChannel("psyche", "id2", true), 0)))
        dialogsListAdapter?.addItem(d)
        val e = DefaultDialog(ServerChatChannel("jick", "i3", true), ChatkitMessage(ServerChatMessage(User("jick", "id3"), "wow, you're so smart", ServerChatChannel("jick", "id3", true), 0)))
        dialogsListAdapter?.addItem(e)
        val f = DefaultDialog(ServerChatChannel("pantsless", "id4", true), ChatkitMessage(ServerChatMessage(User("pantsless", "id4"), "last seen message", ServerChatChannel("pantsless", "id4", true), 0)))
        dialogsListAdapter?.addItem(f)

        val g = DefaultDialog(ServerChatChannel("bashy", "id5", true), ChatkitMessage(ServerChatMessage(User("bashy", "id5"), "kill babies", ServerChatChannel("bashy", "id5", true), 0)))
        dialogsListAdapter?.addItem(g)

        val h = DefaultDialog(ServerChatChannel("hermit", "id6", true), ChatkitMessage(ServerChatMessage(User("hermit", "id6"), "gimme trinkets", ServerChatChannel("hermit", "id6", true), 0)))
        dialogsListAdapter?.addItem(h)

        super.onActivityCreated(savedInstanceState)
    }
}
