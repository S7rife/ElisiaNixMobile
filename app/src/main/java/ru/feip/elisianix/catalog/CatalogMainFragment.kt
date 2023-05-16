package ru.feip.elisianix.catalog

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import ru.feip.elisianix.R
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.adapters.ActualMainListAdapter
import ru.feip.elisianix.adapters.CategoryMainListAdapter
import ru.feip.elisianix.catalog.view_models.CatalogMainViewModel
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.databinding.FragmentCatalogMainBinding
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.remote.models.ActualSection

class CatalogMainFragment :
    BaseFragment<FragmentCatalogMainBinding>(R.layout.fragment_catalog_main) {

    private val viewModel by lazy {
        ViewModelProvider(this)[CatalogMainViewModel::class.java]
    }

    private lateinit var categoryMainAdapter: CategoryMainListAdapter
    private lateinit var actualMainAdapter: ActualMainListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            categoryMainAdapter = CategoryMainListAdapter()
            actualMainAdapter = ActualMainListAdapter()

            recyclerCategoriesPreview.adapter = categoryMainAdapter
            recyclerCategoriesPreview.layoutManager = GridLayoutManager(requireContext(), 3)

            recyclerActual.adapter = actualMainAdapter
            recyclerActual.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.categories
            .onEach {
                val lst = it.toMutableList().plus(it).plus(it)
                categoryMainAdapter.submitList(lst)
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.newProducts
            .onEach {
                actualMainAdapter.submitList(
                    listOf(ActualSection(0, getString(R.string.new_arrivals), it))
                )
                viewModel.getDiscountProducts()
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.discountProducts
            .onEach {
                val lst = actualMainAdapter.currentList.toMutableList()
                lst.plusAssign(ActualSection(1, getString(R.string.discounts), it))
                actualMainAdapter.submitList(lst)

            }
            .launchWhenStarted(lifecycleScope)

        viewModel.getCategories()
//        viewModel.getNewProducts()
    }
}