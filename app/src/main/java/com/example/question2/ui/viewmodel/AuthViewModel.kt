package com.example.question2.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.question2.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail

    private val _authMessage = MutableLiveData<String?>()
    val authMessage: LiveData<String?> = _authMessage

    init {
        checkUserStatus()
    }

    fun checkUserStatus() {
        _currentUser.value = authRepository.currentUser
        _userEmail.value = authRepository.userEmail
    }

    fun handleGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            val result = authRepository.firebaseAuthWithGoogle(idToken)
            if (result.isSuccess) {
                _currentUser.value = authRepository.currentUser
                _userEmail.value = authRepository.userEmail
                _authMessage.value = "Sign in successful!"
            } else {
                _authMessage.value = "Sign in failed: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        checkUserStatus()
        _authMessage.value = "Signed out."
    }

    fun clearAuthMessage() {
        _authMessage.value = null
    }
}