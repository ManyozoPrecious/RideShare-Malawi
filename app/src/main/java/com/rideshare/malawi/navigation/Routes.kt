package com.rideshare.malawi.navigation

object Routes {
    const val LOGIN = "login"
    const val ROLE_SELECT = "role_select"
    const val DRIVER_VERIFICATION = "driver_verification"
    const val DRIVER_HOME = "driver_home"
    const val CREATE_RIDE = "create_ride"
    const val RIDE_REQUESTS = "ride_requests"
    const val PASSENGER_HOME = "passenger_home"
    const val RIDE_DETAILS = "ride_details/{rideId}"
    const val REQUEST_PAYMENT = "request_payment/{requestId}"

    fun rideDetails(rideId: String) = "ride_details/$rideId"
    fun requestPayment(requestId: String) = "request_payment/$requestId"
}
