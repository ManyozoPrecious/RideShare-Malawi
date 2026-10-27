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

class ContactTests {

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
    fun RS_CONTACT_001_driverContactVisibleAfterPayment() {
        val ride = AppState.rides.first { it.id == "r1" }
        val req = AppState.requestSeat(ride, "Grace Mwale")
        AppState.updateRequestStatus(req, RequestStatus.ACCEPTED)

        composeRule.loginAs("Grace Mwale", "0991234567")
        composeRule.choosePassenger()

        composeRule.openFirst("Lilongwe → Blantyre")
        composeRule.onNodeWithText("Pay with PayChangu (sandbox)").performClick()

        composeRule.waitForText("Payment successful", timeoutMs = 5000)

        composeRule.onNodeWithText("Driver contact").assertIsDisplayed()
        composeRule.onNodeWithText("Chisomo Banda").assertIsDisplayed()
        composeRule.onNodeWithText("+265 991 23 45 67").assertIsDisplayed()
    }


    @Test
    fun RS_CONTACT_002_driverContactHiddenBeforePayment() {
        val ride = AppState.rides.first { it.id == "r1" }
        val req = AppState.requestSeat(ride, "Grace Mwale")
        AppState.updateRequestStatus(req, RequestStatus.ACCEPTED)

        composeRule.loginAs("Grace Mwale", "0991234567")
        composeRule.choosePassenger()

        composeRule.openFirst("Lilongwe → Blantyre")

        composeRule.onNodeWithText("Driver contact").assertDoesNotExist()
        composeRule.onNodeWithText("+265 991 23 45 67").assertDoesNotExist()
    }
}
