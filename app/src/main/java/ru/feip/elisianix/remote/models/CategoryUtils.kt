package ru.feip.elisianix.remote.models

class CategoryUtil(categories: List<CategoryMainPreview>) {
    private var ids: List<Pair<Int, MutableList<Int>>>

    init {
        ids = categories.map { Pair(it.id, mutableListOf()) }
        recurCat(categories)
    }

    private fun recurCat(cat: List<CategoryMainPreview>) {
        cat.forEach {
            recList(it.id, it.subCategories)
        }
    }

    private fun recList(level0Id: Int, parentList: List<CategoryMainPreview>?) {
        parentList?.forEach { child ->
            val idx = ids.indexOfFirst { it.first == level0Id }
            ids[idx].second.plusAssign(child.id)
            recList(level0Id, child.subCategories)
        }
    }

    fun getLevel0Idx(prod: ProductMainPreview): Int {
        return ids.indexOfFirst { prod.category.id in it.second }
    }
}



