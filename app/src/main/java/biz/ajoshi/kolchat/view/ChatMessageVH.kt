package biz.ajoshi.kolchat.view

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.style.ImageSpan
import android.view.View
import biz.ajoshi.kolchat.R
import biz.ajoshi.kolchat.persistence.ChatMessage
import biz.ajoshi.kolchat.util.StringUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.span.DraweeSpan
import com.facebook.drawee.span.DraweeSpanStringBuilder
import com.facebook.imagepipeline.request.ImageRequestBuilder
import kotlinx.android.synthetic.main.chat_message.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ajoshi on 7/22/17.
 */
val timeFormat = SimpleDateFormat.getTimeInstance()

class ChatMessageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    // TODO this might be doing a findviewbyid each time and not caching. confirm.
    // yes it is. DO NOT USE THIS
    fun bind(message: ChatMessage) {
        // TODO don't do all this excessive computation on ui thread. maybe use Transform/Map to convert to some ui model
        if (message.shouldHideUsername()) {
            itemView.user_name.visibility = View.GONE
        } else {
            itemView.user_name.text = StringUtil.getHtml(message.userName)
            itemView.user_name.visibility = View.VISIBLE
        }
        val oldSpannable = StringUtil.getHtml(message.text)
        val newSpannable = DraweeSpanStringBuilder(oldSpannable)
        val imageSpans = newSpannable.getSpans(0, oldSpannable.length, ImageSpan::class.java)
        val embeddedImageSize = itemView.context.resources.getDimensionPixelSize(R.dimen.chat_message_embedded_image_size)
        // go through spans and replace imagespans with drawee spans
        val draweeHierarchy = GenericDraweeHierarchyBuilder.newInstance(itemView.context.resources).setFadeDuration(0).build()
        for (imageSpan in imageSpans) {
            val oldSpanStartPosition = newSpannable.getSpanStart(imageSpan)
            val imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imageSpan.source)).build()
            val controller = Fresco.newDraweeControllerBuilder().setImageRequest(imageRequest).build()
            newSpannable.removeSpan(imageSpan)
            newSpannable.setImageSpan(itemView.context, draweeHierarchy, controller, oldSpanStartPosition, embeddedImageSize, embeddedImageSize
                    , false, DraweeSpan.ALIGN_CENTER)
        }

        itemView.text.setDraweeSpanStringBuilder(newSpannable)
        itemView.timestamp.text = timeFormat.format(Date(message.localtimeStamp))
        /* allow links to be clicked
        htmlText= This is the only link I need! <a target=_blank href="https://www.kingdomofloathing.com/"><font color=blue>[link]</font></a> https:// www.kingdomofloathin g.com/,
        only the 'link' part is clickable with this. Alt solution is to string replace and then enable autolink
        */
        itemView.text.movementMethod = LinkMovementMethod.getInstance();
        // todo use userid for right click options at some point
    }
}
