package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemCartProductBinding
import ru.feip.elisianix.extensions.addStrikethrough
import ru.feip.elisianix.extensions.inCurrency
import ru.feip.elisianix.extensions.sizeFormat
import ru.feip.elisianix.remote.models.CartItemRemote


class ProductCartListAdapter(
    private val clickListenerToProduct: (CartItemRemote) -> Unit,
    private val cartItemActionsMenuClickListener: OptionsMenuClickListener,
) : ListAdapter<CartItemRemote, RecyclerView.ViewHolder>(ItemCallback()) {

    interface OptionsMenuClickListener {
        fun onOptionsMenuClicked(
            cartItem: CartItemRemote,
            position: Int,
            view: View
        )
    }

    inner class ProductCartList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemCartProductBinding.bind(item)

        init {
            binding.apply {
                cartProductImage.setOnClickListener {
                    val position = absoluteAdapterPosition
                    if (position in currentList.indices) {
                        clickListenerToProduct.invoke(currentList[position])
                    }
                }
                cartProductName.setOnClickListener {
                    val position = absoluteAdapterPosition
                    if (position in currentList.indices) {
                        clickListenerToProduct.invoke(currentList[position])
                    }
                }
                cartProductActions.setOnClickListener {
                    val position = absoluteAdapterPosition
                    if (position in currentList.indices) {
                        cartItemActionsMenuClickListener.onOptionsMenuClicked(
                            currentList[absoluteAdapterPosition], position, itemView
                        )
                    }
                }
            }
        }

        fun bind(item: CartItemRemote) {
            binding.apply {
                Glide.with(itemView).load(item.productImage.url)
                    .error(R.drawable.ic_no_image)
                    .into(cartProductImage)
                cartProductName.text = item.name
                cartProductBrand.text = item.brand.name
                cartProductCategory.text = item.category.name
                cartProductColor.text = item.productColor.name

                cartProductSize.sizeFormat(item.productSize.value)
                cartProductNewPrice.inCurrency(item.price)
                cartProductOldPrice.inCurrency(item.price)
                cartProductOldPrice.addStrikethrough()

                cartProductIsLast.isVisible = item.isLast
            }
        }
    }

    private class ItemCallback : DiffUtil.ItemCallback<CartItemRemote>() {
        override fun areItemsTheSame(
            oldItem: CartItemRemote,
            newItem: CartItemRemote
        ): Boolean = oldItem.productId == newItem.productId &&
                oldItem.productColor.id == newItem.productColor.id &&
                oldItem.productSize.id == newItem.productSize.id

        override fun areContentsTheSame(
            oldItem: CartItemRemote,
            newItem: CartItemRemote
        ): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return ProductCartList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductCartList -> {
                val item = currentList[position]
                holder.bind(item)
            }
        }
    }
}