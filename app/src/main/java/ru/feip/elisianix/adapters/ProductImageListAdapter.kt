package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemProductImageBinding
import ru.feip.elisianix.remote.models.Image


class ProductImageListAdapter(
    private val clickListenerToImageViewer: (Image) -> Unit,
) : ListAdapter<Image, RecyclerView.ViewHolder>(ItemCallback()) {

    inner class ProductImageList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemProductImageBinding.bind(item)

        init {
            binding.item.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position in currentList.indices) {
                    clickListenerToImageViewer.invoke(currentList[position])
                }
            }
        }

        fun bind(item: Image) {
            binding.apply {
                Glide.with(itemView).load(item.url)
                    .timeout(60000)
                    .placeholder(R.drawable.shape_placeholder)
                    .error(R.drawable.shape_placeholder)
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
            .inflate(R.layout.item_product_image, parent, false)
        return ProductImageList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductImageList -> {
                val item = currentList[position]
                holder.bind(item)
            }
        }
    }
}