package ru.feip.elisianix.cart

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.onEach
import ru.feip.elisianix.R
import ru.feip.elisianix.adapters.ProductCartListAdapter
import ru.feip.elisianix.cart.view_models.CartOrderingViewModel
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.BaseFragment
import ru.feip.elisianix.common.db.CartItem
import ru.feip.elisianix.common.db.checkInCart
import ru.feip.elisianix.common.db.checkInFavorites
import ru.feip.elisianix.common.db.editItemInCart
import ru.feip.elisianix.common.db.editItemInFavorites
import ru.feip.elisianix.databinding.FragmentCartOrderingBinding
import ru.feip.elisianix.extensions.disableAnimation
import ru.feip.elisianix.extensions.inCurrency
import ru.feip.elisianix.extensions.inStockUnits
import ru.feip.elisianix.extensions.launchWhenStarted
import ru.feip.elisianix.extensions.setUnderline
import ru.feip.elisianix.remote.models.PickupPoint
import ru.feip.elisianix.remote.models.emptyAuthBundle
import ru.feip.elisianix.remote.models.getEmptyError
import ru.feip.elisianix.remote.models.parseDays
import ru.tinkoff.decoro.FormattedTextChangeListener
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.slots.PredefinedSlots
import ru.tinkoff.decoro.watchers.FormatWatcher
import ru.tinkoff.decoro.watchers.MaskFormatWatcher
import kotlin.properties.Delegates

class CartOrderingFragment :
    BaseFragment<FragmentCartOrderingBinding>(R.layout.fragment_cart_ordering) {

    private val viewModel by lazy {
        ViewModelProvider(this)[CartOrderingViewModel::class.java]
    }

    private lateinit var productCartAdapter: ProductCartListAdapter
    private val cartDao = App.INSTANCE.db.CartDao()
    private val favDao = App.INSTANCE.db.FavoritesDao()

    private var currentDeliveryType = "Self"
    private var currentPhoneNumber = ""
    private lateinit var pickupPoints: List<PickupPoint>
    private var currentPlace: PickupPoint? by Delegates.observable(initialValue = null) { _, _, _ ->
        updatePlaceUI()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController(requireActivity(), R.id.rootActivityContainer)
            .currentBackStackEntry?.savedStateHandle?.getLiveData<Int>("map_choose_result")
            ?.observe(viewLifecycleOwner) { pointId ->
                currentPlace = pickupPoints.first { it.id == pointId }
            }

        cartDao.getAllLive().observe(viewLifecycleOwner) { updateAdaptersFromOther() }
        favDao.getAllLive().observe(viewLifecycleOwner) { updateAdaptersFromOther() }

        binding.apply {
            toolbarCart.setNavigationOnClickListener { findNavController().popBackStack() }
            radioGroup.setOnCheckedChangeListener { _, itemId ->
                val isDelivery = itemId == R.id.radioDelivery
                deliveryContainer.isVisible = isDelivery
                pickupContainer.isVisible = !isDelivery
                pickupMapBtn.isVisible = !isDelivery

                when (isDelivery) {
                    true -> {
                        deliveryInfoText.text = paymentEmulateDelivery
                        currentDeliveryType = "Delivery"
                    }

                    false -> {
                        deliveryInfoText.text = paymentEmulateShowroom
                        currentDeliveryType = "Self"
                    }
                }
            }
            radioGroup.check(R.id.radioPickUp)

            pickupMapBtn.setUnderline()
            pickupMapBtn.setOnClickListener { toMap() }
            checkoutBtn.setOnClickListener { toOrder() }
            swipeRefresh.setOnRefreshListener { updateAdaptersFromOther() }

            productCartAdapter = ProductCartListAdapter(
                {
                    toProductScreen(it.productId, it.productColor.id, it.productSize.id)
                },
                object : ProductCartListAdapter.OptionsMenuClickListener {
                    override fun onOptionsMenuClicked(
                        id: Int,
                        colorId: Int,
                        sizeId: Int,
                        view: View
                    ) {
                        performCartItemActionsMenuClick(id, colorId, sizeId, view)
                    }
                },
            )
            recyclerCartProducts.disableAnimation()
            recyclerCartProducts.adapter = productCartAdapter
            recyclerCartProducts.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )

            val mask = MaskImpl(PredefinedSlots.RUS_PHONE_NUMBER, true)
            mask.isForbidInputWhenFilled = true
            val formatWatcher = MaskFormatWatcher(mask)
            formatWatcher.installOn(phoneInputEdit)
            formatWatcher.setCallback(PhoneNumberChangeListener())

            updateUi()
            viewModel.getPickupPoints()
        }

        viewModel.showLoading
            .onEach { binding.loader.isVisible = it }
            .launchWhenStarted(lifecycleScope)

        viewModel.orderId
            .onEach { goToOrdered(it) }
            .launchWhenStarted(lifecycleScope)

        viewModel.pickupPoints
            .onEach { lst ->
                pickupPoints = lst.map { requireContext().parseDays(it) }
                if (currentPlace == null) currentPlace = pickupPoints[0]
            }
            .launchWhenStarted(lifecycleScope)

        viewModel.cart
            .onEach { cart ->
                productCartAdapter.submitList(cart.items.filter { checkInCart(it) })
                binding.apply {
                    cartTotalCountValue.inStockUnits(cart.itemsCount)
                    cartTotalPayableValue.inCurrency(cart.finalPrice)
                    cartDiscountSumValue.inCurrency(cart.discountPrice)
                    cartTotalSumValue.inCurrency(cart.totalPrice)
                    swipeRefresh.isRefreshing = false
                }
                updateUi()
            }
            .launchWhenStarted(lifecycleScope)

        setUpTextFields()
    }

    private fun updateUi() {
        val cartListVis = productCartAdapter.currentList.isNotEmpty()
        val cartVis = cartDao.getAll().isNotEmpty()
        binding.apply {
            cartContainer.isVisible = cartVis && cartListVis
            cartTotalContainer.isVisible = cartVis && cartListVis
            cartBottomContainer.isVisible = cartVis && cartListVis
        }
    }

    private fun performCartItemActionsMenuClick(
        id: Int, colorId: Int, sizeId: Int, view: View
    ) {
        val inCart = checkInFavorites(id)
        val path = view.findViewById<ImageView>(R.id.cartProductActions)
        val popupMenu = PopupMenu(view.context, path, Gravity.END)
        popupMenu.inflate(R.menu.cart_item_actions_menu)
        popupMenu.menu.findItem(R.id.cartToFavorite).isVisible = !inCart
        popupMenu.menu.findItem(R.id.cartRemoveFavorite).isVisible = inCart
        popupMenu.setForceShowIcon(true)
        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.cartToFavorite -> {
                        editFavorites(id)
                        return true
                    }

                    R.id.cartRemoveFavorite -> {
                        editFavorites(id)
                        return true
                    }

                    R.id.cartRemove -> {
                        editItemInCart(CartItem(-1, id, colorId, sizeId, 1))
                        return true
                    }
                }
                return false
            }
        })
        popupMenu.show()
    }

    private fun updateAdaptersFromOther() {
        if (cartDao.checkCnt() < 1) {
            findNavController().popBackStack()
        }
        val lst = productCartAdapter.currentList
        productCartAdapter.submitList(lst.filter { checkInCart(it) })
        updateUi()
        viewModel.getCartNoAuth()
    }

    private val paymentEmulateDelivery =
        "Отправляем заказы курьерской службой по всей России. Согласуем с вами сроки и стоимость, чтобы вы могли выбрать оптимальный способ доставки."
    private val paymentEmulateShowroom = "В шоуруме вы можете расплатиться картой или наличными"

    private fun updatePlaceUI() {
        binding.apply {
            currentPlace?.let {
                pickupAddress.text = it.address
                pickupHours.text = it.dayHoursOneLine
            }
        }
    }

    private fun toProductScreen(productId: Int, colorId: Int? = null, sizeId: Int? = null) {
        val navController = findNavController(requireView())
        val graph = navController.graph
        val walletGraph = graph.findNode(R.id.nav_graph_catalog) as NavGraph
        walletGraph.setStartDestination(R.id.catalogProductFragment)
        navController.navigate(
            R.id.action_cartOrderingFragment_to_nav_graph_catalog,
            bundleOf(
                "product_id" to productId,
                "color_id" to colorId,
                "size_id" to sizeId
            )
        )
    }

    private fun editFavorites(productId: Int) {
        when (App.AUTH) {
            true -> editItemInFavorites(productId)
            false -> findNavController(requireActivity(), R.id.rootActivityContainer)
                .navigate(R.id.action_navBottomFragment_to_noAuthFirstFragment, emptyAuthBundle)
        }
    }

    private fun goToOrdered(orderNumber: Int) {
        findNavController().navigate(
            R.id.action_cartOrderingFragment_to_cartOrderedDialog,
            bundleOf("order_number" to orderNumber.toString())
        )
    }

    private fun toMap() {
        findNavController(requireActivity(), R.id.rootActivityContainer)
            .navigate(R.id.action_navBottomFragment_to_mapFragment)
    }

    private fun toOrder() {
        binding.apply {
            viewModel.toOrder(
                name = nameInputEdit.text.toString(),
                phone = currentPhoneNumber,
                email = emailInputEdit.text.toString(),
                deliveryType = currentDeliveryType,
                pickupPointId = currentPlace?.id,
                comment = commentsInputEdit.text.toString(),
                index = indexInputEdit.text.toString(),
                city = cityInputEdit.text.toString(),
                street = streetInputEdit.text.toString(),
                house = houseInputEdit.text.toString(),
                flat = flatInputEdit.text.toString()
            )
        }
    }

    private fun setUpTextFields() {
        binding.apply {
            viewModel.nameEmpty
                .onEach {
                    nameInputLayout.error = requireContext().getEmptyError(nameInputLayout.hint)
                }
                .launchWhenStarted(lifecycleScope)

            viewModel.phoneEmpty
                .onEach {
                    phoneInputLayout.error = requireContext().getEmptyError(phoneInputLayout.hint)
                }
                .launchWhenStarted(lifecycleScope)

            viewModel.phoneIncorrect
                .onEach {
                    val error = "${phoneInputLayout.hint} ${getString(R.string.incorrect_error)}"
                    phoneInputLayout.error = error
                }
                .launchWhenStarted(lifecycleScope)

            viewModel.emailEmpty
                .onEach {
                    emailInputLayout.error = requireContext().getEmptyError(emailInputLayout.hint)
                }
                .launchWhenStarted(lifecycleScope)

            viewModel.indexEmpty
                .onEach {
                    indexInputLayout.error = requireContext().getEmptyError(indexInputLayout.hint)
                }
                .launchWhenStarted(lifecycleScope)

            viewModel.cityEmpty
                .onEach {
                    cityInputLayout.error = requireContext().getEmptyError(cityInputLayout.hint)
                }
                .launchWhenStarted(lifecycleScope)

            viewModel.streetEmpty
                .onEach {
                    streetInputLayout.error = requireContext().getEmptyError(streetInputLayout.hint)
                }
                .launchWhenStarted(lifecycleScope)

            viewModel.houseEmpty
                .onEach {
                    houseInputLayout.error = requireContext().getEmptyError(houseInputLayout.hint)
                }
                .launchWhenStarted(lifecycleScope)

            nameInputEdit.addTextChangedListener { nameInputLayout.isErrorEnabled = false }
            phoneInputEdit.addTextChangedListener { phoneInputLayout.isErrorEnabled = false }
            emailInputEdit.addTextChangedListener { emailInputLayout.isErrorEnabled = false }
            cityInputEdit.addTextChangedListener { cityInputLayout.isErrorEnabled = false }
            indexInputEdit.addTextChangedListener { indexInputLayout.isErrorEnabled = false }
            streetInputEdit.addTextChangedListener { streetInputLayout.isErrorEnabled = false }
            houseInputEdit.addTextChangedListener { houseInputLayout.isErrorEnabled = false }
            flatInputEdit.addTextChangedListener { flatInputLayout.isErrorEnabled = false }
            commentsInputEdit.addTextChangedListener { commentsInputLayout.isErrorEnabled = false }

            nameInputEdit.setOnFocusChangeListener { _, _ ->
                nameInputLayout.isErrorEnabled = false
            }
            phoneInputEdit.setOnFocusChangeListener { _, _ ->
                phoneInputLayout.isErrorEnabled = false
            }
            emailInputEdit.setOnFocusChangeListener { _, _ ->
                emailInputLayout.isErrorEnabled = false
            }
            cityInputEdit.setOnFocusChangeListener { _, _ ->
                cityInputLayout.isErrorEnabled = false
            }
            indexInputEdit.setOnFocusChangeListener { _, _ ->
                indexInputLayout.isErrorEnabled = false
            }
            streetInputEdit.setOnFocusChangeListener { _, _ ->
                streetInputLayout.isErrorEnabled = false
            }
            houseInputEdit.setOnFocusChangeListener { _, _ ->
                houseInputLayout.isErrorEnabled = false
            }
            flatInputEdit.setOnFocusChangeListener { _, _ ->
                flatInputLayout.isErrorEnabled = false
            }
            commentsInputEdit.setOnFocusChangeListener { _, _ ->
                commentsInputLayout.isErrorEnabled = false
            }
        }
    }

    inner class PhoneNumberChangeListener : FormattedTextChangeListener {
        override fun beforeFormatting(oldValue: String?, newValue: String?): Boolean {
            return oldValue == newValue
        }

        override fun onTextFormatted(formatter: FormatWatcher?, newFormattedText: String?) {
            formatter?.apply {
                currentPhoneNumber = formatter.mask.toUnformattedString()
            }
        }
    }
}