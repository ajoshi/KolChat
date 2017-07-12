package biz.ajoshi.kolchat

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.ajoshi.kolchat.chatkit.ChatkitMessage
import biz.ajoshi.kolchat.chatkit.DefaultDialog
import biz.ajoshi.kolchat.model.ChatChannel
import biz.ajoshi.kolchat.model.ChatMessage
import biz.ajoshi.kolchat.model.User
import com.stfalcon.chatkit.dialogs.DialogsList
import com.stfalcon.chatkit.dialogs.DialogsListAdapter
import java.util.*

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
        val dialogsList : DialogsList = activity?.findViewById(R.id.dialogsList) as DialogsList
        dialogsListAdapter = DialogsListAdapter(null)//ImagelessDialogsListAdapter()
        dialogsList.setAdapter(dialogsListAdapter)

        val dd  = DefaultDialog(ChatChannel("games","games",false), ChatkitMessage(ChatMessage(User("corman", "bbb"), "omg AR", "games", Date())))
        val aa  = DefaultDialog(ChatChannel("trade","trade",false), ChatkitMessage(ChatMessage(User("aeshma", "bbcccb"), "selling some shit", "trade", Date())))
        val bbd  = DefaultDialog(ChatChannel("ajoshi","butts", true), ChatkitMessage(ChatMessage(User("ajoshi", "butts"), "something funny and smart", "butts", Date())))
        dialogsListAdapter?.addItem(dd)
        dialogsListAdapter?.addItem(aa)
        dialogsListAdapter?.addItem(bbd)

        val c  = DefaultDialog(ChatChannel("aeshma","id1", true), ChatkitMessage(ChatMessage(User("aeshma", "id1"), "something mean", "id1", Date())))
        dialogsListAdapter?.addItem(c)
        val d  = DefaultDialog(ChatChannel("psyche","id2", true), ChatkitMessage(ChatMessage(User("psyche", "id2"), "linguistic stuff", "id2", Date())))
        dialogsListAdapter?.addItem(d)
        val e  = DefaultDialog(ChatChannel("jick","i3", true), ChatkitMessage(ChatMessage(User("jick", "id3"), "wow, you're so smart", "id3", Date())))
        dialogsListAdapter?.addItem(e)
        val f  = DefaultDialog(ChatChannel("pantsless","id4", true), ChatkitMessage(ChatMessage(User("pantsless", "id4"), "last seen massage", "id4", Date())))
        dialogsListAdapter?.addItem(f)

        val g  = DefaultDialog(ChatChannel("bashy","id5", true), ChatkitMessage(ChatMessage(User("bashy", "id5"), "kill babies", "id5", Date())))
        dialogsListAdapter?.addItem(g)

        val h  = DefaultDialog(ChatChannel("hermit","id6", true), ChatkitMessage(ChatMessage(User("hermit", "id6"), "gimme trinkets", "id6", Date())))
        dialogsListAdapter?.addItem(h)

        super.onActivityCreated(savedInstanceState)
    }
}