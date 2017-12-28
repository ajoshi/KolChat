package biz.ajoshi.kolchat.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import biz.ajoshi.kolchat.R

/**
 * View that contains a list (should the be a recylcler/listview?) of buttons that map to chat commands
 * Created by ajoshi
 */
class QuickCommandView : LinearLayout, QuickCommandVH.VHCommandClickListener {
    interface CommandClickListener {
        /**
         * Called when a command in the Quick Command bar is tapped
         */
        fun onCommandClicked(command: QuickCommand)
    }

    override fun onViewHolderCommandClicked(command: QuickCommand) {
        commandClickListener?.onCommandClicked(command)
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initializeViews(context)
    }

    private var commandClickListener: CommandClickListener? = null

    fun initializeViews(context: Context) {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.quick_command_view, this)
        val commands = mutableListOf<QuickCommand>()
        commands.add(QuickCommand("em", "/em "))
        commands.add(QuickCommand("romans", "/romans"))
        commands.add(QuickCommand("who", "/who"))
        commands.add(QuickCommand(">:(", ">:("))
        val recyclerView = view.findViewById<RecyclerView>(R.id.quick_command_list)
        val layoutMgr = LinearLayoutManager(context)
        layoutMgr.orientation = LinearLayoutManager.HORIZONTAL
        val adapter = QuickCommandAdapter(this)
        adapter.commands = commands
        recyclerView.adapter = adapter
        recyclerView?.layoutManager = layoutMgr
    }

    fun setClickListener(listener: CommandClickListener) {
        commandClickListener = listener
    }
}

/**
 * Defines a quick command. Currently just a name that is shown, and a command that is sent to the server
 */
data class QuickCommand(val name: String, val command: String)

/**
 * Viewholder for a QuickCommand
 */
class QuickCommandVH(itemView: View, val commandClickListener: VHCommandClickListener) : RecyclerView.ViewHolder(itemView) {

    interface VHCommandClickListener {
        fun onViewHolderCommandClicked(command: QuickCommand)
    }

    val nameTv: TextView
    var currentCommand: QuickCommand? = null

    init {
        nameTv = itemView.findViewById<TextView>(R.id.name)
        itemView.setOnClickListener {
            currentCommand?.let {
                commandClickListener.onViewHolderCommandClicked(currentCommand!!)
            }

        }
    }

    fun bind(command: QuickCommand) {
        currentCommand = command
        nameTv.text = command.name
    }
}

/**
 * Basic Array Adapter for quick commands
 */
class QuickCommandAdapter(val commandClickListener: QuickCommandVH.VHCommandClickListener) : RecyclerView.Adapter<QuickCommandVH>() {
    var commands = mutableListOf<QuickCommand>()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): QuickCommandVH {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.quick_command_item, parent, false)
        return QuickCommandVH(view, commandClickListener)
    }

    override fun onBindViewHolder(holder: QuickCommandVH?, position: Int) {
        if (commands.size > position) {
            holder?.bind(commands.get(position))
        }
    }

    override fun getItemCount(): Int {
        return commands.size
    }

    fun setCommandList(newList: List<QuickCommand>) {
        // TODO see if this is even needed or if this can make do with an immutable list
        commands = newList.toMutableList()
        notifyDataSetChanged()
    }
}
