package com.rideshare.malawi.tests

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rideshare.malawi.data.AppState
import com.rideshare.malawi.data.Driver
import com.rideshare.malawi.data.Ride
import com.rideshare.malawi.data.VerificationStatus
import com.rideshare.malawi.navigation.RideShareNavHost
import com.rideshare.malawi.ui.theme.RideShareMalawiTheme
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RideDetailsTests {

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
    fun RS_DETAIL_001_rideDetailsDisplayed() {
        composeRule.loginAs()
        composeRule.choosePassenger()

        composeRule.openFirst("Lilongwe  →  Blantyre")

        composeRule.onNodeWithText("Ride details").assertIsDisplayed()
        composeRule.onNodeWithText("Lilongwe → Blantyre").assertIsDisplayed()
        composeRule.onNodeWithText("Fri, 10 Oct · Departs 06:30").assertIsDisplayed()
        composeRule.onNodeWithText("3 seat(s) available").assertIsDisplayed()
        composeRule.onNodeWithText("Chisomo Banda · ★ 4.8 rating").assertIsDisplayed()
        composeRule.onNodeWithText("Verified").assertIsDisplayed()
        composeRule.onNodeWithText("MWK 18,000").assertIsDisplayed()
        composeRule.onNodeWithText("Request a seat").assertIsEnabled().assertIsDisplayed()
    }


    @Test
    fun RS_DETAIL_002_backArrowExitsDetailsWithoutRequesting() {
        composeRule.loginAs()
        composeRule.choosePassenger()

        composeRule.openFirst("Lilongwe  →  Blantyre")
        composeRule.onNodeWithContentDescription("Back").performClick()

        composeRule.onNodeWithText("Find a ride").assertIsDisplayed()
        composeRule.onNodeWithText("Your requests").assertDoesNotExist()
        assertTrue(AppState.myRequests.isEmpty())
    }


    @Test
    fun RS_DETAIL_003_fullRideDisablesRequestButton() {
        val fullDriver = Driver("d-full", "Banda", "+265 990 00 00 00", "LL 111", VerificationStatus.VERIFIED, 4.5f)
        val fullRide = Ride("r-full", fullDriver, "Salima", "Lilongwe", "Sun, 12 Oct", "10:00", 0, 5000)
        AppState.addRide(fullRide)

        composeRule.loginAs()
        composeRule.choosePassenger()

        composeRule.openFirst("Salima  →  Lilongwe")

        composeRule.onNodeWithText("0 seat(s) available").assertIsDisplayed()
        composeRule.onNodeWithText("Request a seat").assertIsNotEnabled()
        assertTrue(AppState.myRequests.isEmpty())
    }
}
