package ru.feip.elisianix.common


import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.SearchCategoriesListAdapter
import ru.feip.elisianix.adapters.SearchHistoryListAdapter
import ru.feip.elisianix.common.db.SearchQuery
import ru.feip.elisianix.databinding.FragmentSearchWidgetBinding
import ru.feip.elisianix.extensions.disableAnimation
import ru.feip.elisianix.extensions.isPreviousDest
import ru.feip.elisianix.remote.models.Category
import ru.feip.elisianix.remote.models.Image


class SearchWidgetFragment :
    BaseFragment<FragmentSearchWidgetBinding>(R.layout.fragment_search_widget) {

    private lateinit var searchCategoriesAdapter: SearchCategoriesListAdapter
    private lateinit var searchHistoryAdapter: SearchHistoryListAdapter
    private val historyDao = App.INSTANCE.db.searchHistoryDao()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyDao.deleteExcept()
        historyDao.getAllLive().observe(viewLifecycleOwner) {
            binding.youSearched.isVisible = it.isNotEmpty()
            searchHistoryAdapter.submitList(it)
        }

        val ra = requireArguments()
        val currentSearchQuery = ra.getString("search_query")
        val catIds = ra.getStringArrayList("category_ids")?.map { it.toInt() }
        val catUrls = ra.getStringArrayList("category_urls")
        val catNames = ra.getStringArrayList("category_names")

        if (catIds != null && catUrls != null && catNames != null) {
            initCategoriesAdapter(List(catIds.size) { i ->
                Category(catIds[i], catNames[i], Image(-1, "", catUrls[i]))
            })
        }

        binding.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.categoryToolbarSearch -> {}
                    R.id.categoryToolbarClose -> {
                        searchView.setQuery("", false)
                    }
                }
                true
            }

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    if (query.isNotEmpty()) {
                        doSearch(query)
                    }
                    return query.isNotEmpty()
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    toolbarUpdate()
                    return true
                }
            })

            searchView.setQuery(currentSearchQuery, false)

            searchHistoryAdapter = SearchHistoryListAdapter { doSearch(it.query) }
            searchHistoryAdapter.submitList(historyDao.getAll())
            recyclerSearchHistory.disableAnimation()
            recyclerSearchHistory.adapter = searchHistoryAdapter
            recyclerSearchHistory.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )
        }
    }

    private fun doSearch(query: String) {
        if (searchHistoryAdapter.currentList.none { it.query == query }) {
            updateSearchHistory(query)
        }

        if (findNavController().isPreviousDest(R.id.catalogCategoryFragment)) {
            setFragmentResult("resultSearchQueryDialog", bundleOf("search_query" to query))
            findNavController().popBackStack()
        } else {
            findNavController().navigate(
                R.id.action_searchWidgetFragment_to_catalogCategoryFragment,
                bundleOf("search_query" to query)
            )
        }
    }

    private fun updateSearchHistory(query: String) {
        historyDao.insert(SearchQuery(0, query))
        historyDao.deleteExcept()
    }

    private fun toolbarUpdate() {
        binding.apply {
            val focus = searchView.query.isNotEmpty()
            toolbar.menu.findItem(R.id.categoryToolbarClose).isVisible = focus
            toolbar.menu.findItem(R.id.categoryToolbarSearch).isVisible = !focus
        }
    }

    private fun initCategoriesAdapter(lst: List<Category>) {
        searchCategoriesAdapter = SearchCategoriesListAdapter {
            findNavController().navigate(
                R.id.action_searchWidgetFragment_to_catalogCategoryFragment,
                bundleOf("category_id" to it.id.toString(), "section_name" to it.name)
            )
        }
        binding.apply {
            recyclerSearchCategories.disableAnimation()
            recyclerSearchCategories.adapter = searchCategoriesAdapter
            recyclerSearchCategories.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )

            searchCategoriesAdapter.submitList(lst)
            categories.isVisible = true
        }
    }
}
