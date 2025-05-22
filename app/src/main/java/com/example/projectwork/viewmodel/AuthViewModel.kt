package com.example.projectwork.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectwork.data.AppDatabase
import com.example.projectwork.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val username: String = ""
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = UserRepository(database.userDao())
    }
    
    fun login(username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val user = repository.login(username, password)
                
                if (user != null) {
                    _authState.update { 
                        it.copy(
                            isLoading = false, 
                            isLoggedIn = true, 
                            username = user.username
                        ) 
                    }
                    onSuccess()
                } else {
                    _authState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = "Invalid username or password"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _authState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = e.message ?: "Login failed"
                    ) 
                }
            }
        }
    }
    
    fun register(username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val userId = repository.register(username, password)
                
                if (userId != null) {
                    _authState.update { 
                        it.copy(
                            isLoading = false, 
                            isLoggedIn = true, 
                            username = username
                        ) 
                    }
                    onSuccess()
                } else {
                    _authState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = "Username already exists"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _authState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = e.message ?: "Registration failed"
                    ) 
                }
            }
        }
    }
    
    fun logout() {
        _authState.update { 
            AuthState() 
        }
    }
} 