package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.feip.elisianix.R
import ru.feip.elisianix.common.db.cartDao
import ru.feip.elisianix.common.db.checkInCartById
import ru.feip.elisianix.common.db.checkInFavorites
import ru.feip.elisianix.common.db.favDao
import ru.feip.elisianix.databinding.ItemMainCategoryBlockProductBinding
import ru.feip.elisianix.extensions.inCurrency
import ru.feip.elisianix.extensions.setCartStatus
import ru.feip.elisianix.extensions.setFavoriteStatus
import ru.feip.elisianix.remote.models.ProductMainPreview


class ProductCategoryBlockMainListAdapter(
    private val clickListenerToProduct: (ProductMainPreview) -> Unit,
    private val clickListenerCartBtn: (ProductMainPreview) -> Unit,
    private val clickListenerFavoriteBtn: (ProductMainPreview) -> Unit,
    private val lifecycleOwner: LifecycleOwner,
) : ListAdapter<ProductMainPreview, RecyclerView.ViewHolder>(ItemCallback()) {

    inner class ProductCategoryBlockMainList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemMainCategoryBlockProductBinding.bind(item)

        init {
            binding.apply {
                productName.setOnClickListener {
                    val position = absoluteAdapterPosition
                    if (position in currentList.indices) {
                        clickListenerToProduct.invoke(currentList[position])
                    }
                }
                productImage.setOnClickListener {
                    val position = absoluteAdapterPosition
                    if (position in currentList.indices) {
                        clickListenerToProduct.invoke(currentList[position])
                    }
                }
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
                favDao.checkCntLive().observe(lifecycleOwner) {
                    val position = absoluteAdapterPosition
                    if (position in currentList.indices) {
                        val prod = currentList[position]
                        val check = checkInFavorites(prod.id)
                        if (prod.inFavorites != check) {
                            prod.inFavorites = check
                            notifyItemChanged(position)
                        }
                    }
                }
            }
        }

        fun bind(item: ProductMainPreview) {
            binding.apply {
                Glide.with(itemView).load(item.images[0].url)
                    .timeout(60000)
                    .placeholder(R.drawable.shape_placeholder)
                    .error(R.drawable.shape_placeholder)
                    .into(productImage)
                productName.text = item.name
                productPrice.inCurrency(item.price)

                productCartBtn.setCartStatus(item.inCart)
                productFavoriteBtn.setFavoriteStatus(item.inFavorites)
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
            .inflate(R.layout.item_main_category_block_product, parent, false)
        view.layoutParams.width = (parent.measuredWidth * 0.48).toInt()
        return ProductCategoryBlockMainList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductCategoryBlockMainList -> {
                val item = currentList[position]
                holder.bind(item)
            }
        }
    }
}