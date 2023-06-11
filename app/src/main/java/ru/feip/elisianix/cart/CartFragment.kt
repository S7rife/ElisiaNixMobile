package ru.feip.elisianix.cart

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.ProductCartListAdapter
import ru.feip.elisianix.cart.view_models.CartViewModel
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.BaseFragment
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
        }

        binding.apply {
            toolbarCart.title = getString(R.string.cart).uppercase()
            toBuyBtn.text = getString(R.string.check_out)

            productCartAdapter = ProductCartListAdapter(
                {
                    //TODO go to product detail screen
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
        productCartAdapter.submitList(cart.items)
        val draw = cart.items.isNotEmpty()
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

    override fun onResume() {
        super.onResume()
        hideCart(productCartAdapter.currentList.isNotEmpty())
    }

    private fun performCartItemActionsMenuClick(
        cIt: CartItemRemote,
        pos: Int,
        view: View
    ) {
        val path = view.findViewById<ImageView>(R.id.cartProductActions)
        val popupMenu = PopupMenu(view.context, path, Gravity.END)
        popupMenu.inflate(R.menu.cart_item_actions_menu)
        popupMenu.setForceShowIcon(true)
        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.cartToFavorite -> {
                        // TODO add to favorites
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