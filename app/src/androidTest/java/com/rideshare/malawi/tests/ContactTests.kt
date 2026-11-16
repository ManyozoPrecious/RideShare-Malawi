package com.rideshare.malawi.tests

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rideshare.malawi.ui.theme.RideShareMalawiTheme
import com.rideshare.malawi.v2.data.AppGraph
import com.rideshare.malawi.v2.data.InMemoryRideRepository
import com.rideshare.malawi.v2.navigation.RideShareNavHost
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ContactTests {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        AppGraph.repository = InMemoryRideRepository()
        composeRule.setContent {
            RideShareMalawiTheme {
                RideShareNavHost()
            }
        }
    }

    @Test
    fun RS_CONTACT_001_driverContactVisibleAfterPayChanguPayment() {
        val repo = AppGraph.repository

        // Log in as passenger Grace Mwale
        composeRule.onNodeWithText("Phone number").performTextInput("888000001")
        composeRule.onNodeWithText("Password").performTextInput("demo123")
        composeRule.onNodeWithText("Log in").performClick()
        composeRule.waitForIdle()

        // Request seat
        composeRule.onNodeWithTag("ride_card_r1").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Request a seat").performClick()
        composeRule.waitForIdle()

        val reqId = repo.requests.value.first { it.passengerId == "p1" && it.rideId == "r1" }.id

        // Driver d1 accepts
        repo.login("991000001", "demo123")
        repo.respondToRequest(reqId, accept = true)

        // Return to passenger
        repo.login("888000001", "demo123")

        // Navigate to Bookings
        composeRule.onNodeWithText("Bookings").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("booking_item_$reqId").performClick()
        composeRule.waitForIdle()

        // Pay with PayChangu
        composeRule.onNodeWithText("Pay MWK 18,000 with PayChangu").performClick()
        composeRule.waitForText("MWK 18,000 Paid via PayChangu", timeoutMs = 5000)

        // Driver contact unlocked
        composeRule.onNodeWithText("Driver contact").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Chisomo Banda").assertIsDisplayed()
        composeRule.onNodeWithText("Call").assertIsDisplayed()
        composeRule.onNodeWithText("SMS").assertIsDisplayed()
    }

    @Test
    fun RS_CONTACT_002_driverContactHiddenBeforePayment() {
        val repo = AppGraph.repository

        // Log in as passenger Grace Mwale
        composeRule.onNodeWithText("Phone number").performTextInput("888000001")
        composeRule.onNodeWithText("Password").performTextInput("demo123")
        composeRule.onNodeWithText("Log in").performClick()
        composeRule.waitForIdle()

        // Request seat
        composeRule.onNodeWithTag("ride_card_r1").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Request a seat").performClick()
        composeRule.waitForIdle()

        val reqId = repo.requests.value.first { it.passengerId == "p1" && it.rideId == "r1" }.id

        // Driver d1 accepts
        repo.login("991000001", "demo123")
        repo.respondToRequest(reqId, accept = true)

        // Return to passenger
        repo.login("888000001", "demo123")

        // Navigate to Bookings
        composeRule.onNodeWithText("Bookings").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("booking_item_$reqId").performClick()
        composeRule.waitForIdle()

        // Contact is hidden before payment
        composeRule.onNodeWithText("Driver contact").assertDoesNotExist()
        composeRule.onNodeWithText("Complete payment above to unlock driver contact details (Call & SMS).").assertIsDisplayed()
    }
}
