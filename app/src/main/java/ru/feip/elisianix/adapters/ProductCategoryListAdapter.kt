package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemCategoryProductBinding
import ru.feip.elisianix.extensions.addStrikethrough
import ru.feip.elisianix.extensions.inCurrency
import ru.feip.elisianix.extensions.setCartStatus
import ru.feip.elisianix.extensions.setFavoriteStatus
import ru.feip.elisianix.remote.models.Image
import ru.feip.elisianix.remote.models.ProductMainPreview

class ProductCategoryListAdapter(
    private val clickListenerToProduct: (Pair<Int, Image>) -> Unit,
    private val clickListenerCartBtn: (ProductMainPreview) -> Unit,
    private val clickListenerFavoriteBtn: (ProductMainPreview) -> Unit
) : ListAdapter<ProductMainPreview, RecyclerView.ViewHolder>(ItemCallback()) {

    inner class ProductCategoryList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemCategoryProductBinding.bind(item)
        private lateinit var productImageAdapter: ProductImageToListAdapter

        init {
            binding.apply {
                productCartBtn.setOnClickListener {
                    val position = absoluteAdapterPosition
                    if (position in currentList.indices) {
                        clickListenerCartBtn.invoke(currentList[position])
                        notifyItemChanged(position)
                    }
                }
                productFavoriteBtn.setOnClickListener {
                    val position = absoluteAdapterPosition
                    if (position in currentList.indices) {
                        clickListenerFavoriteBtn.invoke(currentList[position])
                        notifyItemChanged(position)
                    }
                }
            }
        }

        fun bind(item: ProductMainPreview) {
            binding.apply {
                productName.text = item.name

                productNewPrice.inCurrency(item.price)
                productOldPrice.inCurrency(item.price)
                productOldPrice.addStrikethrough()

                productCartBtn.setCartStatus(item.inCart)
                productFavoriteBtn.setFavoriteStatus(item.inFavorites)

                // TODO change tag and price with discount from remote
                productActualTag.isVisible = item.isNew

                productImageAdapter = ProductImageToListAdapter(clickListenerToProduct)
                recyclerProductImage.adapter = productImageAdapter
                recyclerProductImage.layoutManager =
                    LinearLayoutManager(
                        itemView.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                productImageAdapter.submitList(item.images.map { Pair(item.id, it) })

                if (item.images.count() > 1) {
                    recyclerProductImageIndicator.attachToRecyclerView(recyclerProductImage)
                }
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
            .inflate(R.layout.item_category_product, parent, false)
        return ProductCategoryList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductCategoryList -> {
                val item = currentList[position]
                holder.bind(item)
            }
        }
    }
}