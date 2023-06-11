package ru.feip.elisianix.catalog

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.ProductCategoryBlockMainListAdapter
import ru.feip.elisianix.adapters.ProductColorListAdapter
import ru.feip.elisianix.adapters.ProductImageListAdapter
import ru.feip.elisianix.adapters.ProductSizeListAdapter
import ru.feip.elisianix.catalog.view_models.CatalogProductViewModel
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.common.db.CartItem
import ru.feip.elisianix.databinding.FragmentCatalogProductBinding
import ru.feip.elisianix.extensions.addStrikethrough
import ru.feip.elisianix.extensions.inCurrency
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.remote.models.ProductDetail
import kotlin.properties.Delegates

class CatalogProductFragment :
    BaseFragment<FragmentCatalogProductBinding>(R.layout.fragment_catalog_product) {

    private val viewModel by lazy {
        ViewModelProvider(this)[CatalogProductViewModel::class.java]
    }

    private lateinit var productImageAdapter: ProductImageListAdapter
    private lateinit var productColorAdapter: ProductColorListAdapter
    private lateinit var productSizeAdapter: ProductSizeListAdapter
    private lateinit var productRecsAdapter: ProductCategoryBlockMainListAdapter
    private var productId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = requireArguments().getInt("product_id")
        viewModel.getProductDetail(productId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            tableOfSizesBtn.paintFlags = Paint.UNDERLINE_TEXT_FLAG

            productImageAdapter = ProductImageListAdapter {
                Navigation.findNavController(requireActivity(), R.id.rootActivityContainer)
                    .navigate(
                        R.id.action_navBottomFragment_to_catalogProductImageViewerFragment,
                        bundleOf("product_id" to productId)
                    )
            }
            recyclerProductImage.adapter = productImageAdapter
            recyclerProductImage.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            PagerSnapHelper().attachToRecyclerView(recyclerProductImage)

            productColorAdapter = ProductColorListAdapter {
                currentProduct = currentProduct.copy(colorId = it.id)
                productColorCurrent.text = it.name
            }
            recyclerColorSelector.adapter = productColorAdapter
            recyclerColorSelector.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            productSizeAdapter = ProductSizeListAdapter {
                currentProduct = currentProduct.copy(sizeId = it.id)
            }
            recyclerSizeSelector.adapter = productSizeAdapter
            recyclerSizeSelector.layoutManager = GridLayoutManager(requireContext(), 4)

            productRecsAdapter = ProductCategoryBlockMainListAdapter(
                {
                    findNavController().navigate(
                        R.id.action_catalogProductFragment_self,
                        bundleOf("product_id" to it.id)
                    )
                }, {}, {}
            )
            recyclerProductRecsBlock.adapter = productRecsAdapter
            recyclerProductRecsBlock.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            recyclerProductRecsIndicator.attachToRecyclerView(recyclerProductRecsBlock)

            swipeRefresh.setOnRefreshListener { viewModel.getProductDetail(productId) }
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.product
            .onEach { prod ->
                viewModel.getProductRecs(prod.category.id)
                updateProductUi(prod)
                currentProduct = CartItem(
                    0, prod.id, prod.colors[0].id, prod.sizes[0].id, 1
                )
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.productRecs
            .onEach {
                productRecsAdapter.submitList(it)
                binding.apply {
                    swipeRefresh.isRefreshing = false
                    productRecsBlock.isVisible = true
                }
            }
            .launchWhenStarted(lifecycleScope)
    }

    private fun updateProductUi(prod: ProductDetail) {
        binding.apply {
            productTagCategory.text = prod.category.name
            productTagCategory.setOnClickListener { }
            productTagBrand.text = prod.brand.name

            productName.text = prod.name

            productPriceNew.inCurrency(prod.price)
            productPriceOld.inCurrency(prod.price)
            productPriceOld.addStrikethrough()

            productColorCurrent.text = prod.colors[0].name

            val features = prod.features.joinToString(separator = "\n") { "● " + it.value }
            productDescription.text = features

            productSizeAdapter.submitList(prod.sizes.filter { it.available > 0 })
            productImageAdapter.submitList(prod.images)
            productColorAdapter.submitList(prod.colors)

            productAllContainer.isVisible = true
            productCartBtnContainer.isVisible = true
            toolbar.menu.findItem(R.id.productToolbarToFavorites).isVisible = true
            productIsNew.isVisible = prod.isNew

            if (prod.images.count() > 1) {
                recyclerProductImageIndicator.attachToRecyclerView(recyclerProductImage)
            }

            productTagCategory.setOnClickListener {
                findNavController().navigate(
                    R.id.action_catalogProductFragment_to_catalogCategoryFragment,
                    bundleOf(
                        "category_id" to prod.category.id.toString(),
                        "section_name" to prod.category.name
                    )
                )
            }
            productTagBrand.setOnClickListener {
                findNavController().navigate(
                    R.id.action_catalogProductFragment_to_catalogCategoryFragment,
                    bundleOf(
                        "brand_id" to prod.brand.id.toString(),
                        "section_name" to prod.brand.name
                    )
                )
            }
        }
    }

    private fun checkCountProductInCart() {
        val currentCount = App.INSTANCE.db.CartDao()
            .checkInCart(
                currentProduct.productId,
                currentProduct.colorId,
                currentProduct.sizeId
            )
        productInCart = currentCount > 0
    }

    private var productInCart: Boolean by Delegates.observable(false) { _, _, inCart ->
        val blackColor = resources.getColor(R.color.black, context?.theme)
        val whiteColor = resources.getColor(R.color.white, context?.theme)
        binding.apply {
            if (inCart) {
                productCartBtn.setOnClickListener(deleteFromCartBtnClickListener)
                productCartBtn.text = getString(R.string.in_the_cart)
                productCartBtn.setTextColor(blackColor)
                productCartBtn.setBackgroundColor(whiteColor)
            } else {
                productCartBtn.setOnClickListener(insertToCartBtnClickListener)
                productCartBtn.text = getString(R.string.to_cart)
                productCartBtn.setTextColor(whiteColor)
                productCartBtn.setBackgroundColor(blackColor)
            }
        }
    }

    //productId, productColor, productSize, cnt
    private var currentProduct: CartItem by Delegates.observable(
        CartItem(0, 0, 0, 0, 0)
    ) { _, _, _ ->
        checkCountProductInCart()
    }

    private val insertToCartBtnClickListener = View.OnClickListener {
        App.INSTANCE.db.CartDao().insert(currentProduct)
        checkCountProductInCart()
    }
    private val deleteFromCartBtnClickListener = View.OnClickListener {
        App.INSTANCE.db.CartDao().deleteByInfo(
            currentProduct.productId,
            currentProduct.colorId,
            currentProduct.sizeId
        )
        checkCountProductInCart()
    }
}