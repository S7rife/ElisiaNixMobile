package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemProductSizeBinding
import ru.feip.elisianix.remote.models.Size
import ru.feip.elisianix.remote.models.SizeMap


class ProductSizeListAdapter(
    private val clickListenerSizeSelector: (Size) -> Unit
) : ListAdapter<Size, RecyclerView.ViewHolder>(ItemCallback()) {

    var selectedItemPos = -1
    var lastItemSelectedPos = -1

    inner class ProductSizeList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemProductSizeBinding.bind(item)

        fun default() {
            binding.item.isChecked = false
        }

        fun selected() {
            binding.apply {
                item.isChecked = true
            }
        }

        init {
            binding.apply {
                item.setOnClickListener {
                    selectedItemPos = adapterPosition

                    val position = adapterPosition
                    if (position in currentList.indices) {
                        clickListenerSizeSelector.invoke(currentList[position])
                        lastItemSelectedPos = if (lastItemSelectedPos == -1) {
                            selectedItemPos
                        } else {
                            notifyItemChanged(lastItemSelectedPos)
                            selectedItemPos
                        }
                        notifyItemChanged(selectedItemPos)
                    }
                }
            }
        }

        fun bind(item: Size) {
            binding.apply {
                val sizes = SizeMap.valueOf(item.value).sizes
                val size = "${item.value}\n(${sizes.first}-${sizes.second})"
                productSize.text = size
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
                if (position == selectedItemPos) {
                    holder.selected()
                } else holder.default()
                holder.bind(currentList[position])
            }
        }
    }
}