package ru.feip.elisianix.start.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.feip.elisianix.common.db.UserInfo
import ru.feip.elisianix.remote.ApiService
import ru.feip.elisianix.remote.Result

class NoAuthSecondViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableSharedFlow<Boolean>(replay = 0)
    private val _userInfo = MutableSharedFlow<UserInfo>(replay = 0)

    val showLoading get() = _showLoading
    val userInfo get() = _userInfo
    val success get() = _success

    fun sendAuthCode(phoneNumber: String, code: String) {
        viewModelScope.launch {
            apiService.sendAuthCode(phoneNumber, code)
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect {
                    when (it) {
                        is Result.Success -> _userInfo.emit(it.result)
                        is Result.Error -> _success.emit(false)
                    }
                }
        }
    }
}