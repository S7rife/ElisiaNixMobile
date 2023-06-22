package ru.feip.elisianix.start

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.databinding.FragmentSplashBinding
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.start.view_models.SplashViewModel


class SplashFragment : BaseFragment<FragmentSplashBinding>(R.layout.fragment_splash) {

    private val viewModel by lazy {
        ViewModelProvider(this)[SplashViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController =
            Navigation.findNavController(requireActivity(), R.id.rootActivityContainer)
        val mainGraph = navController.navInflater.inflate(R.navigation.nav_graph_start)

        mainGraph.setStartDestination(R.id.navBottomFragment)

        when (App.sharedPreferences.contains("token")) {
            true -> viewModel.updateCartFromRemote()
            false -> navController.graph = mainGraph
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.cartUpdated
            .onEach { navController.graph = mainGraph }
            .launchWhenStarted(lifecycleScope)
    }
}