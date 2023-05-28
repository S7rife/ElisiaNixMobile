package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemMainCategoryBlockBinding
import ru.feip.elisianix.remote.models.MainBlock
import ru.feip.elisianix.remote.models.ProductMainPreview


class CategoryBlockMainListAdapter(
    private val clickListenerToCategory: (MainBlock) -> Unit,
    private val clickListenerToProduct: (ProductMainPreview) -> Unit,
) : ListAdapter<MainBlock, RecyclerView.ViewHolder>(ItemCallback()) {

    inner class CategoryBlockMainList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemMainCategoryBlockBinding.bind(item)
        private lateinit var productCategoryBlockAdapter: ProductCategoryBlockMainListAdapter

        init {
            binding.apply {
                categoryBlockToCategoryBtn.setOnClickListener {
                    val position = adapterPosition
                    if (position in currentList.indices) {
                        clickListenerToCategory.invoke(currentList[position])
                    }
                }
            }
        }


        fun bind(item: MainBlock) {
            binding.apply {
                categoryBlockName.text = item.name

                productCategoryBlockAdapter =
                    ProductCategoryBlockMainListAdapter(clickListenerToProduct)
                recyclerProduct.adapter = productCategoryBlockAdapter
                recyclerProduct.layoutManager =
                    LinearLayoutManager(
                        itemView.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )

                productCategoryBlockAdapter.submitList(item.products)
            }
        }
    }

    private class ItemCallback : DiffUtil.ItemCallback<MainBlock>() {
        override fun areItemsTheSame(
            oldItem: MainBlock,
            newItem: MainBlock
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: MainBlock,
            newItem: MainBlock
        ): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_main_category_block, parent, false)
        return CategoryBlockMainList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryBlockMainList -> {
                val item = currentList[position]
                holder.bind(item)
            }
        }
    }
}