package com.rideshare.malawi.v2.data

enum class UserRole {
    DRIVER, PASSENGER
}

enum class VerificationStatus {
    NOT_SUBMITTED, PENDING, VERIFIED
}

enum class RequestStatus {
    PENDING, ACCEPTED, REJECTED
}

enum class PaymentStatus {
    UNPAID, PROCESSING, SUCCESS, FAILED
}

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val password: String,
    val role: UserRole,
    val rating: Float = 4.7f,
    val vehicleModel: String = "",
    val vehicleReg: String = "",
    val documentUri: String? = null,
    val verification: VerificationStatus = VerificationStatus.NOT_SUBMITTED
)

data class Ride(
    val id: String,
    val driverId: String,
    val pickup: String,
    val destination: String,
    val date: String,
    val time: String,
    val seatsAvailable: Int,
    val pricePerSeat: Int
)

data class RideRequest(
    val id: String,
    val rideId: String,
    val passengerId: String,
    val status: RequestStatus,
    val createdAt: Long,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID
)
