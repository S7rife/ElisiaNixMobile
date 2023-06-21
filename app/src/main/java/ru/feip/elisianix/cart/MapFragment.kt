package ru.feip.elisianix.cart

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.cart.view_models.MapViewModel
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.databinding.FragmentMapBinding
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.remote.models.PickupPoint
import ru.feip.elisianix.remote.models.parseDays
import kotlin.properties.Delegates


class MapFragment : BaseFragment<FragmentMapBinding>(R.layout.fragment_map), OnMapReadyCallback {

    private val viewModel by lazy {
        ViewModelProvider(this)[MapViewModel::class.java]
    }

    private var currentPlace: PickupPoint? by Delegates.observable(initialValue = null) { _, _, _ ->
        updatePlaceLayout()
    }

    private lateinit var pickupPoints: List<PickupPoint>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getPickupPoints()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val supportMapFragment =
            childFragmentManager.findFragmentById(binding.map.id) as SupportMapFragment

        binding.apply {
            toolbarMap.setOnClickListener { findNavController().popBackStack() }

            dialogPlace.dialogCloseBtn.setOnClickListener {
                placeInclude.isVisible = false
            }

            dialogPlace.choosePointBtn.setOnClickListener {
                chooseAndBack()
            }
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.pickupPoints
            .onEach { lst ->
                pickupPoints = lst.map { requireContext().parseDays(it) }
                supportMapFragment.getMapAsync(this)
            }
            .launchWhenStarted(lifecycleScope)
    }

    override fun onMapReady(p0: GoogleMap) {
        p0.uiSettings.isZoomControlsEnabled = true
        p0.uiSettings.isMapToolbarEnabled = false
        val start = pickupPoints[0].cooParse
        val startPoint = LatLng(start.first, start.second)

        pickupPoints.forEach {
            val mark = p0.addMarker(
                MarkerOptions()
                    .position(LatLng(it.cooParse.first, it.cooParse.second))
                    .title(it.address)
            )
            mark?.tag = it
        }
        p0.moveCamera(CameraUpdateFactory.newLatLng(startPoint))
        p0.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 12f))

        p0.setOnMarkerClickListener { marker ->
            binding.placeInclude.isVisible = true
            currentPlace = marker.tag as PickupPoint
            false
        }
    }

    private fun updatePlaceLayout() {
        val par = binding.dialogPlace
        currentPlace?.let {
            par.placeName.text = it.address
            par.placeDaysHours.text = it.dayHoursOneLine
        }
    }

    private fun chooseAndBack() {
        val navController =
            Navigation.findNavController(requireActivity(), R.id.rootActivityContainer)
        navController.previousBackStackEntry?.savedStateHandle?.set(
            "map_choose_result",
            currentPlace?.id
        )
        navController.popBackStack()
    }
}