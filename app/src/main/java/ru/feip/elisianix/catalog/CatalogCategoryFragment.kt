package ru.feip.elisianix.catalog

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.ProductCategoryListAdapter
import ru.feip.elisianix.catalog.view_models.CatalogCategoryViewModel
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.databinding.FragmentCatalogCategoryBinding
import ru.feip.elisianix.extensions.launchWhenStarted


class CatalogCategoryFragment :
    BaseFragment<FragmentCatalogCategoryBinding>(R.layout.fragment_catalog_category) {

    private val viewModel by lazy {
        ViewModelProvider(this)[CatalogCategoryViewModel::class.java]
    }

    private lateinit var productCategoryAdapter: ProductCategoryListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryId = requireArguments().getInt("category_id")
        val categoryName = requireArguments().getString("category_name")


        binding.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.title = categoryName?.uppercase()
            productCategoryAdapter = ProductCategoryListAdapter()
            recyclerCatalogCategory.adapter = productCategoryAdapter
            recyclerCatalogCategory.layoutManager = GridLayoutManager(requireContext(), 2)
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.products
            .onEach {
                productCategoryAdapter.submitList(it)
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.getCategoryProducts(categoryId)
    }
}