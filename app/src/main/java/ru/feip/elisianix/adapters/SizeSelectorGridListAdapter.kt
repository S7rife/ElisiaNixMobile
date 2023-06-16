package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemProductSizeBinding
import ru.feip.elisianix.extensions.setStrokeSelector
import ru.feip.elisianix.extensions.sizeFormat
import ru.feip.elisianix.remote.models.SizeMap


class SizeSelectorGridListAdapter(
    private val clickListenerSizeSelector: (SizeMap) -> Unit
) : ListAdapter<SizeMap, RecyclerView.ViewHolder>(ItemCallback()) {

    var currentPos = -1
    var lastPos = -1
    var availableSizes = listOf<SizeMap>()

    inner class SizeSelectorList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemProductSizeBinding.bind(item)

        fun default() {
            binding.cardItem.setStrokeSelector(false)
        }

        fun selected() {
            binding.cardItem.setStrokeSelector(true)
        }

        init {
            binding.cardItem.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position in currentList.indices) {
                    if (currentList[position] in availableSizes) {
                        lastPos = currentPos.also { currentPos = position }
                        if (lastPos == currentPos) {
                            lastPos = -1
                            currentPos = -1
                        }
                        notifyItemChanged(lastPos)
                        notifyItemChanged(position)
                        clickListenerSizeSelector.invoke(currentList[position])
                    }
                }
            }
        }

        fun bind(item: SizeMap) {
            binding.apply {
                productSize.sizeFormat(item.name, true)
                cardItem.isChecked = item !in availableSizes
            }
            if (item !in availableSizes) {
                binding.cardItem.isChecked = true
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
            .inflate(R.layout.item_product_size, parent, false)
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