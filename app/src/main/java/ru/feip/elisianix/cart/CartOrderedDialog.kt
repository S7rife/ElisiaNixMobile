package ru.feip.elisianix.cart

import android.os.Bundle
import android.view.View
import ru.feip.elisianix.R
import ru.feip.elisianix.common.BaseBottomDialog
import ru.feip.elisianix.databinding.DialogCartOrderedBinding


class CartOrderedDialog :
    BaseBottomDialog<DialogCartOrderedBinding>(R.layout.dialog_cart_ordered) {

    private var orderNumber: String? = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderNumber = requireArguments().getString("order_number")

        binding.apply {
            dialogCloseBtn.setOnClickListener { dismiss() }
            initMessage()
            vkBtn.isChecked = true
            youtubeBtn.isChecked = true
            telegramBtn.isChecked = true
        }
    }

    private fun initMessage() {
        binding.apply {
            val message =
                "${getString(R.string.thanks_order)} $orderNumber ${getString(R.string.has_been_completed)}"
            orderedMessage.text = message
        }
    }
}