package com.rideshare.malawi.v2.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryRideRepository : RideRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _users = MutableStateFlow(initialUsers())
    override val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _rides = MutableStateFlow(initialRides())
    override val rides: StateFlow<List<Ride>> = _rides.asStateFlow()

    private val _requests = MutableStateFlow<List<RideRequest>>(emptyList())
    override val requests: StateFlow<List<RideRequest>> = _requests.asStateFlow()

    override fun register(name: String, phone: String, password: String, role: UserRole): Result<User> {
        if (name.isBlank()) {
            return Result.failure(IllegalStateException("Please enter your name."))
        }
        val normPhone = normalizePhone(phone)
        if (normPhone.length < 9) {
            return Result.failure(IllegalStateException("Enter a valid phone number."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalStateException("Password must be at least 6 characters."))
        }
        if (_users.value.any { it.phone == normPhone }) {
            return Result.failure(IllegalStateException("An account with this phone number already exists."))
        }

        val newUser = User(
            id = "u-${System.currentTimeMillis()}-${_users.value.size}",
            name = name.trim(),
            phone = normPhone,
            password = password,
            role = role
        )
        _users.value = _users.value + newUser
        _currentUser.value = newUser
        return Result.success(newUser)
    }

    override fun login(phone: String, password: String): Result<User> {
        val normPhone = normalizePhone(phone)
        val user = _users.value.find { it.phone == normPhone && it.password == password }
            ?: return Result.failure(IllegalStateException("Incorrect phone number or password."))

        _currentUser.value = user
        return Result.success(user)
    }

    override fun logout() {
        _currentUser.value = null
    }

    override fun submitVerification(vehicleModel: String, vehicleReg: String, documentUri: String?): Result<Unit> {
        val user = _currentUser.value
            ?: return Result.failure(IllegalStateException("Only drivers can submit verification."))
        if (user.role != UserRole.DRIVER) {
            return Result.failure(IllegalStateException("Only drivers can submit verification."))
        }
        if (vehicleModel.trim().isBlank()) {
            return Result.failure(IllegalStateException("Enter your vehicle make and model."))
        }
        if (vehicleReg.trim().isBlank()) {
            return Result.failure(IllegalStateException("Enter your vehicle registration number."))
        }
        if (documentUri.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Attach a photo of your registration document."))
        }

        val updatedUser = user.copy(
            vehicleModel = vehicleModel.trim(),
            vehicleReg = vehicleReg.trim().uppercase(),
            documentUri = documentUri,
            verification = VerificationStatus.PENDING
        )
        _currentUser.value = updatedUser
        _users.value = _users.value.map { if (it.id == user.id) updatedUser else it }
        return Result.success(Unit)
    }

    override fun simulateAdminApproval(): Result<Unit> {
        val user = _currentUser.value
            ?: return Result.failure(IllegalStateException("There is nothing waiting for review."))
        if (user.role != UserRole.DRIVER || user.verification != VerificationStatus.PENDING) {
            return Result.failure(IllegalStateException("There is nothing waiting for review."))
        }

        val updatedUser = user.copy(verification = VerificationStatus.VERIFIED)
        _currentUser.value = updatedUser
        _users.value = _users.value.map { if (it.id == user.id) updatedUser else it }
        return Result.success(Unit)
    }

    override fun createRide(
        pickup: String,
        destination: String,
        date: String,
        time: String,
        seats: Int,
        pricePerSeat: Int
    ): Result<Ride> {
        val user = _currentUser.value
            ?: return Result.failure(IllegalStateException("You need to be verified before you can offer rides."))
        if (user.role != UserRole.DRIVER) {
            return Result.failure(IllegalStateException("You need to be verified before you can offer rides."))
        }
        if (user.verification != VerificationStatus.VERIFIED) {
            return Result.failure(IllegalStateException("You need to be verified before you can offer rides."))
        }
        if (pickup.trim().isBlank() || destination.trim().isBlank()) {
            return Result.failure(IllegalStateException("Enter both a pickup location and a destination."))
        }
        if (date.trim().isBlank() || time.trim().isBlank()) {
            return Result.failure(IllegalStateException("Enter the date and time of the ride."))
        }
        if (seats <= 0) {
            return Result.failure(IllegalStateException("A ride needs at least one seat."))
        }
        if (pricePerSeat <= 0) {
            return Result.failure(IllegalStateException("Enter a price per seat."))
        }

        val newRide = Ride(
            id = "r-${System.currentTimeMillis()}-${_rides.value.size}",
            driverId = user.id,
            pickup = pickup.trim(),
            destination = destination.trim(),
            date = date.trim(),
            time = time.trim(),
            seatsAvailable = seats,
            pricePerSeat = pricePerSeat
        )
        _rides.value = listOf(newRide) + _rides.value
        return Result.success(newRide)
    }

    override fun requestSeat(rideId: String): Result<RideRequest> {
        val user = _currentUser.value
            ?: return Result.failure(IllegalStateException("Log in as a passenger to request a seat."))
        if (user.role != UserRole.PASSENGER) {
            return Result.failure(IllegalStateException("Log in as a passenger to request a seat."))
        }

        val ride = _rides.value.find { it.id == rideId }
            ?: return Result.failure(IllegalStateException("This ride no longer exists."))

        if (ride.seatsAvailable <= 0) {
            return Result.failure(IllegalStateException("This ride is full."))
        }

        val hasExistingRequest = _requests.value.any {
            it.rideId == rideId && it.passengerId == user.id && it.status != RequestStatus.REJECTED
        }
        if (hasExistingRequest) {
            return Result.failure(IllegalStateException("You have already requested a seat on this ride."))
        }

        val newRequest = RideRequest(
            id = "req-${System.currentTimeMillis()}-${_requests.value.size}",
            rideId = rideId,
            passengerId = user.id,
            status = RequestStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )
        _requests.value = listOf(newRequest) + _requests.value
        return Result.success(newRequest)
    }

    override fun respondToRequest(requestId: String, accept: Boolean): Result<Unit> {
        val user = _currentUser.value
            ?: return Result.failure(IllegalStateException("Only drivers can respond to requests."))
        if (user.role != UserRole.DRIVER) {
            return Result.failure(IllegalStateException("Only drivers can respond to requests."))
        }

        val request = _requests.value.find { it.id == requestId }
            ?: return Result.failure(IllegalStateException("Request not found."))

        val ride = _rides.value.find { it.id == request.rideId }
            ?: return Result.failure(IllegalStateException("Request not found."))

        if (ride.driverId != user.id) {
            return Result.failure(IllegalStateException("That request is for someone else's ride."))
        }

        if (request.status != RequestStatus.PENDING) {
            return Result.failure(IllegalStateException("This request has already been answered."))
        }

        if (accept) {
            if (ride.seatsAvailable <= 0) {
                return Result.failure(IllegalStateException("No seats left on this ride."))
            }
            val updatedRide = ride.copy(seatsAvailable = ride.seatsAvailable - 1)
            _rides.value = _rides.value.map { if (it.id == ride.id) updatedRide else it }

            val updatedReq = request.copy(status = RequestStatus.ACCEPTED)
            _requests.value = _requests.value.map { if (it.id == request.id) updatedReq else it }
            return Result.success(Unit)
        } else {
            val updatedReq = request.copy(status = RequestStatus.REJECTED)
            _requests.value = _requests.value.map { if (it.id == request.id) updatedReq else it }
            return Result.success(Unit)
        }
    }

    override fun updatePaymentStatus(requestId: String, status: PaymentStatus): Result<Unit> {
        val request = _requests.value.find { it.id == requestId }
            ?: return Result.failure(IllegalStateException("Booking request not found."))
        val updatedReq = request.copy(paymentStatus = status)
        _requests.value = _requests.value.map { if (it.id == requestId) updatedReq else it }
        return Result.success(Unit)
    }

    companion object {
        fun normalizePhone(phone: String): String {
            var digits = phone.filter { it.isDigit() }
            if (digits.startsWith("265") && digits.length > 9) {
                digits = digits.substring(3)
            }
            if (digits.startsWith("0")) {
                digits = digits.substring(1)
            }
            return digits
        }

        private fun initialUsers() = listOf(
            User("d1", "Chisomo Banda", "991000001", "demo123", UserRole.DRIVER, 4.8f, "Toyota Hiace", "BT 4521", null, VerificationStatus.VERIFIED),
            User("d2", "Thandiwe Phiri", "991000002", "demo123", UserRole.DRIVER, 4.6f, "Toyota Noah", "LL 7709", null, VerificationStatus.VERIFIED),
            User("d3", "Mphatso Zulu", "991000003", "demo123", UserRole.DRIVER, 4.9f, "Nissan Caravan", "MZ 3390", null, VerificationStatus.VERIFIED),
            User("p1", "Grace Mwale", "888000001", "demo123", UserRole.PASSENGER)
        )

        private fun initialRides() = listOf(
            Ride("r1", "d1", "Lilongwe", "Blantyre", "Sat, 24 Oct", "06:30", 3, 18000),
            Ride("r2", "d2", "Lilongwe", "Mzuzu", "Sat, 24 Oct", "07:00", 2, 15000),
            Ride("r3", "d3", "Blantyre", "Zomba", "Sun, 25 Oct", "09:15", 4, 6000),
            Ride("r4", "d1", "Lilongwe", "Zomba", "Sun, 25 Oct", "13:00", 1, 12000),
            Ride("r5", "d2", "Mzuzu", "Lilongwe", "Mon, 26 Oct", "05:45", 3, 15000)
        )
    }
}
