package com.rideshare.malawi.data

enum class UserRole { DRIVER, PASSENGER }

enum class VerificationStatus { PENDING, VERIFIED }

enum class RequestStatus { PENDING, ACCEPTED, REJECTED }

enum class PaymentStatus { UNPAID, PROCESSING, SUCCESS, FAILED }

data class Driver(
    val id: String,
    val name: String,
    val phone: String,
    val vehicleRegNumber: String,
    val verification: VerificationStatus,
    val rating: Float = 4.6f
)

data class Ride(
    val id: String,
    val driver: Driver,
    val pickup: String,
    val destination: String,
    val date: String,
    val time: String,
    val seatsAvailable: Int,
    val pricePerSeat: Int
)

data class RideRequest(
    val id: String,
    val ride: Ride,
    val passengerName: String,
    val status: RequestStatus,
    val payment: PaymentStatus = PaymentStatus.UNPAID
)
