package com.rideshare.malawi.v2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.rideshare.malawi.v2.data.AppGraph
import com.rideshare.malawi.v2.data.Ride
import com.rideshare.malawi.v2.data.RideRepository
import com.rideshare.malawi.v2.data.RideRequest
import com.rideshare.malawi.v2.data.User
import kotlinx.coroutines.flow.StateFlow

class RidesViewModel(
    private val repo: RideRepository
) : ViewModel() {

    constructor() : this(AppGraph.repository)

    val rides: StateFlow<List<Ride>> = repo.rides
    val requests: StateFlow<List<RideRequest>> = repo.requests
    val users: StateFlow<List<User>> = repo.users

    var notice by mutableStateOf<String?>(null)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun clearNotice() {
        notice = null
    }

    fun clearError() {
        error = null
    }

    fun createRide(
        pickup: String,
        destination: String,
        date: String,
        time: String,
        seats: Int,
        pricePerSeat: Int,
        onSuccess: () -> Unit
    ) {
        val result = repo.createRide(pickup, destination, date, time, seats, pricePerSeat)
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

    fun requestSeat(rideId: String, driverName: String, onSuccess: () -> Unit = {}) {
        val result = repo.requestSeat(rideId)
        result.fold(
            onSuccess = {
                error = null
                notice = "Request sent to $driverName. Track it under Bookings."
                onSuccess()
            },
            onFailure = { throwable ->
                error = throwable.message
            }
        )
    }

    fun respond(requestId: String, accept: Boolean, onSuccess: () -> Unit = {}) {
        val result = repo.respondToRequest(requestId, accept)
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
