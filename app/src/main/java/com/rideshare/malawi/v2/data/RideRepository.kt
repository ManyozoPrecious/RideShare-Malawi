package com.rideshare.malawi.v2.data

import kotlinx.coroutines.flow.StateFlow

interface RideRepository {
    val currentUser: StateFlow<User?>
    val users: StateFlow<List<User>>
    val rides: StateFlow<List<Ride>>
    val requests: StateFlow<List<RideRequest>>

    fun register(name: String, phone: String, password: String, role: UserRole): Result<User>
    fun login(phone: String, password: String): Result<User>
    fun logout()
    fun submitVerification(vehicleModel: String, vehicleReg: String, documentUri: String?): Result<Unit>
    fun simulateAdminApproval(): Result<Unit>
    fun createRide(pickup: String, destination: String, date: String, time: String, seats: Int, pricePerSeat: Int): Result<Ride>
    fun requestSeat(rideId: String): Result<RideRequest>
    fun respondToRequest(requestId: String, accept: Boolean): Result<Unit>
}
