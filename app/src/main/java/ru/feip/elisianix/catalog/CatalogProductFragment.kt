package ru.feip.elisianix.catalog

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.ProductCategoryBlockMainListAdapter
import ru.feip.elisianix.adapters.ProductColorListAdapter
import ru.feip.elisianix.adapters.ProductImageListAdapter
import ru.feip.elisianix.adapters.ProductSizeListAdapter
import ru.feip.elisianix.catalog.view_models.CatalogProductViewModel
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.databinding.FragmentCatalogProductBinding
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.remote.models.ProductColor

class CatalogProductFragment :
    BaseFragment<FragmentCatalogProductBinding>(R.layout.fragment_catalog_product) {

    private val viewModel by lazy {
        ViewModelProvider(this)[CatalogProductViewModel::class.java]
    }

    private lateinit var productImageAdapter: ProductImageListAdapter
    private lateinit var productColorAdapter: ProductColorListAdapter
    private lateinit var productSizeAdapter: ProductSizeListAdapter
    private lateinit var productRecsAdapter: ProductCategoryBlockMainListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productId = requireArguments().getInt("product_id")

        binding.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            tableOfSizesBtn.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            productImageAdapter = ProductImageListAdapter()
            recyclerProductImage.adapter = productImageAdapter
            recyclerProductImage.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            productColorAdapter = ProductColorListAdapter {
                productColorCurrent.text = it.name
            }
            recyclerColorSelector.adapter = productColorAdapter
            recyclerColorSelector.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            productSizeAdapter = ProductSizeListAdapter({})
            recyclerSizeSelector.adapter = productSizeAdapter
            recyclerSizeSelector.layoutManager = GridLayoutManager(requireContext(), 4)

            productRecsAdapter = ProductCategoryBlockMainListAdapter {
                findNavController().navigate(
                    R.id.action_catalogProductFragment_self,
                    bundleOf("product_id" to it.id)
                )
            }
            recyclerProductRecsBlock.adapter = productRecsAdapter
            recyclerProductRecsBlock.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.product
            .onEach {
                binding.apply {
                    viewModel.getProductRecs(it.category.id)

                    productImageAdapter.submitList(it.images)

                    productTagCategory.text = it.category.name
                    productTagCategory.setOnClickListener {  }
                    productTagBrand.text = it.brand.name

                    productName.text = it.name

                    val newPrice = String.format("%.3f", it.price) + "₽"
                    val oldPrice = String.format("%.3f", it.price * 2) + "₽"
                    productPriceNew.text = newPrice
                    productPriceOld.text = oldPrice

                    productColorCurrent.text = it.color.name
                    productDescription.text = it.description

                    productSizeAdapter.submitList(it.sizes.filter { it.available > 0 })

                    productColorAdapter.submitList(
                        // Hardcode
                        listOf(
                            it.color,
                            ProductColor(1, "color1", "#D2691E"),
                            ProductColor(2, "color2", "#808080"),
                            ProductColor(3, "color3", "#FF00FF")
                        )
                    )
                }
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.productRecs
            .onEach {
                productRecsAdapter.submitList(it)
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.getProductDetail(productId)
    }
}