package ru.feip.elisianix.cart

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import ru.feip.elisianix.R
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.BaseBottomDialog
import ru.feip.elisianix.databinding.DialogCartOrderedBinding


class CartOrderedDialog :
    BaseBottomDialog<DialogCartOrderedBinding>(R.layout.dialog_cart_ordered) {

    private var orderNumber: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderNumber = requireArguments().getInt("order_number")

        binding.apply {
            dialogCloseBtn.setOnClickListener { dismiss() }
            initMessage()
            vkBtn.isChecked = true
            youtubeBtn.isChecked = true
            telegramBtn.isChecked = true
        }
        App.INSTANCE.db.CartDao().deleteAll()
    }

    private fun initMessage() {
        binding.apply {
            val message =
                "${getString(R.string.thanks_order)}$orderNumber ${getString(R.string.has_been_completed)}"
            orderedMessage.text = message
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        findNavController().popBackStack(R.id.cartOrderingFragment, true)
        super.onDismiss(dialog)
    }
}