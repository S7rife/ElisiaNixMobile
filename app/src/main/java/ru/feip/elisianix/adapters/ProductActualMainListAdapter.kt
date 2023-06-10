package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemMainActualProductBinding
import ru.feip.elisianix.extensions.addStrikethrough
import ru.feip.elisianix.extensions.inCurrency
import ru.feip.elisianix.remote.models.ProductMainPreview


class ProductActualMainListAdapter(
    private val clickListenerToProduct: (ProductMainPreview) -> Unit,
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
            }
        }

        fun bind(item: ProductMainPreview) {
            binding.apply {
                Glide.with(itemView).load(item.images[0].url)
                    .error(R.drawable.ic_no_image)
                    .into(productImage)

                productName.text = item.name
                productActualTag.text = actualName

                val cur = itemView.resources.getString(R.string.currency)
                productNewPrice.text = item.price.inCurrency(cur)
                productOldPrice.addStrikethrough(item.price.inCurrency(cur))
                // TODO change old price with remote
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