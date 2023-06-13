package ru.feip.elisianix.common

import android.os.Bundle
import android.view.View
import ru.feip.elisianix.R
import ru.feip.elisianix.databinding.FragmentNavBottomBinding
import ru.feip.elisianix.extensions.setBadge
import ru.feip.elisianix.extensions.setupWithNavController


class NavBottomFragment : BaseFragment<FragmentNavBottomBinding>(R.layout.fragment_nav_bottom) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {

        val navGraphIds = listOf(
            R.navigation.nav_graph_catalog,
            R.navigation.nav_graph_cart,
            R.navigation.nav_graph_favorite,
            R.navigation.nav_graph_profile,
        )
        binding.apply {
            bottomNavView.setupWithNavController(
                navGraphIds = navGraphIds,
                fragmentManager = childFragmentManager,
                containerId = R.id.bottomNavContainer,
                intent = requireActivity().intent
            )

            App.INSTANCE.db.FavoritesDao().checkCntLive().observe(viewLifecycleOwner) {
                bottomNavView.setBadge(R.id.nav_graph_favorite, it)
            }
            App.INSTANCE.db.CartDao().checkCntLive().observe(viewLifecycleOwner) {
                bottomNavView.setBadge(R.id.nav_graph_cart, it)
            }
        }
    }
}