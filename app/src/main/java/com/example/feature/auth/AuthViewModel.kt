package com.example.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.AuthRepository
import com.example.core.data.GoogleSignInResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isSigningIn: Boolean = false,
    val isCheckingSession: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var autoSignInAttempted = false

    fun attemptAutoSignInOnce(context: Context) {
        if (autoSignInAttempted || authRepository.currentUser() != null) return
        autoSignInAttempted = true
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingSession = true) }
            authRepository.attemptAutoSignIn(context)
            _uiState.update { it.copy(isCheckingSession = false) }
        }
    }

    fun onGoogleSignInClick(context: Context) {
        if (_uiState.value.isSigningIn) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSigningIn = true, errorMessage = null, infoMessage = null)
            }
            when (val result = authRepository.signInWithGoogle(context)) {
                is GoogleSignInResult.Success -> {
                    _uiState.update { it.copy(isSigningIn = false) }
                }
                is GoogleSignInResult.Cancelled -> {
                    _uiState.update {
                        it.copy(
                            isSigningIn = false,
                            infoMessage = "Sign-in cancelled. Tap below whenever you're ready."
                        )
                    }
                }
                is GoogleSignInResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSigningIn = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            authRepository.signOut(context)
            _uiState.value = AuthUiState()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }
}
