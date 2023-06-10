package ru.feip.elisianix.common


import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.SearchCategoriesListAdapter
import ru.feip.elisianix.adapters.SearchHistoryListAdapter
import ru.feip.elisianix.common.db.SearchQuery
import ru.feip.elisianix.databinding.FragmentSearchWidgetBinding
import ru.feip.elisianix.extensions.isPreviousDest
import ru.feip.elisianix.extensions.launchWhenStarted
import kotlin.properties.Delegates


class SearchWidgetFragment :
    BaseFragment<FragmentSearchWidgetBinding>(R.layout.fragment_search_widget) {

    private lateinit var searchCategoriesAdapter: SearchCategoriesListAdapter
    private lateinit var searchHistoryAdapter: SearchHistoryListAdapter
    private var currentSearchHistory: List<SearchQuery> by Delegates.observable(mutableListOf()) { _, _, newValue ->
        searchHistoryAdapter.submitList(newValue)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[SearchWidgetViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentSearchQuery = requireArguments().getString("search_query")

        App.INSTANCE.db.searchHistoryDao().deleteExcept()
        App.INSTANCE.db.searchHistoryDao().getAll().observe(viewLifecycleOwner) {
            currentSearchHistory = it
            binding.youSearched.isVisible = it.isNotEmpty()
        }

        binding.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.category_toolbar_search -> {}
                    R.id.category_toolbar_close -> {
                        searchView.setQuery("", false)
                    }
                }
                true
            }

            searchView.setOnQueryTextFocusChangeListener { _, onFocus ->
                // TODO suggestions
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

            searchCategoriesAdapter = SearchCategoriesListAdapter {
                findNavController().navigate(
                    R.id.action_searchWidgetFragment_to_catalogCategoryFragment,
                    bundleOf("category_id" to it.id.toString(), "section_name" to it.name)
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
                searchView.setQuery(it.query, true)
            }
            recyclerSearchHistory.adapter = searchHistoryAdapter
            recyclerSearchHistory.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )

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

        viewModel.getCategories()
    }

    private fun doSearch(query: String) {
        if (currentSearchHistory.none { it.query == query }) {
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
        App.INSTANCE.db.searchHistoryDao().insert(SearchQuery(0, query))
        App.INSTANCE.db.searchHistoryDao().deleteExcept()
        App.INSTANCE.db.searchHistoryDao().getAll().observe(viewLifecycleOwner) {
            currentSearchHistory = it
        }
    }

    private fun toolbarUpdate() {
        binding.apply {
            val focus = searchView.query.isNotEmpty()
            toolbar.menu.findItem(R.id.category_toolbar_close).isVisible = focus
            toolbar.menu.findItem(R.id.category_toolbar_search).isVisible = !focus
        }
    }
}
