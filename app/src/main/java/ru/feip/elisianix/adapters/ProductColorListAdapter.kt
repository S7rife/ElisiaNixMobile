package ru.feip.elisianix.adapters

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemProductColorBinding
import ru.feip.elisianix.remote.models.ProductColor


class ProductColorListAdapter(
    private val clickListenerColorSelector: (ProductColor) -> Unit
) : ListAdapter<ProductColor, RecyclerView.ViewHolder>(ItemCallback()) {

    var selectedItemPos = -1
    var lastItemSelectedPos = -1

    inner class ProductColorList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemProductColorBinding.bind(item)

        fun default() {
            binding.item.isSelected = false
        }

        fun selected() {
            binding.item.isSelected = true
        }

        init {
            binding.apply {
                item.setOnClickListener {
                    selectedItemPos = adapterPosition

                    val position = adapterPosition
                    if (position in currentList.indices) {
                        clickListenerColorSelector.invoke(currentList[position])
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

        fun bind(item: ProductColor) {
            itemView.background.mutate().colorFilter = BlendModeColorFilter(Color.parseColor(item.value), BlendMode.SRC_OVER)

        }
    }

    private class ItemCallback : DiffUtil.ItemCallback<ProductColor>() {
        override fun areItemsTheSame(
            oldItem: ProductColor,
            newItem: ProductColor
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ProductColor,
            newItem: ProductColor
        ): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_color, parent, false)
        return ProductColorList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductColorList -> {
                if (position == selectedItemPos) {
                    holder.selected()
                } else holder.default()
                holder.bind(currentList[position])
            }
        }
    }
}