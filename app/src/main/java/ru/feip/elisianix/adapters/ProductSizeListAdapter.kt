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
import ru.feip.elisianix.remote.models.Size


class ProductSizeListAdapter(
    private val clickListenerSizeSelector: (Size) -> Unit
) : ListAdapter<Size, RecyclerView.ViewHolder>(ItemCallback()) {

    var currentPos = -1
    var lastPos = -1

    inner class ProductSizeList(item: View) : RecyclerView.ViewHolder(item) {
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
                if (position in currentList.indices && currentList[position].available > 0) {
                    lastPos = currentPos.also { currentPos = position }
                    notifyItemChanged(lastPos)
                    if (lastPos == currentPos) lastPos = (-1).also { currentPos = -1 }
                    notifyItemChanged(position)
                    clickListenerSizeSelector.invoke(currentList[position])
                }
            }
        }

        fun bind(item: Size) {
            binding.apply {
                productSize.sizeFormat(item.value, true)
                cardItem.isChecked = item.available <= 0
            }
        }
    }

    private class ItemCallback : DiffUtil.ItemCallback<Size>() {
        override fun areItemsTheSame(
            oldItem: Size,
            newItem: Size
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Size,
            newItem: Size
        ): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_size, parent, false)
        return ProductSizeList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductSizeList -> {
                if (position == currentPos) {
                    holder.selected()
                } else holder.default()
                holder.bind(currentList[position])
            }
        }
    }
}