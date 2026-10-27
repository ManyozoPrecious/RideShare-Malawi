package com.rideshare.malawi.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

object AppState {

    var currentRole = mutableStateOf<UserRole?>(null)
    var currentUserName = mutableStateOf("")
    var driverVerification = mutableStateOf(VerificationStatus.PENDING)

    private val sampleDrivers = listOf(
        Driver("d1", "Chisomo Banda", "+265 991 23 45 67", "BT 4521", VerificationStatus.VERIFIED, 4.8f),
        Driver("d2", "Thandiwe Phiri", "+265 888 76 54 32", "LL 7709", VerificationStatus.VERIFIED, 4.6f),
        Driver("d3", "Mphatso Zulu", "+265 999 11 22 33", "MZ 3390", VerificationStatus.VERIFIED, 4.9f)
    )

    private fun sampleRides() = listOf(
        Ride("r1", sampleDrivers[0], "Lilongwe", "Blantyre", "Fri, 10 Oct", "06:30", 3, 18000),
        Ride("r2", sampleDrivers[1], "Lilongwe", "Mzuzu", "Fri, 10 Oct", "07:00", 2, 15000),
        Ride("r3", sampleDrivers[2], "Blantyre", "Zomba", "Sat, 11 Oct", "09:15", 4, 6000),
        Ride("r4", sampleDrivers[0], "Lilongwe", "Zomba", "Sat, 11 Oct", "13:00", 1, 12000),
        Ride("r5", sampleDrivers[1], "Mzuzu", "Lilongwe", "Sun, 12 Oct", "05:45", 3, 15000)
    )

    val rides = mutableStateListOf(*sampleRides().toTypedArray())

    val myCreatedRides = mutableStateListOf<Ride>()
    val incomingRequests = mutableStateListOf<RideRequest>()
    val myRequests = mutableStateListOf<RideRequest>()

    fun resetForTesting() {
        currentRole.value = null
        currentUserName.value = ""
        driverVerification.value = VerificationStatus.PENDING
        rides.clear(); rides.addAll(sampleRides())
        myCreatedRides.clear()
        incomingRequests.clear()
        myRequests.clear()
    }

    fun addRide(ride: Ride) {
        rides.add(0, ride)
        myCreatedRides.add(0, ride)
    }

    fun requestSeat(ride: Ride, passengerName: String): RideRequest {
        val request = RideRequest(
            id = "req-${System.currentTimeMillis()}",
            ride = ride,
            passengerName = passengerName,
            status = RequestStatus.PENDING
        )
        incomingRequests.add(0, request)
        myRequests.add(0, request)
        return request
    }

    private fun replace(request: RideRequest, updated: RideRequest) {
        val i1 = incomingRequests.indexOfFirst { it.id == request.id }
        if (i1 != -1) incomingRequests[i1] = updated
        val i2 = myRequests.indexOfFirst { it.id == request.id }
        if (i2 != -1) myRequests[i2] = updated
    }

    fun updateRequestStatus(request: RideRequest, status: RequestStatus) {
        replace(request, request.copy(status = status))
    }

    fun updatePayment(request: RideRequest, status: PaymentStatus) {
        replace(request, request.copy(payment = status))
    }
}
