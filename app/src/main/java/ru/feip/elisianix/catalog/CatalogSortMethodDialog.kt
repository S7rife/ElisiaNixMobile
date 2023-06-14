package ru.feip.elisianix.catalog

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.SortMethodListAdapter
import ru.feip.elisianix.common.BaseBottomDialog
import ru.feip.elisianix.databinding.DialogSortMethodBinding
import ru.feip.elisianix.remote.models.sortMethods

class CatalogSortMethodDialog :
    BaseBottomDialog<DialogSortMethodBinding>(R.layout.dialog_sort_method) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.apply {
            dialogCloseBtn.setOnClickListener { findNavController().popBackStack() }
            dialogLabel.text = getString(R.string.sorting)

            val currentSortMethod = sortMethods.first {
                requireArguments().getInt("sort_method") == it.value.first
            }

            val dialogSortMethodAdapter = SortMethodListAdapter {
                setFragmentResult(
                    "resultSortMethodDialog",
                    bundleOf("sort_method" to it.value.first)
                )
                findNavController().popBackStack()
            }
            recyclerSortMethod.adapter = dialogSortMethodAdapter
            recyclerSortMethod.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )
            dialogSortMethodAdapter.currentPos = currentSortMethod.value.first
            dialogSortMethodAdapter.submitList(sortMethods)
        }
    }
}