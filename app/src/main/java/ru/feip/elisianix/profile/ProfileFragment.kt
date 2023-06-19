package ru.feip.elisianix.profile

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import ru.feip.elisianix.R
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.databinding.FragmentProfileBinding
import ru.feip.elisianix.remote.models.emptyAuthBundle

class ProfileFragment : BaseFragment<FragmentProfileBinding>(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!App.AUTH) {
            Navigation.findNavController(requireActivity(), R.id.rootActivityContainer)
                .navigate(R.id.action_navBottomFragment_to_noAuthFirstFragment, emptyAuthBundle)
        }
    }
}