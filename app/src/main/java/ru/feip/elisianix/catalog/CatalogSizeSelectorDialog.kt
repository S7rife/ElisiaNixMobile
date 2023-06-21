package ru.feip.elisianix.catalog

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.SizeSelectorListAdapter
import ru.feip.elisianix.catalog.view_models.CatalogSizeSelectorViewModel
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.BaseBottomDialog
import ru.feip.elisianix.common.db.CartItem
import ru.feip.elisianix.common.db.checkInCartByInfo
import ru.feip.elisianix.common.db.editItemInCart
import ru.feip.elisianix.databinding.DialogSizeSelectorBinding
import ru.feip.elisianix.extensions.disableAnimation
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.extensions.withColors
import ru.feip.elisianix.remote.models.SizeMap
import ru.feip.elisianix.remote.models.allSizes
import kotlin.properties.Delegates

class CatalogSizeSelectorDialog :
    BaseBottomDialog<DialogSizeSelectorBinding>(R.layout.dialog_size_selector) {

    private val viewModel by lazy {
        ViewModelProvider(this)[CatalogSizeSelectorViewModel::class.java]
    }

    private lateinit var dialogSizeSelectorAdapter: SizeSelectorListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            dialogCloseBtn.setOnClickListener { findNavController().popBackStack() }
            dialogLabel.text = getString(R.string.select_size)
            footer.signUpFitting.paintFlags = Paint.UNDERLINE_TEXT_FLAG

            val sizeIds = requireArguments().getStringArrayList("size_ids")?.map { it.toInt() }
            val availableSizes = requireArguments().getStringArrayList("available_sizes")
            val productId = requireArguments().getInt("product_id")
            val colorId = requireArguments().getInt("color_id")

            dialogSizeSelectorAdapter = SizeSelectorListAdapter {
                if (sizeIds != null) {
                    currentProduct = currentProduct.copy(sizeId = sizeIds[it])
                }
            }

            recyclerSizeSelector.disableAnimation()
            recyclerSizeSelector.adapter = dialogSizeSelectorAdapter
            recyclerSizeSelector.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )

            availableSizes?.let {
                dialogSizeSelectorAdapter.availableSizes =
                    availableSizes.map { SizeMap.valueOf(it) }
            }
            dialogSizeSelectorAdapter.submitList(allSizes)

            currentProduct = currentProduct.copy(productId = productId, colorId = colorId)

            cartBtn.setOnClickListener {
                if (currentProduct.sizeId != -1) {
                    when (App.AUTH) {
                        true -> viewModel.updateItemInRemoteCart(currentProduct)
                        false -> {
                            editItemInCart(currentProduct)
                            updateOrBack()
                        }
                    }
                }
            }
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.productUpdatedInRemote
            .onEach { updateOrBack() }
            .launchWhenStarted(lifecycleScope)
    }

    private var productInCart: Boolean by Delegates.observable(false) { _, _, inCart ->
        binding.cartBtn.withColors(inCart)
        binding.cartBtn.text = when (inCart) {
            true -> getString(R.string.in_the_cart)
            false -> getString(R.string.to_cart)
        }
    }

    private var currentProduct: CartItem by Delegates.observable(
        CartItem(0, 0, 0, -1, 1)
    ) { _, _, newProduct ->
        productInCart = checkInCartByInfo(newProduct)
    }

    private fun updateOrBack() {
        currentProduct = currentProduct
        if (productInCart) {
            setFragmentResult(
                "resultSizeSelectorDialog",
                bundleOf(
                    "selected_size_id" to currentProduct.sizeId,
                    "size_position" to dialogSizeSelectorAdapter.currentPos
                )
            )
            findNavController().popBackStack()
        }
    }
}