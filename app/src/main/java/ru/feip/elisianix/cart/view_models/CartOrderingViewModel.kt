package ru.feip.elisianix.cart.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.db.CartItem
import ru.feip.elisianix.common.db.checkInCartByInfo
import ru.feip.elisianix.common.db.deleteItemInCart
import ru.feip.elisianix.remote.ApiService
import ru.feip.elisianix.remote.Result
import ru.feip.elisianix.remote.models.Address
import ru.feip.elisianix.remote.models.Cart
import ru.feip.elisianix.remote.models.CartItemRemote
import ru.feip.elisianix.remote.models.PickupPoint
import ru.feip.elisianix.remote.models.RequestOrder
import ru.feip.elisianix.remote.models.RequestProductCart
import ru.feip.elisianix.remote.models.RequestProductCartUpdate
import ru.feip.elisianix.remote.models.pickupPointParse

class CartOrderingViewModel : ViewModel() {
    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _cart = MutableSharedFlow<Cart>(replay = 1)
    private val _pickupPoints = MutableSharedFlow<List<PickupPoint>>(replay = 1)
    private val _orderId = MutableSharedFlow<Int>(replay = 0)
    private val _productUpdatedInRemote = MutableSharedFlow<Boolean>(replay = 0)

    val showLoading get() = _showLoading
    val pickupPoints get() = _pickupPoints
    val orderId get() = _orderId
    val cart get() = _cart
    val productUpdatedInRemote get() = _productUpdatedInRemote

    fun getCartNoAuth() {
        viewModelScope.launch {
            val localCartProducts = App.INSTANCE.db.CartDao().getAll().map {
                RequestProductCart(it.productId, it.sizeId, it.colorId, it.count)
            }
            apiService.getCartNoAuth(localCartProducts)
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect { cartRemote ->
                    when (cartRemote) {
                        is Result.Success -> {
                            cartRemote.result.items?.let {
                                cartRemote.result.items = sortItems(cartRemote.result.items!!)
                                    .filter {
                                        checkInCartByInfo(
                                            CartItem(
                                                0, it.productId, it.productColor.id,
                                                it.productSize.id, 0
                                            )
                                        )
                                    }
                            }
                            _cart.emit(cartRemote.result)
                        }

                        is Result.Error -> {}
                    }
                }
        }
    }

    private fun sortItems(items: List<CartItemRemote>): List<CartItemRemote> {
        return items.sortedWith(
            compareByDescending<CartItemRemote> { it.name }
                .thenByDescending { it.productId }
                .thenByDescending { it.productColor.id }
                .thenByDescending { it.productSize.id })
    }

    fun getPickupPoints() {
        viewModelScope.launch {
            apiService.getPickupPoints()
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect { res ->
                    when (res) {
                        is Result.Success -> {
                            _pickupPoints.emit(res.result.map { pickupPointParse(it) })
                        }

                        is Result.Error -> {}
                    }
                }
        }
    }

    private val _nameEmpty = MutableSharedFlow<Boolean>(replay = 0)
    private val _phoneEmpty = MutableSharedFlow<Boolean>(replay = 0)
    private val _phoneIncorrect = MutableSharedFlow<Boolean>(replay = 0)
    private val _emailEmpty = MutableSharedFlow<Boolean>(replay = 0)
    private val _indexEmpty = MutableSharedFlow<Boolean>(replay = 0)
    private val _cityEmpty = MutableSharedFlow<Boolean>(replay = 0)
    private val _streetEmpty = MutableSharedFlow<Boolean>(replay = 0)
    private val _houseEmpty = MutableSharedFlow<Boolean>(replay = 0)

    val nameEmpty get() = _nameEmpty
    val phoneEmpty get() = _phoneEmpty
    val phoneIncorrect get() = _phoneIncorrect
    val emailEmpty get() = _emailEmpty
    val indexEmpty get() = _indexEmpty
    val cityEmpty get() = _cityEmpty
    val streetEmpty get() = _streetEmpty
    val houseEmpty get() = _houseEmpty

    fun toOrder(
        name: String, phone: String, email: String, deliveryType: String,
        pickupPointId: Int?, comment: String?, index: String, city: String,
        street: String, house: String, flat: String?
    ) {
        val order = RequestOrder(
            name = name, phone = phone, email = email,
            deliveryType = deliveryType, pickupPointId = pickupPointId,
            address = Address(index, city, street, house, flat), comment = comment
        )
        viewModelScope.launch {
            if (name.isBlank()) _nameEmpty.emit(false).also { return@launch }
            if (phone.isBlank()) _phoneEmpty.emit(false).also { return@launch }
            if (phone.length != 12) _phoneIncorrect.emit(false).also { return@launch }
            if (email.isBlank()) _emailEmpty.emit(false).also { return@launch }

            when (deliveryType) {
                "Delivery" -> {
                    if (city.isBlank()) _cityEmpty.emit(false).also { return@launch }
                    if (index.isBlank()) _indexEmpty.emit(false).also { return@launch }
                    if (street.isBlank()) _streetEmpty.emit(false).also { return@launch }
                    if (house.isBlank()) _houseEmpty.emit(false).also { return@launch }

                    order.pickupPointId = null
                    apiService.toOrder(order)
                        .onStart { _showLoading.value = true }
                        .onCompletion { _showLoading.value = false }
                        .collect {
                            when (it) {
                                is Result.Success -> _orderId.emit(it.result.id)
                                is Result.Error -> {}
                            }
                        }
                }

                "Self" -> {
                    order.address = null
                    apiService.toOrder(order)
                        .onStart { _showLoading.value = true }
                        .onCompletion { _showLoading.value = false }
                        .collect {
                            when (it) {
                                is Result.Success -> _orderId.emit(it.result.id)
                                is Result.Error -> {}
                            }
                        }
                }
            }
        }
    }

    fun removeFromRemoteCart(item: CartItem) {
        val remove = RequestProductCartUpdate(item.productId, item.sizeId, item.colorId, 0)
        viewModelScope.launch {
            if (checkInCartByInfo(item)) {
                apiService.updateInRemoteCart(remove)
                    .onStart { _showLoading.value = true }
                    .onCompletion { _showLoading.value = false }
                    .collect {
                        when (it) {
                            is Result.Success -> {
                                deleteItemInCart(item)
                                _productUpdatedInRemote.emit(true)
                            }

                            is Result.Error -> {}
                        }
                    }
            }
        }
    }
}