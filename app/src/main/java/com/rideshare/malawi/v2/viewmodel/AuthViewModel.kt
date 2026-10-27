package com.rideshare.malawi.v2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.rideshare.malawi.v2.data.AppGraph
import com.rideshare.malawi.v2.data.RideRepository
import com.rideshare.malawi.v2.data.User
import com.rideshare.malawi.v2.data.UserRole
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(
    private val repo: RideRepository
) : ViewModel() {

    constructor() : this(AppGraph.repository)

    val currentUser: StateFlow<User?> = repo.currentUser

    var error by mutableStateOf<String?>(null)
        private set

    fun clearError() {
        error = null
    }

    fun login(phone: String, password: String, onSuccess: (User) -> Unit) {
        val result = repo.login(phone, password)
        result.fold(
            onSuccess = { user ->
                error = null
                onSuccess(user)
            },
            onFailure = { throwable ->
                error = throwable.message
            }
        )
    }

    fun register(name: String, phone: String, password: String, role: UserRole, onSuccess: (User) -> Unit) {
        val result = repo.register(name, phone, password, role)
        result.fold(
            onSuccess = { user ->
                error = null
                onSuccess(user)
            },
            onFailure = { throwable ->
                error = throwable.message
            }
        )
    }

    fun logout() {
        repo.logout()
        error = null
    }

    fun submitVerification(model: String, reg: String, uri: String?, onSuccess: () -> Unit = {}) {
        val result = repo.submitVerification(model, reg, uri)
        result.fold(
            onSuccess = {
                error = null
                onSuccess()
            },
            onFailure = { throwable ->
                error = throwable.message
            }
        )
    }

    fun simulateApproval(onSuccess: () -> Unit = {}) {
        val result = repo.simulateAdminApproval()
        result.fold(
            onSuccess = {
                error = null
                onSuccess()
            },
            onFailure = { throwable ->
                error = throwable.message
            }
        )
    }
}
