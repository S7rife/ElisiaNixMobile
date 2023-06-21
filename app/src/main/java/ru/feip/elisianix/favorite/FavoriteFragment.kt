package ru.feip.elisianix.favorite

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.ProductFavoriteListAdapter
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.common.db.checkInFavorites
import ru.feip.elisianix.common.db.editItemInFavorites
import ru.feip.elisianix.databinding.FragmentFavoriteBinding
import ru.feip.elisianix.extensions.disableAnimation
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.favorite.view_models.FavoriteViewModel
import ru.feip.elisianix.remote.models.ProductMainPreview
import ru.feip.elisianix.remote.models.emptyAuthBundle
import ru.feip.elisianix.remote.models.toCartDialogData

class FavoriteFragment : BaseFragment<FragmentFavoriteBinding>(R.layout.fragment_favorite) {

    private val viewModel by lazy {
        ViewModelProvider(this)[FavoriteViewModel::class.java]
    }

    private lateinit var productFavoriteAdapter: ProductFavoriteListAdapter
    private val favDao = App.INSTANCE.db.FavoritesDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getFavoritesNoAuth()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favDao.checkCntLive().observe(viewLifecycleOwner) { updateAdapterFromOther() }

        binding.apply {
            toolbar.title = getString(R.string.favorite).uppercase()
            swipeRefresh.setOnRefreshListener { updateAdapterFromOther() }

            productFavoriteAdapter = ProductFavoriteListAdapter(
                {
                    toProductScreen(it.productId, it.categoryId)
                },
                {
                    openAddToCartDialog(it)
                },
                {
                    editFavorites(it.id)
                },
                (viewLifecycleOwner)
            )
            recyclerFavoriteProducts.disableAnimation()
            recyclerFavoriteProducts.adapter = productFavoriteAdapter
            recyclerFavoriteProducts.layoutManager = GridLayoutManager(requireContext(), 2)

            updateUi()
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.favorites
            .onEach {
                productFavoriteAdapter.submitList(it)
                binding.swipeRefresh.isRefreshing = false
                updateUi()
            }
            .launchWhenStarted(lifecycleScope)
    }

    private fun openAddToCartDialog(item: ProductMainPreview) {
        val bundle = toCartDialogData(item)
        bundle?.apply {
            val navController = findNavController(requireView())
            val graph = navController.graph
            val walletGraph = graph.findNode(R.id.nav_graph_catalog) as NavGraph
            walletGraph.setStartDestination(R.id.catalogAddToCartDialog)
            navController.navigate(R.id.action_favoriteFragment_to_nav_graph_catalog, bundle)
        }
    }

    private fun updateAdapterFromOther() {
        val lst = productFavoriteAdapter.currentList
        productFavoriteAdapter.submitList(lst.filter { checkInFavorites(it.id) })
        viewModel.getFavoritesNoAuth()
    }

    private fun updateUi() {
        val favListVis = productFavoriteAdapter.currentList.isNotEmpty()
        val favVis = favDao.getAll().isNotEmpty()
        binding.emptyState.isVisible = !favVis && !favListVis
    }

    private fun toProductScreen(productId: Int, categoryId: Int) {
        val navController = findNavController(requireView())
        val graph = navController.graph
        val walletGraph = graph.findNode(R.id.nav_graph_catalog) as NavGraph
        walletGraph.setStartDestination(R.id.catalogProductFragment)
        navController.navigate(
            R.id.action_favoriteFragment_to_nav_graph_catalog,
            bundleOf("product_id" to productId, "category_id" to categoryId)
        )
    }

    private fun editFavorites(productId: Int) {
        when (App.AUTH) {
            true -> editItemInFavorites(productId)
            false -> findNavController(requireActivity(), R.id.rootActivityContainer)
                .navigate(R.id.action_navBottomFragment_to_noAuthFirstFragment, emptyAuthBundle)
        }
    }
}