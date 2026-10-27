package com.rideshare.malawi.v2.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DRIVER_RIDES = "driver_rides"
    const val DRIVER_REQUESTS = "driver_requests"
    const val PASSENGER_SEARCH = "passenger_search"
    const val PASSENGER_BOOKINGS = "passenger_bookings"
    const val PROFILE = "profile"
    const val DRIVER_VERIFICATION = "driver_verification"
    const val CREATE_RIDE = "create_ride"
    const val RIDE_DETAILS = "ride_details/{rideId}"
    const val BOOKING_DETAILS = "booking_details/{requestId}"

    fun rideDetails(rideId: String) = "ride_details/$rideId"
    fun bookingDetails(requestId: String) = "booking_details/$requestId"
}
