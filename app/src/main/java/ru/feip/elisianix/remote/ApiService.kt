package ru.feip.elisianix.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.feip.elisianix.remote.models.*

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
}