package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemProductImageViewerBinding
import ru.feip.elisianix.remote.models.Image


class ProductImageViewerListAdapter(
    private val clickListenerToImageViewer: (Pair<Image, Int>) -> Unit,
) : ListAdapter<Image, RecyclerView.ViewHolder>(ItemCallback()) {

    var currentPos = 0
    var lastPos = -1

    inner class ProductImageList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemProductImageViewerBinding.bind(item)

        fun default() {
            binding.productImageContainer.alpha = 0.7f
        }

        fun selected() {
            binding.productImageContainer.alpha = 1f
        }

        init {
            binding.item.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position in currentList.indices) {
                    clickListenerToImageViewer.invoke(Pair(currentList[position], position + 1))
                    lastPos = currentPos
                    currentPos = position
                    notifyItemChanged(lastPos)
                    notifyItemChanged(currentPos)
                }
            }
        }

        fun bind(item: Image) {
            binding.apply {
                Glide.with(itemView).load(item.url)
                    .error(R.drawable.ic_no_image)
                    .into(productImage)
            }
        }
    }

    private class ItemCallback : DiffUtil.ItemCallback<Image>() {
        override fun areItemsTheSame(
            oldItem: Image,
            newItem: Image
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Image,
            newItem: Image
        ): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_image_viewer, parent, false)
        return ProductImageList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductImageList -> {
                if (position == currentPos) {
                    holder.selected()
                } else holder.default()
                holder.bind(currentList[position])
            }
        }
    }
}