package ru.feip.elisianix.cart

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.ProductCartListAdapter
import ru.feip.elisianix.adapters.ProductCategoryBlockMainListAdapter
import ru.feip.elisianix.cart.view_models.CartViewModel
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.common.db.CartItem
import ru.feip.elisianix.common.db.checkInCart
import ru.feip.elisianix.common.db.checkInFavorites
import ru.feip.elisianix.common.db.editItemInCart
import ru.feip.elisianix.common.db.editItemInFavorites
import ru.feip.elisianix.databinding.FragmentCartBinding
import ru.feip.elisianix.extensions.disableAnimation
import ru.feip.elisianix.extensions.inCurrency
import ru.feip.elisianix.extensions.inStockUnits
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.remote.models.ProductMainPreview
import ru.feip.elisianix.remote.models.emptyAuthBundle
import ru.feip.elisianix.remote.models.toCartDialogData

class CartFragment : BaseFragment<FragmentCartBinding>(R.layout.fragment_cart) {

    private val viewModel by lazy {
        ViewModelProvider(this)[CartViewModel::class.java]
    }

    private lateinit var productCartAdapter: ProductCartListAdapter
    private lateinit var productLikedAdapter: ProductCategoryBlockMainListAdapter
    private val cartDao = App.INSTANCE.db.CartDao()
    private val favDao = App.INSTANCE.db.FavoritesDao()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cartDao.getAllLive().observe(viewLifecycleOwner) { updateAdaptersFromOther() }
        favDao.getAllLive().observe(viewLifecycleOwner) { updateAdaptersFromOther() }

        binding.apply {
            toolbarCart.title = getString(R.string.cart).uppercase()
            toBuyBtn.text = getString(R.string.check_out)
            toBuyBtn.setOnClickListener {
                goToCheckOut()
            }

            swipeRefresh.setOnRefreshListener { updateAdaptersFromOther() }

            productCartAdapter = ProductCartListAdapter(
                {
                    toProductScreen(it.productId, it.productColor.id, it.productSize.id)
                },
                object : ProductCartListAdapter.OptionsMenuClickListener {
                    override fun onOptionsMenuClicked(
                        id: Int,
                        colorId: Int,
                        sizeId: Int,
                        view: View
                    ) {
                        performCartItemActionsMenuClick(id, colorId, sizeId, view)
                    }
                },
            )
            recyclerCartProducts.disableAnimation()
            recyclerCartProducts.adapter = productCartAdapter
            recyclerCartProducts.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )

            productLikedAdapter = ProductCategoryBlockMainListAdapter(
                {
                    toProductScreen(it.id)
                },
                {
                    openAddToCartDialog(it)
                },
                {
                    editFavorites(it.id)
                }
            )
            recyclerLiked.disableAnimation()
            recyclerLiked.adapter = productLikedAdapter
            recyclerLiked.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            recyclerLikedIndicator.attachToRecyclerView(recyclerLiked)

            updateUi()
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.productUpdatedInRemote
            .onEach { editItemInCart(it) }
            .launchWhenStarted(lifecycleScope)

        viewModel.cart
            .onEach { cart ->
                productCartAdapter.submitList(cart.items.filter { checkInCart(it) })
                binding.apply {
                    cartTotalCountValue.inStockUnits(cart.itemsCount)
                    cartTotalPayableValue.inCurrency(cart.finalPrice)
                    cartDiscountSumValue.inCurrency(cart.discountPrice)
                    cartTotalSumValue.inCurrency(cart.totalPrice)
                    swipeRefresh.isRefreshing = false
                }
                updateUi()
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.likedProducts
            .onEach {
                productLikedAdapter.submitList(it)
                binding.swipeRefresh.isRefreshing = false
            }
            .launchWhenStarted(lifecycleScope)
    }

    private fun updateUi() {
        val cartListVis = productCartAdapter.currentList.isNotEmpty()
        val cartVis = cartDao.getAll().isNotEmpty()
        val likeVis = favDao.getAllButCart().isNotEmpty()
        binding.apply {
            emptyState.isVisible = !cartVis && !cartListVis
            cartContainer.isVisible = cartVis && cartListVis
            cartTotalContainer.isVisible = cartVis && cartListVis
            likedBlock.isVisible = likeVis && cartVis
            cartBottomContainer.isVisible = cartVis && cartListVis
        }
    }

    private fun performCartItemActionsMenuClick(
        id: Int, colorId: Int, sizeId: Int, view: View
    ) {
        val inCart = checkInFavorites(id)
        val path = view.findViewById<ImageView>(R.id.cartProductActions)
        val popupMenu = PopupMenu(view.context, path, Gravity.END)
        popupMenu.inflate(R.menu.cart_item_actions_menu)
        popupMenu.menu.findItem(R.id.cartToFavorite).isVisible = !inCart
        popupMenu.menu.findItem(R.id.cartRemoveFavorite).isVisible = inCart
        popupMenu.setForceShowIcon(true)
        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.cartToFavorite -> {
                        editFavorites(id)
                        return true
                    }

                    R.id.cartRemoveFavorite -> {
                        editFavorites(id)
                        return true
                    }

                    R.id.cartRemove -> {
                        val cartItem = CartItem(-1, id, colorId, sizeId, 1)
                        when (App.AUTH) {
                            true -> viewModel.updateItemInRemoteCart(cartItem)
                            false -> editItemInCart(cartItem)
                        }
                        return true
                    }
                }
                return false
            }
        })
        popupMenu.show()
    }

    private fun updateAdaptersFromOther() {
        val lst1 = productCartAdapter.currentList
        productCartAdapter.submitList(lst1.filter { checkInCart(it) })

        val lst2 = productLikedAdapter.currentList
        productLikedAdapter.submitList(lst2.filter { checkInFavorites(it.id) && !checkInCart(it.id) })

        updateUi()
        viewModel.getLikedNoAuth()
        viewModel.getCartNoAuth()
    }

    private fun openAddToCartDialog(item: ProductMainPreview) {
        val bundle = toCartDialogData(item)
        bundle?.apply {
            val navController = findNavController(requireView())
            val graph = navController.graph
            val walletGraph = graph.findNode(R.id.nav_graph_catalog) as NavGraph
            walletGraph.setStartDestination(R.id.catalogAddToCartDialog)
            navController.navigate(R.id.action_cartFragment_to_nav_graph_catalog, bundle)
        }
    }

    private fun toProductScreen(productId: Int, colorId: Int? = null, sizeId: Int? = null) {
        val navController = findNavController(requireView())
        val graph = navController.graph
        val walletGraph = graph.findNode(R.id.nav_graph_catalog) as NavGraph
        walletGraph.setStartDestination(R.id.catalogProductFragment)
        navController.navigate(
            R.id.action_cartFragment_to_nav_graph_catalog,
            bundleOf(
                "product_id" to productId,
                "color_id" to colorId,
                "size_id" to sizeId
            )
        )
    }

    private fun editFavorites(productId: Int) {
        when (App.AUTH) {
            true -> editItemInFavorites(productId)
            false -> findNavController(requireActivity(), R.id.rootActivityContainer)
                .navigate(R.id.action_navBottomFragment_to_noAuthFirstFragment, emptyAuthBundle)
        }
    }

    private fun goToCheckOut() {
        when (App.AUTH) {
            true -> findNavController().navigate(R.id.action_cartFragment_to_cartOrderingFragment)
            false -> findNavController(requireActivity(), R.id.rootActivityContainer)
                .navigate(
                    R.id.action_navBottomFragment_to_noAuthFirstFragment,
                    bundleOf("from_cart" to true)
                )
        }
    }
}