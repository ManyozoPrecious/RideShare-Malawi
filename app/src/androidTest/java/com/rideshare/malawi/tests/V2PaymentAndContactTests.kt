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

class V2PaymentAndContactTests {

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
    fun v2_payChanguMobileMoneyPayment_unlocksDriverContact() {
        val repo = AppGraph.repository

        // 1. Log in as seed passenger Grace Mwale (phone 888000001, pass demo123)
        composeRule.onNodeWithText("Phone number").performTextInput("888000001")
        composeRule.onNodeWithText("Password").performTextInput("demo123")
        composeRule.onNodeWithText("Log in").performClick()
        composeRule.waitForIdle()

        // 2. Request seat on ride r1 using testTag
        composeRule.onNodeWithTag("ride_card_r1").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Request a seat").performClick()
        composeRule.waitForIdle()

        // 3. Accept the request as driver d1
        val reqId = repo.requests.value.first { it.passengerId == "p1" && it.rideId == "r1" }.id
        repo.login("991000001", "demo123")
        repo.respondToRequest(reqId, accept = true)

        // 4. Return to passenger p1
        repo.login("888000001", "demo123")

        // 5. Navigate to Bookings tab and open booking details
        composeRule.onNodeWithText("Bookings").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("booking_item_$reqId").performClick()
        composeRule.waitForIdle()

        // 6. Verify Driver Contact is HIDDEN before payment
        composeRule.onNodeWithText("Driver contact").assertDoesNotExist()
        composeRule.onNodeWithText("Complete payment above to unlock driver contact details (Call & SMS).").assertIsDisplayed()

        // 7. Verify PayChangu card elements exist
        composeRule.onNodeWithText("PayChangu Mobile Money").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Airtel Money").assertIsDisplayed()
        composeRule.onNodeWithText("TNM Mpamba").assertIsDisplayed()

        // 8. Perform PayChangu payment
        composeRule.onNodeWithText("Pay MWK 18,000 with PayChangu").performClick()

        // 9. Wait for payment success
        composeRule.waitForText("MWK 18,000 Paid via PayChangu", timeoutMs = 5000)

        // 10. Verify Driver Contact is now UNLOCKED
        composeRule.onNodeWithText("Driver contact").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Chisomo Banda").assertIsDisplayed()
        composeRule.onNodeWithText("Call").assertIsDisplayed()
        composeRule.onNodeWithText("SMS").assertIsDisplayed()
    }
}
