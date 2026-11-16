package com.rideshare.malawi.v2.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FirestoreRideRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : RideRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    override val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _rides = MutableStateFlow<List<Ride>>(emptyList())
    override val rides: StateFlow<List<Ride>> = _rides.asStateFlow()

    private val _requests = MutableStateFlow<List<RideRequest>>(emptyList())
    override val requests: StateFlow<List<RideRequest>> = _requests.asStateFlow()

    private var usersListener: ListenerRegistration? = null
    private var ridesListener: ListenerRegistration? = null
    private var requestsListener: ListenerRegistration? = null

    init {
        listenToCollections()
    }

    private fun listenToCollections() {
        usersListener = firestore.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            if (snapshot.isEmpty) {
                seedInitialUsers()
            } else {
                val userList = snapshot.documents.mapNotNull { doc ->
                    try {
                        User(
                            id = doc.getString("id") ?: doc.id,
                            name = doc.getString("name") ?: "",
                            phone = doc.getString("phone") ?: "",
                            password = doc.getString("password") ?: "",
                            role = UserRole.valueOf(doc.getString("role") ?: UserRole.PASSENGER.name),
                            rating = (doc.getDouble("rating") ?: 4.7).toFloat(),
                            vehicleModel = doc.getString("vehicleModel") ?: "",
                            vehicleReg = doc.getString("vehicleReg") ?: "",
                            documentUri = doc.getString("documentUri"),
                            verification = VerificationStatus.valueOf(doc.getString("verification") ?: VerificationStatus.NOT_SUBMITTED.name)
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                _users.value = userList
                // Keep currentUser reference updated if user details changed
                _currentUser.value?.let { current ->
                    userList.find { it.id == current.id }?.let { updated ->
                        _currentUser.value = updated
                    }
                }
            }
        }

        ridesListener = firestore.collection("rides").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            if (snapshot.isEmpty) {
                seedInitialRides()
            } else {
                val rideList = snapshot.documents.mapNotNull { doc ->
                    try {
                        Ride(
                            id = doc.getString("id") ?: doc.id,
                            driverId = doc.getString("driverId") ?: "",
                            pickup = doc.getString("pickup") ?: "",
                            destination = doc.getString("destination") ?: "",
                            date = doc.getString("date") ?: "",
                            time = doc.getString("time") ?: "",
                            seatsAvailable = (doc.getLong("seatsAvailable") ?: 0L).toInt(),
                            pricePerSeat = (doc.getLong("pricePerSeat") ?: 0L).toInt()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                _rides.value = rideList
            }
        }

        requestsListener = firestore.collection("requests").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val reqList = snapshot.documents.mapNotNull { doc ->
                try {
                    RideRequest(
                        id = doc.getString("id") ?: doc.id,
                        rideId = doc.getString("rideId") ?: "",
                        passengerId = doc.getString("passengerId") ?: "",
                        status = RequestStatus.valueOf(doc.getString("status") ?: RequestStatus.PENDING.name),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        paymentStatus = PaymentStatus.valueOf(doc.getString("paymentStatus") ?: PaymentStatus.UNPAID.name)
                    )
                } catch (e: Exception) {
                    null
                }
            }
            _requests.value = reqList
        }
    }

    private fun seedInitialUsers() {
        val initialUsers = listOf(
            User("d1", "Chisomo Banda", "991000001", "demo123", UserRole.DRIVER, 4.8f, "Toyota Hiace", "BT 4521", null, VerificationStatus.VERIFIED),
            User("d2", "Thandiwe Phiri", "991000002", "demo123", UserRole.DRIVER, 4.6f, "Toyota Noah", "LL 7709", null, VerificationStatus.VERIFIED),
            User("d3", "Mphatso Zulu", "991000003", "demo123", UserRole.DRIVER, 4.9f, "Nissan Caravan", "MZ 3390", null, VerificationStatus.VERIFIED),
            User("p1", "Grace Mwale", "888000001", "demo123", UserRole.PASSENGER)
        )
        initialUsers.forEach { user ->
            saveUserToFirestore(user)
        }
    }

    private fun seedInitialRides() {
        val initialRides = listOf(
            Ride("r1", "d1", "Lilongwe", "Blantyre", "Sat, 24 Oct", "06:30", 3, 18000),
            Ride("r2", "d2", "Lilongwe", "Mzuzu", "Sat, 24 Oct", "07:00", 2, 15000),
            Ride("r3", "d3", "Blantyre", "Zomba", "Sun, 25 Oct", "09:15", 4, 6000),
            Ride("r4", "d1", "Lilongwe", "Zomba", "Sun, 25 Oct", "13:00", 1, 12000),
            Ride("r5", "d2", "Mzuzu", "Lilongwe", "Mon, 26 Oct", "05:45", 3, 15000)
        )
        initialRides.forEach { ride ->
            saveRideToFirestore(ride)
        }
    }

    private fun saveUserToFirestore(user: User) {
        val userMap = mapOf(
            "id" to user.id,
            "name" to user.name,
            "phone" to user.phone,
            "password" to user.password,
            "role" to user.role.name,
            "rating" to user.rating,
            "vehicleModel" to user.vehicleModel,
            "vehicleReg" to user.vehicleReg,
            "documentUri" to user.documentUri,
            "verification" to user.verification.name
        )
        firestore.collection("users").document(user.id).set(userMap)
    }

    private fun saveRideToFirestore(ride: Ride) {
        val rideMap = mapOf(
            "id" to ride.id,
            "driverId" to ride.driverId,
            "pickup" to ride.pickup,
            "destination" to ride.destination,
            "date" to ride.date,
            "time" to ride.time,
            "seatsAvailable" to ride.seatsAvailable,
            "pricePerSeat" to ride.pricePerSeat
        )
        firestore.collection("rides").document(ride.id).set(rideMap)
    }

    private fun saveRequestToFirestore(request: RideRequest) {
        val requestMap = mapOf(
            "id" to request.id,
            "rideId" to request.rideId,
            "passengerId" to request.passengerId,
            "status" to request.status.name,
            "createdAt" to request.createdAt,
            "paymentStatus" to request.paymentStatus.name
        )
        firestore.collection("requests").document(request.id).set(requestMap)
    }

    override fun register(name: String, phone: String, password: String, role: UserRole): Result<User> {
        if (name.isBlank()) {
            return Result.failure(IllegalStateException("Please enter your name."))
        }
        val normPhone = InMemoryRideRepository.normalizePhone(phone)
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
        saveUserToFirestore(newUser)
        _currentUser.value = newUser
        return Result.success(newUser)
    }

    override fun login(phone: String, password: String): Result<User> {
        val normPhone = InMemoryRideRepository.normalizePhone(phone)
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
        saveUserToFirestore(updatedUser)
        _currentUser.value = updatedUser
        return Result.success(Unit)
    }

    override fun simulateAdminApproval(): Result<Unit> {
        val user = _currentUser.value
            ?: return Result.failure(IllegalStateException("There is nothing waiting for review."))
        if (user.role != UserRole.DRIVER || user.verification != VerificationStatus.PENDING) {
            return Result.failure(IllegalStateException("There is nothing waiting for review."))
        }

        val updatedUser = user.copy(verification = VerificationStatus.VERIFIED)
        saveUserToFirestore(updatedUser)
        _currentUser.value = updatedUser
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
        saveRideToFirestore(newRide)
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
        saveRequestToFirestore(newRequest)
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
            saveRideToFirestore(updatedRide)

            val updatedReq = request.copy(status = RequestStatus.ACCEPTED)
            saveRequestToFirestore(updatedReq)
            return Result.success(Unit)
        } else {
            val updatedReq = request.copy(status = RequestStatus.REJECTED)
            saveRequestToFirestore(updatedReq)
            return Result.success(Unit)
        }
    }

    override fun updatePaymentStatus(requestId: String, status: PaymentStatus): Result<Unit> {
        val request = _requests.value.find { it.id == requestId }
            ?: return Result.failure(IllegalStateException("Booking request not found."))
        val updatedReq = request.copy(paymentStatus = status)
        saveRequestToFirestore(updatedReq)
        return Result.success(Unit)
    }
}
