package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemSizeSelectorBinding
import ru.feip.elisianix.extensions.addStrikethrough
import ru.feip.elisianix.extensions.sizeFormat
import ru.feip.elisianix.remote.models.SizeMap


class SizeSelectorListAdapter(
    private val clickListenerSizeSelector: (Int) -> Unit
) : ListAdapter<SizeMap, RecyclerView.ViewHolder>(ItemCallback()) {

    var currentPos = -1
    var lastPos = -1
    var availableSizes = listOf<SizeMap>()

    inner class SizeSelectorList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemSizeSelectorBinding.bind(item)
        private val black = itemView.resources.getColor(R.color.black, itemView.context?.theme)
        private val white = itemView.resources.getColor(R.color.white, itemView.context?.theme)
        private val grey = itemView.resources.getColor(R.color.black30, itemView.context?.theme)

        fun default() {
            binding.itemBox.setBackgroundColor(white)
            binding.sizeValue.setTextColor(black)
        }

        fun selected() {
            binding.itemBox.setBackgroundColor(black)
            binding.sizeValue.setTextColor(white)
        }

        init {
            item.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position in currentList.indices) {
                    if (currentList[position] in availableSizes) {
                        lastPos = currentPos.also { currentPos = position }
                        notifyItemChanged(lastPos)
                        notifyItemChanged(position)
                        clickListenerSizeSelector.invoke(position)
                    }
                }

            }
        }

        fun bind(item: SizeMap) {
            binding.apply {
                sizeValue.sizeFormat(item.name)
                if (item !in availableSizes) {
                    sizeValue.addStrikethrough()
                    sizeValue.setTextColor(grey)
                }
            }
        }
    }

    private class ItemCallback : DiffUtil.ItemCallback<SizeMap>() {
        override fun areItemsTheSame(oldItem: SizeMap, newItem: SizeMap): Boolean =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: SizeMap, newItem: SizeMap): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_size_selector, parent, false)
        return SizeSelectorList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SizeSelectorList -> {
                if (position == currentPos) {
                    holder.selected()
                } else holder.default()
                holder.bind(currentList[position])
            }
        }
    }
}