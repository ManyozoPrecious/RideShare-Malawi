package com.rideshare.malawi.v2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rideshare.malawi.v2.data.AppGraph
import com.rideshare.malawi.v2.data.PaymentStatus
import com.rideshare.malawi.v2.data.Ride
import com.rideshare.malawi.v2.data.RideRepository
import com.rideshare.malawi.v2.data.RideRequest
import com.rideshare.malawi.v2.data.User
import com.rideshare.malawi.v2.data.paychangu.CloudPayChanguService
import com.rideshare.malawi.v2.data.paychangu.MobileMoneyOperator
import com.rideshare.malawi.v2.data.paychangu.PayChanguChargeRequest
import com.rideshare.malawi.v2.data.paychangu.PayChanguService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    private val payChanguService = PayChanguService()
    private val cloudPayChanguService = CloudPayChanguService()

    fun processPayChanguPayment(
        requestId: String,
        operator: MobileMoneyOperator,
        phone: String,
        amount: Int,
        userEmail: String = "passenger@rideshare.mw",
        userName: String = "Passenger",
        onResult: (Result<String>) -> Unit = {}
    ) {
        repo.updatePaymentStatus(requestId, PaymentStatus.PROCESSING)
        viewModelScope.launch {
            val cloudRes = cloudPayChanguService.createMobileMoneyPayment(requestId, phone, operator)
            if (cloudRes.isSuccess) {
                repo.updatePaymentStatus(requestId, PaymentStatus.SUCCESS)
                val msg = "PayChangu Cloud Payment prompt sent to $phone (${operator.displayName})"
                notice = msg
                onResult(Result.success(msg))
            } else {
                val txRef = "tx-${System.currentTimeMillis()}-$requestId"
                val req = PayChanguChargeRequest(
                    amount = amount,
                    currency = "MWK",
                    operator = operator,
                    phoneNumber = phone,
                    email = userEmail,
                    txRef = txRef,
                    firstName = userName
                )
                val res = payChanguService.chargeMobileMoney(req)
                res.fold(
                    onSuccess = { response ->
                        repo.updatePaymentStatus(requestId, PaymentStatus.SUCCESS)
                        notice = response.message
                        onResult(Result.success(response.message))
                    },
                    onFailure = { throwable ->
                        repo.updatePaymentStatus(requestId, PaymentStatus.FAILED)
                        error = throwable.message ?: "PayChangu payment failed"
                        onResult(Result.failure(throwable))
                    }
                )
            }
        }
    }
}
