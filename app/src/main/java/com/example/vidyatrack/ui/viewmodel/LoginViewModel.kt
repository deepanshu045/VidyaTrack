package com.example.vidyatrack.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyatrack.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _loginError = mutableStateOf<String?>(null)
    val loginError: State<String?> = _loginError

    private val _loginSuccess = mutableStateOf<String?>(null)
    val loginSuccess: State<String?> = _loginSuccess

    fun onUsernameChange(value: String) {
        _username.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun login() {
        if (_username.value.isBlank() || _password.value.isBlank()) {
            _loginError.value = "Please enter both username and password"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            
            val credentials = mapOf(
                "username" to _username.value,
                "password" to _password.value
            )
            
            val result = authRepository.login(credentials)
            _isLoading.value = false
            
            result.onSuccess { response ->
                _loginSuccess.value = response.role
            }.onFailure { error ->
                _loginError.value = error.message ?: "Login failed"
            }
        }
    }
}
