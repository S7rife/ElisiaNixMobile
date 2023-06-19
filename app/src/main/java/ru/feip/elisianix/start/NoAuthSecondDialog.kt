package ru.feip.elisianix.start

import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.BaseBottomDialog
import ru.feip.elisianix.databinding.DialogNoAuthSecondBinding
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.start.view_models.NoAuthSecondViewModel
import ru.tinkoff.decoro.FormattedTextChangeListener
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.FormatWatcher
import ru.tinkoff.decoro.watchers.MaskFormatWatcher
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


class NoAuthSecondDialog :
    BaseBottomDialog<DialogNoAuthSecondBinding>(R.layout.dialog_no_auth_second) {

    private val viewModel by lazy {
        ViewModelProvider(this)[NoAuthSecondViewModel::class.java]
    }

    var currentCode: String? by Delegates.observable("") { _, _, new ->
        val len = new?.length == 4
        binding.apply {
            sendCodeAgainBtn.isVisible = !len
            confirmBtn.isVisible = len
        }
    }

    private var phoneNumber: String? = ""
    private var back: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        phoneNumber = requireArguments().getString("phone_number")

        startTimer.start()
        getCodeEmulate()

        binding.apply {
            authMessageNumber.text = requireArguments().getString("phone_number_formatted")
            dialogCloseBtn.setOnClickListener {
                back = false
                dismiss()
            }
            dialogBackBtn.setOnClickListener {
                back = true
                dismiss()
            }

            sendCodeAgainBtn.setOnClickListener {
                startTimer.start()
                getCodeEmulate()
            }

            confirmBtn.setOnClickListener {
                if (phoneNumber != null && currentCode != null) {
                    viewModel.sendAuthCode(phoneNumber!!, currentCode!!)
                }
            }

            val slots = UnderscoreDigitSlotsParser().parseSlots("____")
            val mask = MaskImpl(slots, true)
            mask.isForbidInputWhenFilled = true
            val formatWatcher = MaskFormatWatcher(mask)
            formatWatcher.installOn(authCodeEdit)
            formatWatcher.setCallback(CodeChangeListener())
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.success
            .onEach {
                if (!it) {
                    val text = getString(R.string.code_is_wrong)
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(requireContext(), text, duration)
                    toast.setGravity(Gravity.BOTTOM, 0, 0)
                    toast.show()
                }
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.userInfo
            .onEach {
                App.sharedPreferences.edit().putString("token", it.token).apply()
                App.INSTANCE.db.UserInfoDao().insert(it)
                App.AUTH = true
                back = false
                dismiss()
            }
            .launchWhenStarted(lifecycleScope)
    }

    private val startTimer = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val f: NumberFormat = DecimalFormat("00")
            val min = millisUntilFinished / 60000 % 60
            val sec = millisUntilFinished / 1000 % 60
            val btnText = getString(R.string.send_again) + " (${f.format(min)}:${f.format(sec)})"
            binding.sendCodeAgainBtn.text = btnText
            binding.sendCodeAgainBtn.isEnabled = false
        }

        override fun onFinish() {
            binding.apply {
                sendCodeAgainBtn.text = getString(R.string.send_again)
                sendCodeAgainBtn.isEnabled = true
            }
        }
    }

    inner class CodeChangeListener : FormattedTextChangeListener {
        override fun beforeFormatting(oldValue: String?, newValue: String?): Boolean {
            return oldValue == newValue
        }

        override fun onTextFormatted(formatter: FormatWatcher?, newFormattedText: String?) {
            formatter?.apply {
                val str = formatter.mask.toUnformattedString().filter { it.isDigit() }
                currentCode = str
            }
        }
    }

    private fun getCodeEmulate() {
        val text = getString(R.string.code_emulate)
        val duration = Toast.LENGTH_LONG
        val toast = Toast.makeText(requireContext(), text, duration)
        toast.setGravity(Gravity.BOTTOM, 0, 0)

        val executor = Executors.newSingleThreadScheduledExecutor()
        executor.schedule({
            toast.show()
            executor.shutdown()
        }, 3, TimeUnit.SECONDS)
    }

    override fun onDismiss(dialog: DialogInterface) {
        startTimer.cancel()
        super.onDismiss(dialog)
        setFragmentResult("dialog_result", bundleOf("back" to back))
    }
}