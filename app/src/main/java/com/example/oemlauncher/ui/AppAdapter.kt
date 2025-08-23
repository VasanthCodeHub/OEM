package com.example.oemlauncher.ui


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.oemlauncher.R
import com.example.oemlauncher.model.Launchable

class AppAdapter(
    private val context: Context,
    private var items: List<Launchable>,
    private val onLongPress: (Launchable, View) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<AppAdapter.VH>() {

    fun submitList(newItems: List<Launchable>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)
        val label: TextView = view.findViewById(R.id.label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = items[position]
        holder.icon.setImageDrawable(app.icon)
        holder.label.text = app.label

        holder.itemView.setOnClickListener {
            // Launch target
            val i = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = app.component
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(i)
        }

        holder.itemView.setOnLongClickListener {
            onLongPress(app, holder.itemView)
            true
        }
    }

    override fun getItemCount() = items.size
}