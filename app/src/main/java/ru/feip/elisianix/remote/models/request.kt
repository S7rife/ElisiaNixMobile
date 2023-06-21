package ru.feip.elisianix.remote.models

import com.google.gson.annotations.SerializedName

data class RequestProductCart(
    @SerializedName("productId")
    var productId: Int,

    @SerializedName("sizeId")
    var sizeId: Int,

    @SerializedName("colorId")
    var colorId: Int,

    @SerializedName("count")
    var count: Int
)

data class RequestProductCartUpdate(
    @SerializedName("productId")
    var productId: Int,

    @SerializedName("sizeId")
    var sizeId: Int,

    @SerializedName("colorId")
    var colorId: Int,

    @SerializedName("newCount")
    var newCount: Int
)

data class RequestCartItems(
    @SerializedName("items")
    var items: List<RequestProductCart>
)

data class RequestAuthSendPhoneNumber(
    @SerializedName("phoneNumber")
    var phoneNumber: String
)

data class RequestAuthSendCode(
    @SerializedName("phone")
    var phone: String,

    @SerializedName("code")
    var code: String
)

data class RequestOrder(
    @SerializedName("name")
    var name: String,

    @SerializedName("phone")
    var phone: String,

    @SerializedName("email")
    var email: String,

    @SerializedName("deliveryType")
    var deliveryType: String,

    @SerializedName("pickupPointId")
    var pickupPointId: Int?,

    @SerializedName("address")
    var address: Address?,

    @SerializedName("comment")
    var comment: String?,
)