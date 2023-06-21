package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemMainActualBinding
import ru.feip.elisianix.extensions.disableAnimation
import ru.feip.elisianix.remote.models.MainBlock
import ru.feip.elisianix.remote.models.ProductMainPreview


class ActualMainListAdapter(
    private val clickListenerToProduct: (ProductMainPreview) -> Unit,
    private val clickListenerCartBtn: (ProductMainPreview) -> Unit,
    private val clickListenerFavoriteBtn: (ProductMainPreview) -> Unit,
    private val lifecycleOwner: LifecycleOwner,
) : ListAdapter<MainBlock, RecyclerView.ViewHolder>(ItemCallback()) {

    inner class ActualMainList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemMainActualBinding.bind(item)
        private lateinit var productActualMainAdapter: ProductActualMainListAdapter

        fun bind(item: MainBlock) {
            binding.apply {
                actualSectionName.text = item.name

                productActualMainAdapter = ProductActualMainListAdapter(
                    clickListenerToProduct,
                    clickListenerCartBtn,
                    clickListenerFavoriteBtn,
                    lifecycleOwner
                )
                productActualMainAdapter.actualName = item.tag.toString()
                recyclerProduct.disableAnimation()
                recyclerProduct.adapter = productActualMainAdapter
                recyclerProduct.layoutManager =
                    LinearLayoutManager(
                        itemView.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )

                productActualMainAdapter.submitList(item.products)
                recyclerActualSectionIndicator.attachToRecyclerView(recyclerProduct)
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
            .inflate(R.layout.item_main_actual, parent, false)
        return ActualMainList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ActualMainList -> {
                val item = currentList[position]
                holder.bind(item)
            }
        }
    }
}