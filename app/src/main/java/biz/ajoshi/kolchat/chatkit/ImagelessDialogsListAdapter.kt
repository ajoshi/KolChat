package biz.ajoshi.kolchat.chatkit

import android.view.View
import android.view.ViewGroup
import com.stfalcon.chatkit.R
import com.stfalcon.chatkit.commons.models.IDialog
import com.stfalcon.chatkit.dialogs.DialogsListAdapter

/**
 * Created by ajoshi on 7/6/2017.
 */
class ImagelessDialogsListAdapter() : DialogsListAdapter<DefaultDialog>(null) {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BaseDialogViewHolder<out IDialog<*>> {
        val holder = super.onCreateViewHolder(parent, viewType)
        // yes, this is technically slower, but it's only 2 finds and it lets me use everything
        // else without maintaining another xml
        holder.itemView.findViewById<View>(R.id.dialogLastMessageUserAvatar).visibility = View.GONE
        holder.itemView.findViewById<View>(R.id.dialogAvatar).visibility = View.GONE
        return holder
    }
}
