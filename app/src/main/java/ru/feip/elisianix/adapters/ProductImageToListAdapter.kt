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


class ProductImageToListAdapter(
    private val clickListenerToProduct: (Pair<Int, Image>) -> Unit,
) : ListAdapter<Pair<Int, Image>, RecyclerView.ViewHolder>(ItemCallback()) {

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

        fun bind(item: Pair<Int, Image>) {
            binding.apply {
                Glide.with(itemView).load(item.second.url)
                    .error(R.drawable.ic_no_image)
                    .into(productImage)
            }
        }
    }

    private class ItemCallback : DiffUtil.ItemCallback<Pair<Int, Image>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Int, Image>,
            newItem: Pair<Int, Image>
        ): Boolean = oldItem.second.id == newItem.second.id

        override fun areContentsTheSame(
            oldItem: Pair<Int, Image>,
            newItem: Pair<Int, Image>
        ): Boolean = oldItem.second == newItem.second
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