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
import ru.feip.elisianix.common.db.cardDao
import ru.feip.elisianix.common.db.checkInCartById
import ru.feip.elisianix.common.db.checkInFavorites
import ru.feip.elisianix.common.db.favDao
import ru.feip.elisianix.databinding.ItemMainActualProductBinding
import ru.feip.elisianix.extensions.addStrikethrough
import ru.feip.elisianix.extensions.inCurrency
import ru.feip.elisianix.extensions.setCartStatus
import ru.feip.elisianix.extensions.setFavoriteStatus
import ru.feip.elisianix.remote.models.ProductMainPreview


class ProductActualMainListAdapter(
    private val clickListenerToProduct: (ProductMainPreview) -> Unit,
    private val clickListenerCartBtn: (ProductMainPreview) -> Unit,
    private val clickListenerFavoriteBtn: (ProductMainPreview) -> Unit,
    private val lifecycleOwner: LifecycleOwner,
) : ListAdapter<ProductMainPreview, RecyclerView.ViewHolder>(ItemCallback()) {

    var actualName = ""

    inner class ProductActualMainList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemMainActualProductBinding.bind(item)

        init {
            binding.apply {
                productImageContainer.setOnClickListener {
                    val position = absoluteAdapterPosition
                    if (position in currentList.indices) {
                        clickListenerToProduct.invoke(currentList[position])
                    }
                }
                productName.setOnClickListener {
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
                cardDao.checkCntLive().observe(lifecycleOwner) {
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
                    .error(R.drawable.ic_no_image)
                    .into(productImage)

                productName.text = item.name
                productActualTag.text = actualName

                productNewPrice.inCurrency(item.price)
                productOldPrice.inCurrency(item.price)
                productOldPrice.addStrikethrough()
                // TODO change old price with remote

                productCartBtn.setCartStatus(item.inCart, true)
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
            .inflate(R.layout.item_main_actual_product, parent, false)
        return ProductActualMainList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductActualMainList -> {
                val item = currentList[position]
                holder.bind(item)
            }
        }
    }
}