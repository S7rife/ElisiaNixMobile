package ru.feip.elisianix.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.feip.elisianix.common.db.UserInfo
import ru.feip.elisianix.remote.models.Cart
import ru.feip.elisianix.remote.models.CategoryMainPreview
import ru.feip.elisianix.remote.models.OrderIdResponse
import ru.feip.elisianix.remote.models.PickupPoint
import ru.feip.elisianix.remote.models.ProductDetail
import ru.feip.elisianix.remote.models.ProductMainPreviews
import ru.feip.elisianix.remote.models.RemoteCartItemInfo
import ru.feip.elisianix.remote.models.RequestAuthSendCode
import ru.feip.elisianix.remote.models.RequestAuthSendPhoneNumber
import ru.feip.elisianix.remote.models.RequestCartItems
import ru.feip.elisianix.remote.models.RequestOrder
import ru.feip.elisianix.remote.models.RequestProductCart
import ru.feip.elisianix.remote.models.RequestProductCartUpdate

class ApiService {
    private val api = NetworkService().retrofit.create(Api::class.java)


    ////////////////////////////////////////__CATALOG__////////////////////////////////////////////


    suspend fun getCategories(): Flow<Result<List<CategoryMainPreview>>> =
        flow<Result<List<CategoryMainPreview>>> {
            emit(Result.Success(api.getCategories()))
        }
            .catch { emit(Result.Error(it)) }
            .flowOn(Dispatchers.IO)


    suspend fun getProducts(queryMap: Map<String, String>): Flow<Result<ProductMainPreviews>> =
        flow<Result<ProductMainPreviews>> {
            emit(Result.Success(api.getProducts(queryMap)))
        }
            .catch { emit(Result.Error(it)) }
            .flowOn(Dispatchers.IO)


    suspend fun getProductDetail(productId: Int): Flow<Result<ProductDetail>> =
        flow<Result<ProductDetail>> {
            emit(Result.Success(api.getProductDetail(productId)))
        }
            .catch { emit(Result.Error(it)) }
            .flowOn(Dispatchers.IO)


    suspend fun getProductRecs(categoryId: Int): Flow<Result<ProductMainPreviews>> =
        flow<Result<ProductMainPreviews>> {
            emit(Result.Success(api.getProductRecs(categoryId)))
        }
            .catch { emit(Result.Error(it)) }
            .flowOn(Dispatchers.IO)


    ///////////////////////////////////////////__CART__////////////////////////////////////////////


    suspend fun getCartNoAuth(productsInCart: List<RequestProductCart>): Flow<Result<Cart>> =
        flow<Result<Cart>> {
            emit(Result.Success(api.getCartNoAuth(RequestCartItems(productsInCart))))
        }
            .catch { emit(Result.Error(it)) }
            .flowOn(Dispatchers.IO)


    suspend fun getCartFromRemote(): Flow<Result<Cart>> =
        flow<Result<Cart>> {
            emit(Result.Success(api.getCartFromRemote()))
        }
            .catch { emit(Result.Error(it)) }
            .flowOn(Dispatchers.IO)


    suspend fun addToRemoteCart(productsInCart: RequestProductCart): Flow<Result<RemoteCartItemInfo>> =
        flow<Result<RemoteCartItemInfo>> {
            emit(Result.Success(api.addToRemoteCart(productsInCart)))
        }
            .catch { emit(Result.Error(it)) }
            .flowOn(Dispatchers.IO)


    suspend fun updateInRemoteCart(productsInCart: RequestProductCartUpdate): Flow<Result<Cart>> =
        flow<Result<Cart>> {
            emit(Result.Success(api.updateInRemoteCart(productsInCart)))
        }
            .catch { emit(Result.Error(it)) }
            .flowOn(Dispatchers.IO)


    suspend fun toOrder(order: RequestOrder): Flow<Result<OrderIdResponse>> =
        flow<Result<OrderIdResponse>> {
            emit(Result.Success(api.toOrder(order)))
        }
            .catch { emit(Result.Error(it)) }
            .flowOn(Dispatchers.IO)


    ///////////////////////////////////////////__AUTH__////////////////////////////////////////////


    suspend fun sendPhoneNumber(phoneNumber: String): Flow<Result<Unit>> =
        flow<Result<Unit>> {
            emit(Result.Success(api.sendPhoneNumber(RequestAuthSendPhoneNumber(phoneNumber))))
        }
            .catch { emit(Result.Error(it)) }
            .flowOn(Dispatchers.IO)


    suspend fun sendAuthCode(phoneNumber: String, code: String): Flow<Result<UserInfo>> =
        flow<Result<UserInfo>> {
            emit(Result.Success(api.sendAuthCode(RequestAuthSendCode(phoneNumber, code))))
        }
            .catch { emit(Result.Error(it)) }
            .flowOn(Dispatchers.IO)


    ////////////////////////////////////////////__MAP__////////////////////////////////////////////


    suspend fun getPickupPoints(): Flow<Result<List<PickupPoint>>> =
        flow<Result<List<PickupPoint>>> {
            emit(Result.Success(api.getPickupPoints()))
        }
            .catch { emit(Result.Error(it)) }
            .flowOn(Dispatchers.IO)
}