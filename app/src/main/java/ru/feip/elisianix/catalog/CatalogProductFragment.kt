package ru.feip.elisianix.catalog

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
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
import ru.feip.elisianix.common.db.checkInCart
import ru.feip.elisianix.common.db.checkInFavorites
import ru.feip.elisianix.common.db.editItemInCart
import ru.feip.elisianix.common.db.editItemInFavorites
import ru.feip.elisianix.databinding.FragmentCatalogProductBinding
import ru.feip.elisianix.extensions.addStrikethrough
import ru.feip.elisianix.extensions.disableAnimation
import ru.feip.elisianix.extensions.inCurrency
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.extensions.withColors
import ru.feip.elisianix.remote.models.ProductDetail
import ru.feip.elisianix.remote.models.ProductMainPreview
import ru.feip.elisianix.remote.models.emptyAuthBundle
import ru.feip.elisianix.remote.models.toCartDialogData
import kotlin.properties.Delegates


class CatalogProductFragment :
    BaseFragment<FragmentCatalogProductBinding>(R.layout.fragment_catalog_product) {

    private val viewModel by lazy {
        ViewModelProvider(this)[CatalogProductViewModel::class.java]
    }

    private val cartDao = App.INSTANCE.db.CartDao()
    private val favDao = App.INSTANCE.db.FavoritesDao()

    private lateinit var productImageAdapter: ProductImageListAdapter
    private lateinit var productColorAdapter: ProductColorListAdapter
    private lateinit var productSizeAdapter: ProductSizeListAdapter
    private lateinit var productRecsAdapter: ProductCategoryBlockMainListAdapter
    private var productId = 0
    private var colorId: Int? = null
    private var sizeId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = requireArguments().getInt("product_id")
        colorId = requireArguments().getInt("color_id")
        sizeId = requireArguments().getInt("size_id")
        viewModel.getProductDetail(productId)

        setFragmentResultListener("resultSizeSelectorDialog") { _, bundle ->
            val selectedSizeId = bundle.getInt("selected_size_id")
            val sizePosition = bundle.getInt("size_position")
            productSizeAdapter.currentPos = sizePosition
            productSizeAdapter.notifyItemChanged(sizePosition)
            currentProduct = currentProduct.copy(sizeId = selectedSizeId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentProduct = currentProduct.copy(
            productId = productId,
            colorId = colorId ?: 0,
            sizeId = sizeId ?: -1
        )

        val toFavoriteBtn = binding.toolbar.menu.findItem(R.id.productNotInFavorites)
        val removeFavoriteBtn = binding.toolbar.menu.findItem(R.id.productInFavorites)

        cartDao.checkCntLive().observe(viewLifecycleOwner) {
            updateAdapterFromOther()
            productInCart = checkInCart(currentProduct)
        }
        favDao.checkCntLive().observe(viewLifecycleOwner) {
            updateAdapterFromOther()
            val inFav = checkInFavorites(productId)
            toFavoriteBtn.isVisible = !inFav.also { toFavoriteBtn.isEnabled = !inFav }
            removeFavoriteBtn.isVisible = inFav.also { removeFavoriteBtn.isEnabled = inFav }
        }

        binding.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.setOnMenuItemClickListener {
                editFavorites(productId)
                true
            }

            tableOfSizesBtn.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            tableOfSizesBtn.setOnClickListener {
                findNavController().navigate(
                    R.id.action_catalogProductFragment_to_catalogTableOfSizesDialog
                )
            }

            productImageAdapter = ProductImageListAdapter {
                findNavController(requireActivity(), R.id.rootActivityContainer)
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
            recyclerColorSelector.disableAnimation()
            recyclerColorSelector.adapter = productColorAdapter
            recyclerColorSelector.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            productSizeAdapter = ProductSizeListAdapter {
                currentProduct = if (productSizeAdapter.currentPos == -1) {
                    currentProduct.copy(sizeId = -1)
                } else {
                    currentProduct.copy(sizeId = it.id)
                }
                productInCart = checkInCart(currentProduct)
            }
            recyclerSizeSelector.disableAnimation()
            recyclerSizeSelector.adapter = productSizeAdapter
            recyclerSizeSelector.layoutManager = GridLayoutManager(requireContext(), 4)

            productRecsAdapter = ProductCategoryBlockMainListAdapter(
                {
                    findNavController().navigate(
                        R.id.action_catalogProductFragment_self,
                        bundleOf("product_id" to it.id)
                    )
                },
                {
                    openAddToCartDialog(it)
                },
                {
                    editFavorites(it.id)
                }
            )
            recyclerProductRecsBlock.disableAnimation()
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

        viewModel.productUpdatedInRemote
            .onEach { editItemInCart(it) }
            .launchWhenStarted(lifecycleScope)

        viewModel.product
            .onEach { prod ->
                viewModel.getProductRecs(prod.category.id)
                updateProductUi(prod)
                productInCart = checkInCart(currentProduct)
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

            val features = prod.features.joinToString(separator = "\n") { "● " + it.value }
            productDescription.text = features

            val colorPos =
                prod.colors.indexOfFirst { it.id == colorId }.takeUnless { it == -1 } ?: 0
            val color = prod.colors[colorPos]
            productColorCurrent.text = color.name
            productColorAdapter.currentPos = colorPos
            productColorAdapter.submitList(prod.colors)

            val sizePos = prod.sizes.indexOfFirst { it.id == sizeId }.takeUnless { it == -1 }
            val size = sizePos?.let { prod.sizes[sizePos] }
            productSizeAdapter.currentPos = sizePos ?: -1
            productSizeAdapter.submitList(prod.sizes)

            productImageAdapter.submitList(prod.images)

            productAllContainer.isVisible = true
            productCartBtnContainer.isVisible = true
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
            productCartBtn.setOnClickListener {
                if (currentProduct.sizeId != -1) {
                    when (App.AUTH) {
                        true -> viewModel.updateItemInRemoteCart(currentProduct)
                        false -> editItemInCart(currentProduct)
                    }
                } else {
                    val availableSizes = prod.sizes.filter { it.available > 0 }.map { it.value }
                    val sizeIds = prod.sizes.map { it.id.toString() }
                    findNavController().navigate(
                        R.id.action_catalogProductFragment_to_catalogSizeSelectorDialog,
                        bundleOf(
                            "available_sizes" to availableSizes,
                            "product_id" to currentProduct.productId,
                            "color_id" to currentProduct.colorId,
                            "size_ids" to sizeIds
                        )
                    )
                }
            }
            currentProduct = currentProduct.copy(colorId = color.id, sizeId = size?.id ?: -1)
        }
    }

    private var productInCart: Boolean by Delegates.observable(false) { _, _, inCart ->
        binding.productCartBtn.withColors(inCart)
        binding.productCartBtn.text = when (inCart) {
            true -> getString(R.string.in_the_cart)
            false -> getString(R.string.to_cart)
        }
    }

    private var currentProduct: CartItem by Delegates.observable(
        CartItem(0, 0, 0, -1, 1)
    ) { _, _, newProduct ->
        productInCart = checkInCart(newProduct)
    }

    private fun openAddToCartDialog(item: ProductMainPreview) {
        val bundle = toCartDialogData(item)
        bundle?.apply {
            findNavController().navigate(
                R.id.action_catalogProductFragment_to_catalogAddToCartDialog, bundle
            )
        }
    }

    private fun updateAdapterFromOther() {
        val lst = productRecsAdapter.currentList
        lst.forEachIndexed { idx, item ->
            val inCart = checkInCart(item.id)
            val inFav = checkInFavorites(item.id)
            if (item.inFavorites != inFav || item.inCart != inCart) {
                item.inCart = inCart
                item.inFavorites = inFav
                productRecsAdapter.notifyItemChanged(idx)
            }
        }
    }

    private fun editFavorites(productId: Int) {
        when (App.AUTH) {
            true -> editItemInFavorites(productId)
            false -> findNavController(requireActivity(), R.id.rootActivityContainer)
                .navigate(R.id.action_navBottomFragment_to_noAuthFirstFragment, emptyAuthBundle)
        }
    }
}