package com.rideshare.malawi.tests

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rideshare.malawi.data.AppState
import com.rideshare.malawi.navigation.RideShareNavHost
import com.rideshare.malawi.ui.theme.RideShareMalawiTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RideCreationTests {

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
    fun RS_RIDE_001_successfulRideCreation() {
        composeRule.loginAs()
        composeRule.chooseDriver()
        composeRule.verifyDriverThroughUi()
        composeRule.onNodeWithText("Go to driver home").performClick()

        composeRule.onNodeWithContentDescription("Create ride").performClick()

        composeRule.onNodeWithText("Pickup location").performTextInput("Kasungu")
        composeRule.onNodeWithText("Destination").performTextInput("Lilongwe")
        composeRule.onNodeWithText("Date").performTextInput("Mon, 13 Oct")
        composeRule.onNodeWithText("Time").performTextInput("07:30")
        composeRule.onNodeWithText("Seats available").performTextInput("3")
        composeRule.onNodeWithText("Price per seat (MWK)").performTextInput("8000")

        composeRule.onNodeWithText("Publish ride").performClick()

        composeRule.onNodeWithText("Kasungu  →  Lilongwe").assertIsDisplayed()
        composeRule.onNodeWithText("Mon, 13 Oct · 07:30").assertIsDisplayed()
        composeRule.onNodeWithText("3 seats left").assertIsDisplayed()
        composeRule.onNodeWithText("MWK 8,000").assertIsDisplayed()
    }


    @Test
    fun RS_RIDE_002_emptyFieldsKeepPublishDisabled() {
        composeRule.loginAs()
        composeRule.chooseDriver()
        composeRule.verifyDriverThroughUi()
        composeRule.onNodeWithText("Go to driver home").performClick()

        composeRule.onNodeWithContentDescription("Create ride").performClick()

        composeRule.onNodeWithText("Publish ride").assertIsNotEnabled()
        composeRule.onNodeWithText("Offer a ride").assertIsDisplayed()
    }


    @Test
    fun RS_RIDE_003_unverifiedDriverBlockedFromCreatingRide() {
        composeRule.loginAs()
        composeRule.chooseDriver()

        composeRule.onNodeWithContentDescription("Home").performClick()
        composeRule.onNodeWithContentDescription("Create ride").performClick()

        composeRule.onNodeWithText("Offer a ride").assertDoesNotExist()
    }


    @Test
    fun RS_RIDE_004_createdRideVisibleToPassengers() {
        composeRule.loginAs()
        composeRule.chooseDriver()
        composeRule.verifyDriverThroughUi()
        composeRule.onNodeWithText("Go to driver home").performClick()

        composeRule.onNodeWithContentDescription("Create ride").performClick()
        composeRule.onNodeWithText("Pickup location").performTextInput("Kasungu")
        composeRule.onNodeWithText("Destination").performTextInput("Lilongwe")
        composeRule.onNodeWithText("Date").performTextInput("Mon, 13 Oct")
        composeRule.onNodeWithText("Time").performTextInput("07:30")
        composeRule.onNodeWithText("Seats available").performTextInput("3")
        composeRule.onNodeWithText("Price per seat (MWK)").performTextInput("8000")
        composeRule.onNodeWithText("Publish ride").performClick()

        composeRule.pressBackUntilText("Welcome, Grace Mwale.")
        composeRule.choosePassenger()

        composeRule.onNodeWithText("6 rides available").assertIsDisplayed()
        composeRule.openFirst("Kasungu  →  Lilongwe")
    }
}
