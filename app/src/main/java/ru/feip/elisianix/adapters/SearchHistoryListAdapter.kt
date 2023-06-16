package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.feip.elisianix.R
import ru.feip.elisianix.common.db.SearchQuery
import ru.feip.elisianix.databinding.ItemSearchToolBinding


class SearchHistoryListAdapter(
    private val clickListenerHistory: (SearchQuery) -> Unit
) : ListAdapter<SearchQuery, RecyclerView.ViewHolder>(ItemCallback()) {


    inner class SearchHistoryList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemSearchToolBinding.bind(item)

        init {
            binding.itemSearchTool.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position in currentList.indices) {
                    clickListenerHistory.invoke(currentList[position])
                }
            }
        }

        fun bind(item: SearchQuery) {
            binding.apply {
                Glide.with(itemView).load(R.drawable.ic_recently)
                    .error(R.drawable.ic_no_image)
                    .into(searchToolImage)
                searchToolName.text = item.query
            }
        }
    }

    private class ItemCallback : DiffUtil.ItemCallback<SearchQuery>() {
        override fun areItemsTheSame(
            oldItem: SearchQuery,
            newItem: SearchQuery
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: SearchQuery,
            newItem: SearchQuery
        ): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_tool, parent, false)
        return SearchHistoryList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchHistoryList -> {
                val item = currentList[position]
                holder.bind(item)
            }
        }
    }
}