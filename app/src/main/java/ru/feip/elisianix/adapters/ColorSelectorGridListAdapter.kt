package ru.feip.elisianix.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemColorSelectorBinding
import ru.feip.elisianix.extensions.setContrastText
import ru.feip.elisianix.extensions.setSelectorPaint
import ru.feip.elisianix.remote.models.ProductColor


class ColorSelectorGridListAdapter(
    private val clickListenerColorSelector: (ProductColor) -> Unit
) : ListAdapter<ProductColor, RecyclerView.ViewHolder>(ItemCallback()) {

    var currentPos = 0
    var lastPos = -1

    inner class ColorSelectorGridList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemColorSelectorBinding.bind(item)

        fun default() {
            binding.productColorName.setSelectorPaint(false)
        }

        fun selected() {
            binding.productColorName.setSelectorPaint(true)
        }

        init {
            binding.cardItem.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position in currentList.indices) {
                    lastPos = currentPos
                    currentPos = position
                    notifyItemChanged(lastPos)
                    notifyItemChanged(currentPos)
                    clickListenerColorSelector.invoke(currentList[position])
                }
            }
        }

        fun bind(item: ProductColor) {
            binding.productColorName.setContrastText(Color.parseColor(item.value), item.name)
            binding.cardItem.setCardBackgroundColor(Color.parseColor(item.value))
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
            .inflate(R.layout.item_color_selector, parent, false)
        return ColorSelectorGridList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ColorSelectorGridList -> {
                if (position == currentPos) {
                    holder.selected()
                } else holder.default()
                holder.bind(currentList[position])
            }
        }
    }
}