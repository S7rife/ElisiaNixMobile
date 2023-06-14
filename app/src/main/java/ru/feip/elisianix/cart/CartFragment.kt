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
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.ProductCartListAdapter
import ru.feip.elisianix.cart.view_models.CartViewModel
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.common.db.checkInCart
import ru.feip.elisianix.common.db.checkInFavorites
import ru.feip.elisianix.common.db.editItemInFavorites
import ru.feip.elisianix.databinding.FragmentCartBinding
import ru.feip.elisianix.extensions.inCurrency
import ru.feip.elisianix.extensions.inStockUnits
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.remote.models.Cart
import ru.feip.elisianix.remote.models.CartItemRemote
import ru.feip.elisianix.remote.models.RequestProductCart

class CartFragment : BaseFragment<FragmentCartBinding>(R.layout.fragment_cart) {

    private val viewModel by lazy {
        ViewModelProvider(this)[CartViewModel::class.java]
    }

    private lateinit var productCartAdapter: ProductCartListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.INSTANCE.db.CartDao().getAllLive().observe(viewLifecycleOwner) { list ->
            viewModel.getCartNoAuth(list.map {
                RequestProductCart(it.productId, it.sizeId, it.colorId, it.count)
            })
            binding.emptyState.isVisible = list.isEmpty()
        }

        binding.apply {
            toolbarCart.title = getString(R.string.cart).uppercase()
            toBuyBtn.text = getString(R.string.check_out)

            productCartAdapter = ProductCartListAdapter(
                {
                    val navController = findNavController(view)
                    val graph = navController.graph
                    val walletGraph = graph.findNode(R.id.nav_graph_catalog) as NavGraph
                    walletGraph.setStartDestination(R.id.catalogProductFragment)
                    navController.navigate(
                        R.id.action_cartFragment_to_nav_graph_catalog,
                        bundleOf(
                            "product_id" to it.productId,
                            "color_id" to it.productColor.id,
                            "size_id" to it.productSize.id
                        )
                    )
                },
                object : ProductCartListAdapter.OptionsMenuClickListener {
                    override fun onOptionsMenuClicked(
                        cartItem: CartItemRemote,
                        position: Int,
                        view: View
                    ) {
                        performCartItemActionsMenuClick(cartItem, position, view)
                    }
                },
            )
            recyclerCartProducts.adapter = productCartAdapter
            recyclerCartProducts.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.cart
            .onEach { updateCart(it) }
            .launchWhenStarted(lifecycleScope)
    }

    private fun updateCart(cart: Cart) {
        val newCart = cart.items.filter { checkInCart(it) }
        productCartAdapter.submitList(newCart)
        val draw = newCart.isNotEmpty()
        hideCart(draw, !draw)
        binding.apply {
            if (draw) {
                cartTotalCountValue.inStockUnits(cart.itemsCount)
                cartTotalPayableValue.inCurrency(cart.finalPrice)
                cartDiscountSumValue.inCurrency(cart.discountPrice)
                cartTotalSumValue.inCurrency(cart.totalPrice)
            }
        }
    }

    private fun performCartItemActionsMenuClick(
        cIt: CartItemRemote,
        pos: Int,
        view: View
    ) {
        val inCart = checkInFavorites(cIt.productId)
        val path = view.findViewById<ImageView>(R.id.cartProductActions)
        val popupMenu = PopupMenu(view.context, path, Gravity.END)
        popupMenu.inflate(R.menu.cart_item_actions_menu)
        popupMenu.menu.findItem(R.id.cartToFavorite).isVisible = !inCart
        popupMenu.menu.findItem(R.id.cartRemoveFavorite).isVisible = inCart
        popupMenu.setForceShowIcon(true)
        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                val id = cIt.productId
                when (item.itemId) {
                    R.id.cartToFavorite -> {
                        editItemInFavorites(id)
                        productCartAdapter.currentList[pos].inFavorites = checkInFavorites(id)
                        productCartAdapter.notifyItemChanged(pos)
                        return true
                    }

                    R.id.cartRemoveFavorite -> {
                        editItemInFavorites(id)
                        productCartAdapter.currentList[pos].inFavorites = checkInFavorites(id)
                        productCartAdapter.notifyItemChanged(pos)
                        return true
                    }

                    R.id.cartRemove -> {
                        deleteFromCart(cIt.productId, cIt.productColor.id, cIt.productSize.id, pos)
                        return true
                    }
                }
                return false
            }
        })
        popupMenu.show()
    }

    private fun deleteFromCart(id: Int, colorId: Int, sizeId: Int, pos: Int) {
        val removed = App.INSTANCE.db.CartDao().deleteByInfo(id, colorId, sizeId)
        if (removed > 0) {
            val newLst = productCartAdapter.currentList.filterIndexed { idx, _ -> idx != pos }
            productCartAdapter.submitList(newLst)
            hideCart(newLst.isNotEmpty(), newLst.isEmpty())
        }
    }

    private fun hideCart(visMain: Boolean, visEmpty: Boolean = false) {
        binding.apply {
            cartContainer.isVisible = visMain
            emptyState.isVisible = visEmpty
            toBuyBtnContainer.isVisible = visMain
        }
    }
}