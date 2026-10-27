package com.rideshare.malawi.tests

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rideshare.malawi.data.AppState
import com.rideshare.malawi.data.RequestStatus
import com.rideshare.malawi.data.VerificationStatus
import com.rideshare.malawi.navigation.RideShareNavHost
import com.rideshare.malawi.ui.theme.RideShareMalawiTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RideRequestTests {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        AppState.resetForTesting()
        composeRule.setContent {
            RideShareMalawiTheme {
                RideShareNavHost()
            }
        }
    }


    @Test
    fun RS_REQUEST_001_passengerRequestsSeat() {
        composeRule.loginAs("Grace Mwale", "0991234567")
        composeRule.choosePassenger()

        composeRule.openFirst("Lilongwe  →  Blantyre")
        composeRule.onNodeWithText("Request a seat").performClick()

        composeRule.onNodeWithText("Find a ride").assertIsDisplayed()
        composeRule.onNodeWithText("Your requests").assertIsDisplayed()
        composeRule.openFirst("Lilongwe → Blantyre")
        composeRule.onNodeWithText("Pending").assertIsDisplayed()
    }

    @Test
    fun RS_REQUEST_002_driverViewsPendingRequest() {
        AppState.driverVerification.value = VerificationStatus.VERIFIED
        val ride = AppState.rides.first { it.id == "r1" }
        AppState.requestSeat(ride, "Grace Mwale")

        composeRule.loginAs()
        composeRule.chooseDriver()
        composeRule.onNodeWithText("Go to driver home").performClick()

        composeRule.onNodeWithText("1 awaiting your response").assertIsDisplayed()
        composeRule.onNodeWithText("Ride requests").performClick()

        composeRule.onNodeWithText("Grace Mwale").assertIsDisplayed()
        composeRule.onNodeWithText("Lilongwe → Blantyre · Fri, 10 Oct").assertIsDisplayed()
        composeRule.onNodeWithText("Pending").assertIsDisplayed()
        composeRule.onNodeWithText("Decline").assertIsDisplayed()
        composeRule.onNodeWithText("Accept").assertIsDisplayed()
    }


    @Test
    fun RS_REQUEST_003_driverAcceptsRequest() {
        AppState.driverVerification.value = VerificationStatus.VERIFIED
        val ride = AppState.rides.first { it.id == "r1" }
        val req = AppState.requestSeat(ride, "Grace Mwale")

        composeRule.loginAs()
        composeRule.chooseDriver()
        composeRule.onNodeWithText("Go to driver home").performClick()
        composeRule.onNodeWithText("Ride requests").performClick()

        composeRule.onNodeWithText("Accept").performClick()

        composeRule.onNodeWithText("Accepted").assertIsDisplayed()
        composeRule.onNodeWithText("Accept").assertDoesNotExist()
        composeRule.onNodeWithText("Decline").assertDoesNotExist()

        assertEquals(RequestStatus.ACCEPTED, AppState.incomingRequests.first { it.id == req.id }.status)
    }


    @Test
    fun RS_REQUEST_004_driverDeclinesRequest() {
        AppState.driverVerification.value = VerificationStatus.VERIFIED
        val ride = AppState.rides.first { it.id == "r1" }
        val req = AppState.requestSeat(ride, "Grace Mwale")

        composeRule.loginAs()
        composeRule.chooseDriver()
        composeRule.onNodeWithText("Go to driver home").performClick()
        composeRule.onNodeWithText("Ride requests").performClick()

        composeRule.onNodeWithText("Decline").performClick()

        composeRule.onNodeWithText("Declined").assertIsDisplayed()
        composeRule.onNodeWithText("Accept").assertDoesNotExist()
        composeRule.onNodeWithText("Decline").assertDoesNotExist()

        assertEquals(RequestStatus.REJECTED, AppState.incomingRequests.first { it.id == req.id }.status)
    }


    @Test
    fun RS_REQUEST_005_passengerViewsDeclinedRequest() {
        val ride = AppState.rides.first { it.id == "r1" }
        val req = AppState.requestSeat(ride, "Grace Mwale")
        AppState.updateRequestStatus(req, RequestStatus.REJECTED)

        composeRule.loginAs("Grace Mwale", "0991234567")
        composeRule.choosePassenger()

        composeRule.openFirst("Lilongwe → Blantyre")

        composeRule.onNodeWithText("Declined").assertIsDisplayed()
        composeRule.onNodeWithText("This driver declined your request.").assertIsDisplayed()
        composeRule.onNodeWithText("Try searching for another ride on the same route.").assertIsDisplayed()
        composeRule.onNodeWithText("Pay with PayChangu (sandbox)").assertDoesNotExist()
    }
}
