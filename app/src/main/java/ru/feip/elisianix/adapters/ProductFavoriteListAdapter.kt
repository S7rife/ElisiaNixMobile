package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.feip.elisianix.R
import ru.feip.elisianix.common.db.cartDao
import ru.feip.elisianix.common.db.checkInCartById
import ru.feip.elisianix.databinding.ItemFavoriteProductBinding
import ru.feip.elisianix.extensions.inCurrency
import ru.feip.elisianix.extensions.setCartStatus
import ru.feip.elisianix.extensions.setFavoriteStatus
import ru.feip.elisianix.remote.models.ImageProvider
import ru.feip.elisianix.remote.models.ProductMainPreview


class ProductFavoriteListAdapter(
    private val clickListenerToProduct: (ImageProvider) -> Unit,
    private val clickListenerCartBtn: (ProductMainPreview) -> Unit,
    private val clickListenerFavoriteBtn: (ProductMainPreview) -> Unit,
    private val lifecycleOwner: LifecycleOwner,
) : ListAdapter<ProductMainPreview, RecyclerView.ViewHolder>(ItemCallback()) {

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
                cartDao.checkCntLive().observe(lifecycleOwner) {
                    val position = absoluteAdapterPosition
                    if (position in currentList.indices) {
                        val prod = currentList[position]
                        val check = checkInCartById(prod.id)
                        if (prod.inCart != check) {
                            prod.inCart = check
                            notifyItemChanged(position)
                        }
                    }
                }
            }
        }

        fun bind(item: ProductMainPreview) {
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
                productImageAdapter.submitList(item.images.map {
                    ImageProvider(item.id, item.category.id, it)
                })

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