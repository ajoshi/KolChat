package biz.ajoshi.kolchat.chat.detail

import androidx.recyclerview.widget.RecyclerView

/**
 * A DataObserver that calls a single [doOnChange] method for everything
 */
abstract class PagingChatDataObserver(val adapter: PagingChatAdapter?) :
    RecyclerView.AdapterDataObserver() {

    abstract fun doOnChange()

    override fun onChanged() {
        doOnChange()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        doOnChange()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        // fallback to onItemRangeChanged(positionStart, itemCount) if app
        // does not override this method.
        doOnChange()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        doOnChange()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        doOnChange()
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        doOnChange()
    }

    /**
     * Called when the [Adapter.StateRestorationPolicy] of the [Adapter] changed.
     * When this method is called, the Adapter might be ready to restore its state if it has
     * not already been restored.
     *
     * @see Adapter.getStateRestorationPolicy
     * @see Adapter.setStateRestorationPolicy
     */
    override fun onStateRestorationPolicyChanged() {
        // do nothing
    }
}