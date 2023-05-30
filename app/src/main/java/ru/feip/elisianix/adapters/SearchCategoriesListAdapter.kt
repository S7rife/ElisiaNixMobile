package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemSearchToolBinding
import ru.feip.elisianix.remote.models.CategoryMainPreview


class SearchCategoriesListAdapter(
    private val clickListenerToCategory: (CategoryMainPreview) -> Unit
) : ListAdapter<CategoryMainPreview, RecyclerView.ViewHolder>(ItemCallback()) {


    inner class SearchCategoriesList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemSearchToolBinding.bind(item)

        init {
            item.setOnClickListener {
                val position = adapterPosition
                if (position in currentList.indices) {
                    clickListenerToCategory.invoke(currentList[position])
                }
            }
        }

        fun bind(item: CategoryMainPreview) {
            binding.apply {
                Glide.with(itemView).load(item.image.url)
                    .error(R.drawable.ic_no_image)
                    .into(searchToolImage)
                searchToolName.text = item.name
            }
        }
    }

    private class ItemCallback : DiffUtil.ItemCallback<CategoryMainPreview>() {
        override fun areItemsTheSame(
            oldItem: CategoryMainPreview,
            newItem: CategoryMainPreview
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: CategoryMainPreview,
            newItem: CategoryMainPreview
        ): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_tool, parent, false)
        return SearchCategoriesList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchCategoriesList -> {
                val item = currentList[position]
                holder.bind(item)
            }
        }
    }
}