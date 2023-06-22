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
import ru.feip.elisianix.remote.models.ImageProvider


class ProductImageToListAdapter(
    private val clickListenerToProduct: (ImageProvider) -> Unit,
) : ListAdapter<ImageProvider, RecyclerView.ViewHolder>(ItemCallback()) {

    inner class ProductImageToList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemProductImageBinding.bind(item)

        init {
            binding.item.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position in currentList.indices) {
                    clickListenerToProduct.invoke(currentList[position])
                }
            }
        }

        fun bind(item: ImageProvider) {
            binding.apply {
                Glide.with(itemView).load(item.image.url)
                    .timeout(60000)
                    .placeholder(R.drawable.shape_placeholder)
                    .error(R.drawable.shape_placeholder)
                    .into(productImage)
            }
        }
    }

    private class ItemCallback : DiffUtil.ItemCallback<ImageProvider>() {
        override fun areItemsTheSame(
            oldItem: ImageProvider,
            newItem: ImageProvider
        ): Boolean = oldItem.image.id == newItem.image.id

        override fun areContentsTheSame(
            oldItem: ImageProvider,
            newItem: ImageProvider
        ): Boolean = oldItem.image == newItem.image
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_image, parent, false)
        return ProductImageToList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductImageToList -> {
                val item = currentList[position]
                holder.bind(item)
            }
        }
    }
}