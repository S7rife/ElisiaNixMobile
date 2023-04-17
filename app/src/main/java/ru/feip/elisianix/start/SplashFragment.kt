package ru.feip.elisianix.start

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.FragmentSplashBinding


class SplashFragment : BaseFragment<FragmentSplashBinding>(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController =
            Navigation.findNavController(requireActivity(), R.id.rootActivityContainer)
        val mainGraph = navController.navInflater.inflate(R.navigation.nav_graph_start)

        mainGraph.setStartDestination(R.id.navBottomFragment)
        navController.graph = mainGraph
    }
}