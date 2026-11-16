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

class PaymentTests {

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
    fun RS_PAY_001_successfulPayChanguPayment() {
        val repo = AppGraph.repository

        // 1. Log in as passenger p1 (Grace Mwale)
        composeRule.onNodeWithText("Phone number").performTextInput("888000001")
        composeRule.onNodeWithText("Password").performTextInput("demo123")
        composeRule.onNodeWithText("Log in").performClick()
        composeRule.waitForIdle()

        // 2. Open seeded ride r1 using testTag
        composeRule.onNodeWithTag("ride_card_r1").performClick()
        composeRule.waitForIdle()

        // 3. Request a seat
        composeRule.onNodeWithText("Request a seat").performClick()
        composeRule.waitForIdle()

        // 4. Capture created request ID
        val reqId = repo.requests.value.first { it.passengerId == "p1" && it.rideId == "r1" }.id

        // 5. Log in as driver d1 (Chisomo Banda) and accept request
        repo.login("991000001", "demo123")
        val acceptResult = repo.respondToRequest(reqId, accept = true)
        assert(acceptResult.isSuccess)

        // 6. Return to passenger account
        repo.login("888000001", "demo123")

        // 7. Navigate to Bookings and open accepted booking
        composeRule.onNodeWithText("Bookings").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("booking_item_$reqId").performClick()
        composeRule.waitForIdle()

        // 8. Verify MWK 18,000 fare & PayChangu UI
        composeRule.onAllNodesWithText("MWK 18,000").onFirst().assertExists()
        composeRule.onNodeWithText("PayChangu Mobile Money").performScrollTo().assertIsDisplayed()

        // 9. Process PayChangu payment
        composeRule.onNodeWithText("Pay MWK 18,000 with PayChangu").performClick()

        // 10. Verify successful payment state
        composeRule.waitForText("MWK 18,000 Paid via PayChangu", timeoutMs = 5000)
        composeRule.onNodeWithText("MWK 18,000 Paid via PayChangu").assertIsDisplayed()

        // 11. Verify Pay button no longer exists (double payment prevented)
        composeRule.onNodeWithText("Pay MWK 18,000 with PayChangu").assertDoesNotExist()
    }

    @Test
    fun RS_PAY_002_pendingRequestHasNoPayButton() {
        val repo = AppGraph.repository

        // 1. Log in as passenger p1 (Grace Mwale)
        composeRule.onNodeWithText("Phone number").performTextInput("888000001")
        composeRule.onNodeWithText("Password").performTextInput("demo123")
        composeRule.onNodeWithText("Log in").performClick()
        composeRule.waitForIdle()

        // 2. Open seeded ride r1 using testTag
        composeRule.onNodeWithTag("ride_card_r1").performClick()
        composeRule.waitForIdle()

        // 3. Request a seat
        composeRule.onNodeWithText("Request a seat").performClick()
        composeRule.waitForIdle()

        // 4. Capture request ID (remains pending)
        val reqId = repo.requests.value.first { it.passengerId == "p1" && it.rideId == "r1" }.id

        // 5. Navigate to Bookings tab and open pending booking
        composeRule.onNodeWithText("Bookings").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("booking_item_$reqId").performClick()
        composeRule.waitForIdle()

        // 6. Assert pending status message is displayed and PayChangu is NOT available
        composeRule.onNodeWithText("Waiting for Chisomo Banda to respond.").assertIsDisplayed()
        composeRule.onNodeWithText("PayChangu Mobile Money").assertDoesNotExist()
        composeRule.onNodeWithText("Pay MWK 18,000 with PayChangu").assertDoesNotExist()
    }
}
