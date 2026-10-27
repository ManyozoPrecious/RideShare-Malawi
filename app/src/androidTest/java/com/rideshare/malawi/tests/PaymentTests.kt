package com.rideshare.malawi.tests

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rideshare.malawi.data.AppState
import com.rideshare.malawi.data.RequestStatus
import com.rideshare.malawi.navigation.RideShareNavHost
import com.rideshare.malawi.ui.theme.RideShareMalawiTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PaymentTests {

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
    fun RS_PAY_001_successfulPayment() {
        val ride = AppState.rides.first { it.id == "r1" }
        val req = AppState.requestSeat(ride, "Grace Mwale")
        AppState.updateRequestStatus(req, RequestStatus.ACCEPTED)

        composeRule.loginAs("Grace Mwale", "0991234567")
        composeRule.choosePassenger()

        composeRule.openFirst("Lilongwe → Blantyre")

        composeRule.onNodeWithText("MWK 18,000").assertIsDisplayed()
        composeRule.onNodeWithText("Pay with PayChangu (sandbox)").performClick()

        composeRule.waitForText("Payment successful", timeoutMs = 5000)
        composeRule.onNodeWithText("MWK 18,000 paid").assertIsDisplayed()
    }


    @Test
    fun RS_PAY_002_pendingRequestHasNoPayButton() {
        val ride = AppState.rides.first { it.id == "r1" }
        AppState.requestSeat(ride, "Grace Mwale")

        composeRule.loginAs("Grace Mwale", "0991234567")
        composeRule.choosePassenger()

        composeRule.openFirst("Lilongwe → Blantyre")

        composeRule.onNodeWithText("Waiting for Chisomo Banda to respond to your request.").assertIsDisplayed()
        composeRule.onNodeWithText("Pay with PayChangu (sandbox)").assertDoesNotExist()
    }


    @Test
    fun RS_PAY_003_doublePaymentPrevented() {
        val ride = AppState.rides.first { it.id == "r1" }
        val req = AppState.requestSeat(ride, "Grace Mwale")
        AppState.updateRequestStatus(req, RequestStatus.ACCEPTED)

        composeRule.loginAs("Grace Mwale", "0991234567")
        composeRule.choosePassenger()

        composeRule.openFirst("Lilongwe → Blantyre")

        composeRule.onNodeWithText("Pay with PayChangu (sandbox)").performClick()

        composeRule.onNodeWithText("Pay with PayChangu (sandbox)").assertDoesNotExist()
        composeRule.onNodeWithText("Processing payment…").assertIsDisplayed()

        composeRule.waitForText("Payment successful", timeoutMs = 5000)
        composeRule.onAllNodesWithText("Payment successful").assertCountEquals(1)
    }
}
