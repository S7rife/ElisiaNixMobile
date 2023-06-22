package ru.feip.elisianix.catalog

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.ProductCategoryListAdapter
import ru.feip.elisianix.catalog.view_models.CatalogCategoryViewModel
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.common.db.editItemInFavorites
import ru.feip.elisianix.databinding.FragmentCatalogCategoryBinding
import ru.feip.elisianix.extensions.disableAnimation
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.extensions.smoothScrollToTop
import ru.feip.elisianix.remote.models.ProductMainPreview
import ru.feip.elisianix.remote.models.SearchSettings
import ru.feip.elisianix.remote.models.emptyAuthBundle
import ru.feip.elisianix.remote.models.getFromLocale
import ru.feip.elisianix.remote.models.sortMethods
import ru.feip.elisianix.remote.models.toCartDialogData
import kotlin.properties.Delegates


class CatalogCategoryFragment :
    BaseFragment<FragmentCatalogCategoryBinding>(R.layout.fragment_catalog_category) {

    private val viewModel by lazy {
        ViewModelProvider(this)[CatalogCategoryViewModel::class.java]
    }

    private lateinit var productCategoryAdapter: ProductCategoryListAdapter
    private var orderChanged = false
    private var searchWidgetBundle: Bundle? = null
    private var searchSettings by Delegates.observable(SearchSettings()) { _, _, newSettings ->
        if (newSettings.safe) {
            searchFocus()
            viewModel.getProductsByFilters(newSettings)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener("resultSortMethodDialog") { _, bundle ->
            val newSortMethod = bundle.getInt("sort_method")
            if (searchSettings.sortMethod.value.first != newSortMethod) {
                orderChanged = true
                searchSettings = searchSettings.copy(
                    sortMethod = sortMethods.first { newSortMethod == it.value.first }
                )
            }
        }

        setFragmentResultListener("resultSearchQueryDialog") { _, bundle ->
            val newSearchQuery = bundle.getString("search_query")
            searchSettings = searchSettings.copy(query = newSearchQuery)
        }

        searchSettings = SearchSettings(
            safe = false,
            query = requireArguments().getString("search_query"),
            categoryId = requireArguments().getString("category_id")?.toIntOrNull(),
            brandId = requireArguments().getString("brand_id")?.toIntOrNull(),
        )
        viewModel.getCategories()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!searchSettings.safe) searchSettings = searchSettings.copy(safe = true)

        val sectionName = requireArguments().getString("section_name")
        searchFocus()

        binding.apply {
            searchViewContainer.isChecked = true
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.title = sectionName?.uppercase() ?: getString(R.string.search).uppercase()

            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.categoryToolbarSearch -> {
                        toSearchWidget()
                    }

                    R.id.categoryToolbarClose -> {
                        searchSettings = searchSettings.copy(query = null)
                        emptyState.isVisible = false
                    }
                }
                true
            }

            searchViewContainer.setOnClickListener { toSearchWidget() }

            categorySortingBtn.setOnClickListener {
                findNavController().navigate(
                    R.id.action_catalogCategoryFragment_to_catalogSortMethodDialog,
                    bundleOf("sort_method" to searchSettings.sortMethod.value.first)
                )
            }

            productCategoryAdapter = ProductCategoryListAdapter(
                {
                    findNavController().navigate(
                        R.id.action_catalogCategoryFragment_to_catalogProductFragment,
                        bundleOf("product_id" to it.productId, "category_id" to it.categoryId)
                    )
                },
                {
                    openAddToCartDialog(it)
                },
                {
                    editFavorites(it.id)
                },
                (viewLifecycleOwner)
            )
            recyclerCatalogCategory.disableAnimation()
            recyclerCatalogCategory.adapter = productCategoryAdapter
            recyclerCatalogCategory.layoutManager = GridLayoutManager(requireContext(), 2)

            swipeRefresh.setOnRefreshListener { viewModel.getProductsByFilters(searchSettings) }
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.categories
            .onEach { lst ->
                searchWidgetBundle = bundleOf(
                    "category_ids" to lst.map { it.id.toString() },
                    "category_urls" to lst.map { it.image.url },
                    "category_names" to lst.map { it.name }
                )
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.products
            .onEach { updateList(it) }
            .launchWhenStarted(lifecycleScope)
    }

    private fun searchFocus() {
        binding.apply {
            val focus = !searchSettings.query.isNullOrEmpty()
            searchViewContainer.isVisible = focus

            toolbar.menu.findItem(R.id.categoryToolbarClose).isVisible = focus
            toolbar.menu.findItem(R.id.categoryToolbarSearch).isVisible = !focus

            categorySortingBtn.text = searchSettings.sortMethod.getFromLocale(requireContext())
            currentSearchQuery.text = searchSettings.query.orEmpty()
        }
    }

    private fun updateList(it: List<ProductMainPreview>) {
        productCategoryAdapter.submitList(it)
        binding.apply {
            emptyState.isVisible = it.isEmpty()
            swipeRefresh.isRefreshing = false
            if (orderChanged) recyclerCatalogCategory.smoothScrollToTop()
            orderChanged = false
        }
    }

    private fun openAddToCartDialog(item: ProductMainPreview) {
        val bundle = toCartDialogData(item)
        bundle?.apply {
            findNavController().navigate(
                R.id.action_catalogCategoryFragment_to_catalogAddToCartDialog, bundle
            )
        }
    }

    private fun toSearchWidget() {
        if (searchWidgetBundle != null) {
            val bundle = searchWidgetBundle
            bundle!!.putString("search_query", binding.currentSearchQuery.text.toString())
            findNavController().navigate(
                R.id.action_catalogCategoryFragment_to_searchWidgetFragment, bundle
            )
        }
    }

    private fun editFavorites(productId: Int) {
        when (App.AUTH) {
            true -> editItemInFavorites(productId)
            false -> findNavController(requireActivity(), R.id.rootActivityContainer)
                .navigate(R.id.action_navBottomFragment_to_noAuthFirstFragment, emptyAuthBundle)
        }
    }

    override fun onResume() {
        super.onResume()
        searchSettings = searchSettings
    }
}