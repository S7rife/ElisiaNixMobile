package ru.feip.elisianix.catalog

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.ColorSelectorGridListAdapter
import ru.feip.elisianix.adapters.SizeSelectorGridListAdapter
import ru.feip.elisianix.common.BaseBottomDialog
import ru.feip.elisianix.common.db.CartItem
import ru.feip.elisianix.common.db.checkInCart
import ru.feip.elisianix.common.db.editItemInCart
import ru.feip.elisianix.databinding.DialogAddToCartBinding
import ru.feip.elisianix.extensions.disableAnimation
import ru.feip.elisianix.extensions.withColors
import ru.feip.elisianix.remote.models.ProductColor
import ru.feip.elisianix.remote.models.SizeMap
import ru.feip.elisianix.remote.models.allSizes
import kotlin.properties.Delegates

class CatalogAddToCartDialog :
    BaseBottomDialog<DialogAddToCartBinding>(R.layout.dialog_add_to_cart) {

    private lateinit var productSizeAdapter: SizeSelectorGridListAdapter
    private lateinit var productColorAdapter: ColorSelectorGridListAdapter
    var productId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = requireArguments().getInt("product_id")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            dialogCloseBtn.setOnClickListener { findNavController().popBackStack() }
            dialogLabel.text = getString(R.string.select_color_and_size)

            val ra = requireArguments()
            currentProduct = currentProduct.copy(productId = productId)

            val sizeIds = ra.getStringArrayList("size_ids")?.map { it.toInt() }
            val availableSizes = ra.getStringArrayList("available_sizes")
            val colorIds = ra.getStringArrayList("color_ids")?.map { it.toInt() }
            val colorNames = ra.getStringArrayList("color_names")
            val colorValues = ra.getStringArrayList("color_values")

            if (sizeIds != null && availableSizes != null) {
                initSizeAdapter(sizeIds, availableSizes)
            }
            if (colorIds != null && colorNames != null && colorValues != null) {
                initColorAdapter(List(colorIds.size) { i ->
                    ProductColor(colorIds[i], colorNames[i], colorValues[i])
                })
            }

            cartBtn.setOnClickListener {
                editItemInCart(currentProduct)
                currentProduct = currentProduct
            }
        }
    }

    private var productInCart: Boolean by Delegates.observable(false) { _, _, inCart ->
        binding.cartBtn.withColors(inCart, currentProduct.sizeId == -1)
        binding.cartBtn.text = when (inCart) {
            true -> getString(R.string.in_the_cart)
            false -> getString(R.string.to_cart)
        }
    }

    private var currentProduct: CartItem by Delegates.observable(
        CartItem(0, 0, 0, -1, 1)
    ) { _, _, newProduct -> productInCart = checkInCart(newProduct) }


    private fun initColorAdapter(lst: List<ProductColor>) {
        productColorAdapter = ColorSelectorGridListAdapter {
            currentProduct = currentProduct.copy(colorId = it.id)
        }

        binding.recyclerColorSelector.disableAnimation()
        binding.recyclerColorSelector.adapter = productColorAdapter
        binding.recyclerColorSelector.layoutManager = GridLayoutManager(requireContext(), 2)
        productColorAdapter.submitList(lst)
        currentProduct = currentProduct.copy(colorId = productColorAdapter.currentList[0].id)
    }

    private fun initSizeAdapter(ids: List<Int>, availableSizes: List<String>) {
        productSizeAdapter = SizeSelectorGridListAdapter { item ->
            val idx = availableSizes.indexOfFirst { it == item.name }
            currentProduct = currentProduct.copy(sizeId = ids[idx])
            if (productSizeAdapter.currentPos == -1) {
                currentProduct = currentProduct.copy(sizeId = -1)
            }
        }
        binding.recyclerSizeSelector.disableAnimation()
        binding.recyclerSizeSelector.adapter = productSizeAdapter
        binding.recyclerSizeSelector.layoutManager = GridLayoutManager(requireContext(), 2)
        productSizeAdapter.availableSizes = availableSizes.map { SizeMap.valueOf(it) }
        productSizeAdapter.submitList(allSizes)
    }
}