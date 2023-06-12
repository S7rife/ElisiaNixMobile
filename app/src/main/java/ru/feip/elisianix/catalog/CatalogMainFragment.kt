package ru.feip.elisianix.catalog

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.ActualMainListAdapter
import ru.feip.elisianix.adapters.CategoryBlockMainListAdapter
import ru.feip.elisianix.adapters.CategoryMainListAdapter
import ru.feip.elisianix.catalog.view_models.CatalogMainViewModel
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.databinding.FragmentCatalogMainBinding
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.remote.models.MainBlock
import ru.feip.elisianix.remote.models.checkInCart
import ru.feip.elisianix.remote.models.editItemInCart

class CatalogMainFragment :
    BaseFragment<FragmentCatalogMainBinding>(R.layout.fragment_catalog_main) {

    private val viewModel by lazy {
        ViewModelProvider(this)[CatalogMainViewModel::class.java]
    }

    private lateinit var categoryMainAdapter: CategoryMainListAdapter
    private lateinit var actualMainAdapter: ActualMainListAdapter
    private lateinit var categoryBlockMainAdapter: CategoryBlockMainListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            searchCatalogView.setOnClickListener {
                findNavController().navigate(
                    R.id.action_catalogMainFragment_to_searchWidgetFragment,
                    bundleOf("sort_method" to null)
                )
            }
            categoryMainAdapter = CategoryMainListAdapter {
                findNavController().navigate(
                    R.id.action_catalogMainFragment_to_catalogCategoryFragment,
                    bundleOf("category_id" to it.id.toString(), "section_name" to it.name)
                )
            }
            recyclerCategoriesPreview.adapter = categoryMainAdapter
            recyclerCategoriesPreview.layoutManager = GridLayoutManager(requireContext(), 3)

            actualMainAdapter = ActualMainListAdapter(
                {
                    findNavController().navigate(
                        R.id.action_catalogMainFragment_to_catalogProductFragment,
                        bundleOf("product_id" to it.id)
                    )
                },
                {
                    editItemInCart(it)
                    it.inCart = checkInCart(it)
                },
                {

                }
            )
            recyclerActual.adapter = actualMainAdapter
            recyclerActual.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )

            categoryBlockMainAdapter = CategoryBlockMainListAdapter(
                {
                    findNavController().navigate(
                        R.id.action_catalogMainFragment_to_catalogCategoryFragment,
                        bundleOf("category_id" to it.id.toString(), "section_name" to it.name)
                    )
                },
                {
                    findNavController().navigate(
                        R.id.action_catalogMainFragment_to_catalogProductFragment,
                        bundleOf("product_id" to it.id)
                    )
                },
                {
                    editItemInCart(it)
                    it.inCart = checkInCart(it)
                },
                {

                }
            )
            recyclerCategoryBlocks.adapter = categoryBlockMainAdapter
            recyclerCategoryBlocks.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )

            swipeRefresh.setOnRefreshListener {
                viewModel.getCategories()
            }
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.categories
            .onEach {
                categoryMainAdapter.submitList(it)
                binding.swipeRefresh.isRefreshing = false
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.productActualBlocks
            .onEach {
                if (it.new != null && it.discount != null) {
                    actualMainAdapter.submitList(
                        listOf(
                            MainBlock(
                                0, getString(R.string.new_arrivals),
                                it.new!!.products, getString(R.string.new_)
                            ),
                            MainBlock(
                                1, getString(R.string.discounts),
                                it.discount!!.products, getString(R.string.best_price)
                            )
                        )
                    )
                }
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.productCategoryBlocks
            .onEach { categoryBlockMainAdapter.submitList(it) }
            .launchWhenStarted(lifecycleScope)

        viewModel.getCategories()
    }
}