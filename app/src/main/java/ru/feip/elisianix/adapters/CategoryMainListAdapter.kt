package ru.feip.elisianix.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.ItemMainCategoryBinding
import ru.feip.elisianix.remote.models.Category


class CategoryMainListAdapter(
    private val clickListenerCategoryPreview: (Category) -> Unit,
) : ListAdapter<Category, RecyclerView.ViewHolder>(ItemCallback()) {

    inner class CategoryMainList(item: View) : RecyclerView.ViewHolder(item) {
        private var binding = ItemMainCategoryBinding.bind(item)

        init {
            binding.apply {
                categoryImage.setOnClickListener {
                    val position = absoluteAdapterPosition
                    if (position in currentList.indices) {
                        clickListenerCategoryPreview.invoke(currentList[position])
                    }
                }
            }
        }

        fun bind(item: Category) {
            binding.apply {
                Glide.with(itemView).load(item.image.url)
                    .timeout(60000)
                    .placeholder(R.drawable.shape_placeholder)
                    .error(R.drawable.shape_placeholder)
                    .into(categoryImage)
                categoryName.text = item.name
            }
        }
    }

    private class ItemCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(
            oldItem: Category,
            newItem: Category
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Category,
            newItem: Category
        ): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_main_category, parent, false)
        return CategoryMainList(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryMainList -> {
                val item = currentList[position]
                holder.bind(item)
            }
        }
    }
}