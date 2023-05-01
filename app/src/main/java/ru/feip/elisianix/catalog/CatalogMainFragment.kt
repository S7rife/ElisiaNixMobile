package ru.feip.elisianix.catalog

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import ru.feip.elisianix.R
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.adapters.CategoryMainListAdapter
import ru.feip.elisianix.catalog.view_models.CatalogMainViewModel
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.databinding.FragmentCatalogMainBinding
import ru.feip.elisianix.extensions.launchWhenStarted

class CatalogMainFragment :
    BaseFragment<FragmentCatalogMainBinding>(R.layout.fragment_catalog_main) {

    private val viewModel by lazy {
        ViewModelProvider(this)[CatalogMainViewModel::class.java]
    }

    private lateinit var adapter: CategoryMainListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            adapter = CategoryMainListAdapter()

            recycler.adapter = adapter
            recycler.layoutManager = GridLayoutManager(requireContext(), 3)
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.categories
            .onEach {
                adapter.submitList(it)
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.getCategories()
    }
}