package ru.feip.elisianix.favorite

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.ProductFavoriteListAdapter
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.common.db.checkInCart
import ru.feip.elisianix.common.db.checkInFavorites
import ru.feip.elisianix.common.db.editItemInCart
import ru.feip.elisianix.common.db.editItemInFavorites
import ru.feip.elisianix.databinding.FragmentFavoriteBinding
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.favorite.view_models.FavoriteViewModel

class FavoriteFragment : BaseFragment<FragmentFavoriteBinding>(R.layout.fragment_favorite) {

    private val viewModel by lazy {
        ViewModelProvider(this)[FavoriteViewModel::class.java]
    }

    private lateinit var productFavoriteAdapter: ProductFavoriteListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.INSTANCE.db.FavoritesDao().getAllLive().observe(viewLifecycleOwner) { list ->
            viewModel.getFavoritesNoAuth(list.map { it.productId })
            binding.emptyState.isVisible = list.isEmpty()
        }

        binding.apply {
            toolbar.title = getString(R.string.favorite).uppercase()
            swipeRefresh.setOnRefreshListener {
                viewModel.getFavoritesNoAuth(
                    App.INSTANCE.db.FavoritesDao().getAll().map { it.productId })
            }

            productFavoriteAdapter = ProductFavoriteListAdapter(
                {
                    val navController = Navigation.findNavController(view)
                    val graph = navController.graph
                    val walletGraph = graph.findNode(R.id.nav_graph_catalog) as NavGraph
                    walletGraph.setStartDestination(R.id.catalogProductFragment)
                    navController.navigate(
                        R.id.action_favoriteFragment_to_nav_graph_catalog,
                        bundleOf("product_id" to it.first)
                    )
                },
                {
                    editItemInCart(it.id)
                    it.inCart = checkInCart(it.id)
                },
                {
                    editItemInFavorites(it.id)
                    it.inFavorites = checkInFavorites(it.id)
                    updateFavoriteList()
                }
            )
            recyclerFavoriteProducts.adapter = productFavoriteAdapter
            recyclerFavoriteProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.favorites
            .onEach { lst ->
                productFavoriteAdapter.submitList(lst.filter { checkInFavorites(it.id) })
                binding.swipeRefresh.isRefreshing = false
            }
            .launchWhenStarted(lifecycleScope)
    }

    private fun updateFavoriteList() {
        val newLst = productFavoriteAdapter.currentList.filter { checkInFavorites(it.id) }
        productFavoriteAdapter.submitList(newLst)
    }
}