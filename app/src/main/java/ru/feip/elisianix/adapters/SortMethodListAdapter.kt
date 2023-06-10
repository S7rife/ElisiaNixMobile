package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemSortMethodBinding
import ru.feip.elisianix.remote.models.SortMethod


class SortMethodListAdapter(
    private val clickListenerSortMethod: (SortMethod) -> Unit
) : ListAdapter<SortMethod, RecyclerView.ViewHolder>(ItemCallback()) {

    var currentPos = 0
    var lastPos = -1

    inner class SortMethodList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemSortMethodBinding.bind(item)
        private val blackColor = itemView.resources.getColor(R.color.black, itemView.context?.theme)
        private val whiteColor = itemView.resources.getColor(R.color.white, itemView.context?.theme)

        fun default() {
            binding.itemBox.setBackgroundColor(whiteColor)
            binding.sortMethodName.setTextColor(blackColor)
        }

        fun selected() {
            binding.itemBox.setBackgroundColor(blackColor)
            binding.sortMethodName.setTextColor(whiteColor)
        }

        init {
            item.setOnClickListener {
                val position = absoluteAdapterPosition
                lastPos = currentPos
                currentPos = position
                if (position in currentList.indices) {
                    clickListenerSortMethod.invoke(currentList[position])
                }
                notifyItemChanged(lastPos)
                notifyItemChanged(currentPos)
            }
        }

        fun bind(item: SortMethod) {
            binding.apply {
                sortMethodName.text = item.value.third
            }
        }
    }

    private class ItemCallback : DiffUtil.ItemCallback<SortMethod>() {
        override fun areItemsTheSame(
            oldItem: SortMethod,
            newItem: SortMethod
        ): Boolean = oldItem.value.first == newItem.value.first

        override fun areContentsTheSame(
            oldItem: SortMethod,
            newItem: SortMethod
        ): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sort_method, parent, false)
        return SortMethodList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SortMethodList -> {
                if (position == currentPos) {
                    holder.selected()
                } else holder.default()
                holder.bind(currentList[position])
            }
        }
    }
}