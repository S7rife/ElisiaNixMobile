package ru.feip.elisianix.start

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.common.BaseBottomDialog
import ru.feip.elisianix.databinding.DialogNoAuthFirstBinding
import ru.feip.elisianix.extensions.addRegularPart
import ru.feip.elisianix.extensions.addUnderBoldPart
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.start.view_models.NoAuthFirstViewModel
import ru.tinkoff.decoro.FormattedTextChangeListener
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.slots.PredefinedSlots
import ru.tinkoff.decoro.watchers.FormatWatcher
import ru.tinkoff.decoro.watchers.MaskFormatWatcher
import kotlin.properties.Delegates


class NoAuthFirstDialog :
    BaseBottomDialog<DialogNoAuthFirstBinding>(R.layout.dialog_no_auth_first) {

    private val viewModel by lazy {
        ViewModelProvider(this)[NoAuthFirstViewModel::class.java]
    }

    var currentNumber: Pair<String?, String?> by Delegates.observable(Pair("", "")) { _, _, new ->
        binding.getCodeBtn.isEnabled = new.first?.length == 11
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener("dialog_result") { _, bundle ->
            val back = bundle.getBoolean("back")
            binding.parent.isVisible = back
            if (!back) dismiss()
        }

        val fromCart = requireArguments().getBoolean("from_cart")

        binding.apply {
            dialogCloseBtn.setOnClickListener { dismiss() }
            getCodeBtn.setOnClickListener {
                currentNumber.first?.apply { viewModel.sendPhoneNumber(currentNumber.first!!) }
            }
            if (fromCart) authMessageFromScreen.text = getString(R.string.auth_for_order)
            initFooter()

            val mask = MaskImpl(PredefinedSlots.RUS_PHONE_NUMBER, true)
            mask.isForbidInputWhenFilled = true
            val formatWatcher = MaskFormatWatcher(mask)
            formatWatcher.installOn(authNumberEdit)
            formatWatcher.setCallback(PhoneNumberChangeListener())
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.success
            .onEach {
                if (it) {
                    findNavController().navigate(
                        R.id.action_noAuthFirstDialog_to_noAuthSecondDialog,
                        bundleOf(
                            "phone_number" to currentNumber.first,
                            "phone_number_formatted" to currentNumber.second
                        )
                    )
                    binding.parent.isVisible = false
                }
            }
            .launchWhenStarted(lifecycleScope)
    }

    private fun initFooter() {
        binding.apply {
            authAgreement.text = getString(R.string.you_accept)
            authAgreement.addUnderBoldPart(getString(R.string.the_user_agreement))
            authAgreement.addRegularPart(getString(R.string.and))
            authAgreement.addUnderBoldPart(getString(R.string.privacy_policy))
        }
    }

    inner class PhoneNumberChangeListener : FormattedTextChangeListener {
        override fun beforeFormatting(oldValue: String?, newValue: String?): Boolean {
            return oldValue == newValue
        }

        override fun onTextFormatted(formatter: FormatWatcher?, newFormattedText: String?) {
            formatter?.apply {
                val str = formatter.mask.toUnformattedString().filter { it.isDigit() }
                currentNumber = currentNumber.copy(first = str, second = newFormattedText)
            }
        }
    }
}