package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemMainProductBinding
import ru.feip.elisianix.remote.models.ProductMainPreview


class ProductMainListAdapter(

) : ListAdapter<ProductMainPreview, RecyclerView.ViewHolder>(ItemCallback()) {

    inner class ProductMainList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemMainProductBinding.bind(item)

        fun bind(item: ProductMainPreview) {
            binding.apply {
                Glide.with(itemView).load(item.images[0].url)
                    .error(R.drawable.ic_no_image)
                    .into(productImage)
                val price = "${item.price} ₽"
                productName.text = item.name
                productPrice.text = price
            }
        }
    }

    private class ItemCallback : DiffUtil.ItemCallback<ProductMainPreview>() {
        override fun areItemsTheSame(
            oldItem: ProductMainPreview,
            newItem: ProductMainPreview
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ProductMainPreview,
            newItem: ProductMainPreview
        ): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_main_product, parent, false)
        return ProductMainList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductMainList -> {
                val item = currentList[position]
                holder.bind(item)
            }
        }
    }
}