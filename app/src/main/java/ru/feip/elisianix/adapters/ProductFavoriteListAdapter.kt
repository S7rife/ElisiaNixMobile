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
import ru.feip.elisianix.databinding.ItemFavoriteProductBinding
import ru.feip.elisianix.extensions.inCurrency
import ru.feip.elisianix.extensions.setCartStatus
import ru.feip.elisianix.extensions.setFavoriteStatus
import ru.feip.elisianix.remote.models.Image
import ru.feip.elisianix.remote.models.ProductDetail


class ProductFavoriteListAdapter(
    private val clickListenerToProduct: (Pair<Int, Image>) -> Unit,
    private val clickListenerCartBtn: (ProductDetail) -> Unit,
    private val clickListenerFavoriteBtn: (ProductDetail) -> Unit
) : ListAdapter<ProductDetail, RecyclerView.ViewHolder>(ItemCallback()) {

    inner class ProductFavoriteList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemFavoriteProductBinding.bind(item)
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

        fun bind(item: ProductDetail) {
            binding.apply {
                productName.text = item.name
                productPrice.inCurrency(item.price)
                productCartBtn.setCartStatus(item.inCart)
                productFavoriteBtn.setFavoriteStatus(item.inFavorites)
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

    private class ItemCallback : DiffUtil.ItemCallback<ProductDetail>() {
        override fun areItemsTheSame(
            oldItem: ProductDetail,
            newItem: ProductDetail
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ProductDetail,
            newItem: ProductDetail
        ): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_product, parent, false)
        return ProductFavoriteList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductFavoriteList -> {
                val item = currentList[position]
                holder.bind(item)
            }
        }
    }
}