package biz.ajoshi.kolchat

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.ajoshi.kolchat.chatkit.ChatkitMessage
import biz.ajoshi.kolchat.chatkit.DefaultDialog
import biz.ajoshi.kolchat.persistence.ChatChannel
import biz.ajoshi.kolchat.persistence.ChatMessage
import com.stfalcon.chatkit.dialogs.DialogsList
import com.stfalcon.chatkit.dialogs.DialogsListAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Shows a list of all active chats/channels
 * Created by ajoshi on 7/4/2017.
 */
class ChatListFragment() : Fragment() { // empty constructor

    var dialogsListAdapter: DialogsListAdapter<DefaultDialog>? = null
    var channelUpdateSubscriber: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.chat_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val dialogsList = activity?.findViewById(R.id.dialogsList) as DialogsList
        dialogsListAdapter = DialogsListAdapter(null)//ImagelessDialogsListAdapter()
        dialogsList.setAdapter(dialogsListAdapter)
        dialogsListAdapter!!.setOnDialogClickListener { dialog ->  dialog.channel.id}
        channelUpdateSubscriber = KolChatApp.database
                ?.ChannelDao()
                ?.getAllChannels()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.map {
                    channelList ->
                    makeDialogs(channelList)
                }
                ?.subscribe {
                    dialogs ->
                    dialogsListAdapter!!.setItems(dialogs)
                }
        super.onActivityCreated(savedInstanceState)
    }

    override fun onDestroy() {
        if (channelUpdateSubscriber != null && !channelUpdateSubscriber!!.isDisposed) {
            channelUpdateSubscriber!!.dispose()
        }
        super.onDestroy()
    }

    fun makeDialogs(channels: List<ChatChannel>): List<DefaultDialog> {
        val dialogs = mutableListOf<DefaultDialog>()
        for (channel in channels) dialogs.add(DefaultDialog(channel, ChatkitMessage(ChatMessage(5, "", "", channel.lastMessage, channel.id, channel.lastMessageTime))))
        return dialogs
    }
}
