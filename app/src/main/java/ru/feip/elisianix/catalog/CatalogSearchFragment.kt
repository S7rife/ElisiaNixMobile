package ru.feip.elisianix.catalog


import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.ProductCategoryBlockMainListAdapter
import ru.feip.elisianix.adapters.SearchCategoriesListAdapter
import ru.feip.elisianix.adapters.SearchHistoryListAdapter
import ru.feip.elisianix.catalog.view_models.CatalogSearchViewModel
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.common.db.SearchQuery
import ru.feip.elisianix.databinding.FragmentCatalogSearchBinding
import ru.feip.elisianix.extensions.launchWhenStarted
import kotlin.properties.Delegates


class CatalogSearchFragment :
    BaseFragment<FragmentCatalogSearchBinding>(R.layout.fragment_catalog_search) {

    private lateinit var searchCategoriesAdapter: SearchCategoriesListAdapter
    private lateinit var searchHistoryAdapter: SearchHistoryListAdapter
    private lateinit var searchResultAdapter: ProductCategoryBlockMainListAdapter
    private var currentSearchHistory: List<SearchQuery> by Delegates.observable(mutableListOf()) { property, oldValue, newValue ->
        searchHistoryAdapter.submitList(newValue)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[CatalogSearchViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.INSTANCE.db.searchHistoryDao().deleteExcept()
        App.INSTANCE.db.searchHistoryDao().getAll().observe(viewLifecycleOwner) {
            currentSearchHistory = it
        }

        binding.apply {
            toolbarSearch.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            toolbarSearch.setOnMenuItemClickListener {
                // TODO make focus on search field
                searchCatalogView.requestFocus()
            }


            searchCatalogView.setOnQueryTextFocusChangeListener { _, onFocus ->
                // TODO suggestions
                changeFocus(searchInFocus = onFocus)
                emptyStateContainer.isVisible =
                    searchResultAdapter.currentList.isEmpty() and !onFocus and searchCatalogView.query.isNotEmpty()
            }

            searchCatalogView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    if (query.isNotEmpty()) {
                        changeFocus(searchInFocus = false)
                        doSearch(query)
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    toolbarSearch.menu.findItem(R.id.action_search).isVisible =
                        newText.isEmpty()
                    emptyStateContainer.isVisible =
                        searchResultAdapter.currentList.isEmpty() and newText.isEmpty() and !searchToolsContainer.isVisible
                    return false
                }
            })


            searchCategoriesAdapter = SearchCategoriesListAdapter {
                findNavController().navigate(
                    R.id.action_catalogSearchFragment_to_catalogCategoryFragment,
                    bundleOf("category_id" to it.id, "category_name" to it.name)
                )
            }
            recyclerSearchCategories.adapter = searchCategoriesAdapter
            recyclerSearchCategories.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )

            searchHistoryAdapter = SearchHistoryListAdapter {
                searchCatalogView.setQuery(it.query, true)
                doSearch(it.query)
            }
            recyclerSearchHistory.adapter = searchHistoryAdapter
            recyclerSearchHistory.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )

            searchResultAdapter = ProductCategoryBlockMainListAdapter {
                findNavController().navigate(
                    R.id.action_catalogSearchFragment_to_catalogProductFragment,
                    bundleOf("product_id" to it.id)
                )
            }
            recyclerSearchResult.adapter = searchResultAdapter
            recyclerSearchResult.layoutManager = GridLayoutManager(requireContext(), 2)

            App.INSTANCE.db.searchHistoryDao().getAll().observe(viewLifecycleOwner) {
                currentSearchHistory = it
            }
        }

        viewModel.showLoading
            .onEach { binding.apply { loader.isVisible = it } }
            .launchWhenStarted(lifecycleScope)

        viewModel.categories
            .onEach {
                binding.categories.visibility = View.VISIBLE
                searchCategoriesAdapter.submitList(it)
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.products
            .onEach {
                searchResultAdapter.submitList(it)
                binding.apply {
                    changeFocus(searchInFocus = false)
                    emptyStateContainer.isVisible = it.isEmpty()
                }
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.getCategories()
    }

    private fun doSearch(query: String) {
        binding.apply {
            searchCatalogView.clearFocus()
            emptyStateContainer.isVisible = false
        }
        viewModel.getSearchProducts(query)
        if (currentSearchHistory.none { it.query == query }) {
            updateSearchHistory(query)
        }
    }

    private fun updateSearchHistory(query: String) {
        App.INSTANCE.db.searchHistoryDao().insert(SearchQuery(0, query))
        App.INSTANCE.db.searchHistoryDao().deleteExcept()
        App.INSTANCE.db.searchHistoryDao().getAll().observe(viewLifecycleOwner) {
            currentSearchHistory = it
        }
    }

    private fun changeFocus(searchInFocus: Boolean) {
        binding.apply {
            searchToolsContainer.isVisible = searchInFocus
            recyclerSearchResult.isVisible = !searchInFocus
            if (searchInFocus) {
                emptyStateContainer.isVisible = false
            }
        }
    }
}
