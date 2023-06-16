package ru.feip.elisianix.catalog

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.ProductImageViewerListAdapter
import ru.feip.elisianix.catalog.view_models.CatalogProductImageViewerViewModel
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.databinding.FragmentCatalogProductImageViewerBinding
import ru.feip.elisianix.extensions.disableAnimation
import ru.feip.elisianix.extensions.launchWhenStarted

class CatalogProductImageViewerFragment :
    BaseFragment<FragmentCatalogProductImageViewerBinding>(R.layout.fragment_catalog_product_image_viewer) {

    private val viewModel by lazy {
        ViewModelProvider(this)[CatalogProductImageViewerViewModel::class.java]
    }

    private lateinit var productImageAdapter: ProductImageViewerListAdapter
    private var totalCountImages = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productId = requireArguments().getInt("product_id")

        binding.apply {
            imageViewerBackBtn.setOnClickListener {
                findNavController().popBackStack()
            }

            productImageAdapter = ProductImageViewerListAdapter {
                changeImage(it.first.url, it.second)
            }
            recyclerProductImageViewer.disableAnimation()
            recyclerProductImageViewer.adapter = productImageAdapter
            recyclerProductImageViewer.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.images
            .onEach {
                productImageAdapter.submitList(it)
                binding.imageViewerCounter.isVisible = true
                totalCountImages = it.count()
                changeImage(it[0].url, 1)
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.getProductImages(productId)
    }

    private fun changeImage(url: String, pos: Int) {
        val preposition = getString(R.string.preposition_of)
        binding.apply {
            Glide.with(mainImage).load(url).error(R.drawable.ic_no_image).into(mainImage)
            val count = "$pos $preposition $totalCountImages"
            binding.imageViewerCounter.text = count
        }
    }
}