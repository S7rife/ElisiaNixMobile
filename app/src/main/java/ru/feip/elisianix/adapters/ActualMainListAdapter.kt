package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemMainActualBinding
import ru.feip.elisianix.remote.models.ActualSection


class ActualMainListAdapter(

) : ListAdapter<ActualSection, RecyclerView.ViewHolder>(ItemCallback()) {

    inner class ActualMainList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemMainActualBinding.bind(item)
        private lateinit var productMainAdapter: ProductMainListAdapter

        fun bind(item: ActualSection) {
            binding.apply {
                actualSectionName.text = item.name

                productMainAdapter = ProductMainListAdapter()
                recyclerProduct.adapter = productMainAdapter
                recyclerProduct.layoutManager =
                    LinearLayoutManager(
                        itemView.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )

                productMainAdapter.submitList(item.products)
            }
        }
    }

    private class ItemCallback : DiffUtil.ItemCallback<ActualSection>() {
        override fun areItemsTheSame(
            oldItem: ActualSection,
            newItem: ActualSection
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ActualSection,
            newItem: ActualSection
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