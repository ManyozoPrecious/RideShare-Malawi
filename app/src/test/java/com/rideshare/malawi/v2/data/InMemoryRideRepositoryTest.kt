package com.rideshare.malawi.v2.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InMemoryRideRepositoryTest {

    private lateinit var repo: InMemoryRideRepository

    @Before
    fun setUp() {
        repo = InMemoryRideRepository()
    }

    @Test
    fun registering_logsInUser() {
        val res = repo.register("Test User", "0999123456", "password123", UserRole.PASSENGER)
        assertTrue(res.isSuccess)

        val user = res.getOrNull()
        assertNotNull(user)
        assertEquals("Test User", user?.name)
        assertEquals("999123456", user?.phone)
        assertEquals(user, repo.currentUser.value)
    }

    @Test
    fun register_rejectsInvalidInputs() {
        // Blank name
        val blankName = repo.register("", "0999123456", "password123", UserRole.PASSENGER)
        assertTrue(blankName.isFailure)
        assertEquals("Please enter your name.", blankName.exceptionOrNull()?.message)

        // Bad phone
        val badPhone = repo.register("Test User", "123", "password123", UserRole.PASSENGER)
        assertTrue(badPhone.isFailure)
        assertEquals("Enter a valid phone number.", badPhone.exceptionOrNull()?.message)

        // Short password
        val shortPass = repo.register("Test User", "0999123456", "123", UserRole.PASSENGER)
        assertTrue(shortPass.isFailure)
        assertEquals("Password must be at least 6 characters.", shortPass.exceptionOrNull()?.message)

        // Duplicate phone (seed user Chisomo Banda has 991000001)
        val dupPhone = repo.register("Test User", "+265 991 000 001", "password123", UserRole.PASSENGER)
        assertTrue(dupPhone.isFailure)
        assertEquals("An account with this phone number already exists.", dupPhone.exceptionOrNull()?.message)
    }

    @Test
    fun login_worksWithDifferentPhoneFormats_andFailsWithWrongPassword() {
        // Chisomo Banda: phone stored as "991000001", pass "demo123"
        val loginLocal = repo.login("0991 000 001", "demo123")
        assertTrue(loginLocal.isSuccess)
        assertEquals("d1", repo.currentUser.value?.id)

        repo.logout()
        assertNull(repo.currentUser.value)

        val loginIntl = repo.login("+265 991 000 001", "demo123")
        assertTrue(loginIntl.isSuccess)
        assertEquals("d1", repo.currentUser.value?.id)

        repo.logout()

        val wrongPass = repo.login("991000001", "wrongpass")
        assertTrue(wrongPass.isFailure)
        assertEquals("Incorrect phone number or password.", wrongPass.exceptionOrNull()?.message)
    }

    @Test
    fun unverifiedDriver_blockedUntilSubmitAndApproval() {
        // Register new driver
        val reg = repo.register("New Driver", "0999888777", "password123", UserRole.DRIVER)
        assertTrue(reg.isSuccess)

        // Attempt create ride - blocked
        val ride1 = repo.createRide("Lilongwe", "Blantyre", "Tomorrow", "08:00", 3, 10000)
        assertTrue(ride1.isFailure)
        assertEquals("You need to be verified before you can offer rides.", ride1.exceptionOrNull()?.message)

        // Submit verification
        val sub = repo.submitVerification("Toyota Vitz", "BT 100", "content://doc/1")
        assertTrue(sub.isSuccess)
        assertEquals(VerificationStatus.PENDING, repo.currentUser.value?.verification)

        // Still blocked while PENDING
        val ride2 = repo.createRide("Lilongwe", "Blantyre", "Tomorrow", "08:00", 3, 10000)
        assertTrue(ride2.isFailure)
        assertEquals("You need to be verified before you can offer rides.", ride2.exceptionOrNull()?.message)

        // Admin approval
        val app = repo.simulateAdminApproval()
        assertTrue(app.isSuccess)
        assertEquals(VerificationStatus.VERIFIED, repo.currentUser.value?.verification)

        // Now can create ride
        val ride3 = repo.createRide("Lilongwe", "Blantyre", "Tomorrow", "08:00", 3, 10000)
        assertTrue(ride3.isSuccess)
    }

    @Test
    fun verification_needsDocumentAndDetails() {
        repo.register("New Driver", "0999888777", "password123", UserRole.DRIVER)

        val noModel = repo.submitVerification("", "BT 100", "doc_uri")
        assertTrue(noModel.isFailure)
        assertEquals("Enter your vehicle make and model.", noModel.exceptionOrNull()?.message)

        val noReg = repo.submitVerification("Toyota Vitz", "", "doc_uri")
        assertTrue(noReg.isFailure)
        assertEquals("Enter your vehicle registration number.", noReg.exceptionOrNull()?.message)

        val noDoc = repo.submitVerification("Toyota Vitz", "BT 100", null)
        assertTrue(noDoc.isFailure)
        assertEquals("Attach a photo of your registration document.", noDoc.exceptionOrNull()?.message)
    }

    @Test
    fun rideValidation_checksAllFields() {
        // Log in as seed driver d1 (verified)
        repo.login("991000001", "demo123")

        val noPickup = repo.createRide("", "Blantyre", "Tomorrow", "08:00", 3, 10000)
        assertTrue(noPickup.isFailure)
        assertEquals("Enter both a pickup location and a destination.", noPickup.exceptionOrNull()?.message)

        val noDate = repo.createRide("Lilongwe", "Blantyre", "", "08:00", 3, 10000)
        assertTrue(noDate.isFailure)
        assertEquals("Enter the date and time of the ride.", noDate.exceptionOrNull()?.message)

        val noSeats = repo.createRide("Lilongwe", "Blantyre", "Tomorrow", "08:00", 0, 10000)
        assertTrue(noSeats.isFailure)
        assertEquals("A ride needs at least one seat.", noSeats.exceptionOrNull()?.message)

        val noPrice = repo.createRide("Lilongwe", "Blantyre", "Tomorrow", "08:00", 3, 0)
        assertTrue(noPrice.isFailure)
        assertEquals("Enter a price per seat.", noPrice.exceptionOrNull()?.message)
    }

    @Test
    fun passengerCannotRequestTwice_andDriverCannotRequest() {
        // Driver attempts request
        repo.login("991000001", "demo123")
        val driverReq = repo.requestSeat("r1")
        assertTrue(driverReq.isFailure)
        assertEquals("Log in as a passenger to request a seat.", driverReq.exceptionOrNull()?.message)

        // Log in as seed passenger p1
        repo.login("888000001", "demo123")

        // First request works
        val req1 = repo.requestSeat("r1")
        assertTrue(req1.isSuccess)

        // Duplicate request fails
        val req2 = repo.requestSeat("r1")
        assertTrue(req2.isFailure)
        assertEquals("You have already requested a seat on this ride.", req2.exceptionOrNull()?.message)
    }

    @Test
    fun respondToRequest_acceptReducesSeats_andDeclineKeepsSeats() {
        // Passenger requests seat on r1 (driver d1, seatsAvailable = 3)
        repo.login("888000001", "demo123")
        val reqRes = repo.requestSeat("r1")
        val req = reqRes.getOrThrow()

        // Driver d1 responds
        repo.login("991000001", "demo123")

        // Accept
        val acceptRes = repo.respondToRequest(req.id, accept = true)
        assertTrue(acceptRes.isSuccess)

        val updatedRide = repo.rides.value.find { it.id == "r1" }
        assertEquals(2, updatedRide?.seatsAvailable)

        val updatedReq = repo.requests.value.find { it.id == req.id }
        assertEquals(RequestStatus.ACCEPTED, updatedReq?.status)
    }

    @Test
    fun declineKeepsSeats() {
        // Passenger requests seat on r2 (driver d2, seatsAvailable = 2)
        repo.login("888000001", "demo123")
        val reqRes = repo.requestSeat("r2")
        val req = reqRes.getOrThrow()

        // Driver d2 declines
        repo.login("991000002", "demo123")

        val declineRes = repo.respondToRequest(req.id, accept = false)
        assertTrue(declineRes.isSuccess)

        val updatedRide = repo.rides.value.find { it.id == "r2" }
        assertEquals(2, updatedRide?.seatsAvailable)

        val updatedReq = repo.requests.value.find { it.id == req.id }
        assertEquals(RequestStatus.REJECTED, updatedReq?.status)
    }

    @Test
    fun permissions_onlyOwningDriverCanAnswer_andPassengerCannotAnswer() {
        // Passenger requests seat on r1 (driver d1)
        repo.login("888000001", "demo123")
        val req = repo.requestSeat("r1").getOrThrow()

        // Passenger attempts to answer
        val passAnswer = repo.respondToRequest(req.id, accept = true)
        assertTrue(passAnswer.isFailure)
        assertEquals("Only drivers can respond to requests.", passAnswer.exceptionOrNull()?.message)

        // Wrong driver d2 attempts to answer
        repo.login("991000002", "demo123")
        val wrongDriverAnswer = repo.respondToRequest(req.id, accept = true)
        assertTrue(wrongDriverAnswer.isFailure)
        assertEquals("That request is for someone else's ride.", wrongDriverAnswer.exceptionOrNull()?.message)
    }

    @Test
    fun lastSeatCanBeGivenOnce_thenRideIsFull() {
        // r4 has driver d1 and seatsAvailable = 1
        // Register passenger 1
        repo.register("Pass 1", "0881112223", "password123", UserRole.PASSENGER)
        val req1 = repo.requestSeat("r4").getOrThrow()

        // Register passenger 2
        repo.register("Pass 2", "0884445556", "password123", UserRole.PASSENGER)
        val req2 = repo.requestSeat("r4").getOrThrow()

        // Driver d1 accepts req1
        repo.login("991000001", "demo123")
        val acc1 = repo.respondToRequest(req1.id, accept = true)
        assertTrue(acc1.isSuccess)

        val rideR4 = repo.rides.value.find { it.id == "r4" }
        assertEquals(0, rideR4?.seatsAvailable)

        // Driver d1 tries to accept req2 - should fail because ride is full
        val acc2 = repo.respondToRequest(req2.id, accept = true)
        assertTrue(acc2.isFailure)
        assertEquals("No seats left on this ride.", acc2.exceptionOrNull()?.message)
    }
}
