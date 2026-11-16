package com.rideshare.malawi.tests

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rideshare.malawi.ui.theme.RideShareMalawiTheme
import com.rideshare.malawi.v2.data.AppGraph
import com.rideshare.malawi.v2.data.InMemoryRideRepository
import com.rideshare.malawi.v2.data.UserRole
import com.rideshare.malawi.v2.navigation.RideShareNavHost
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RideCreationTests {

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
    fun RS_RIDE_001_successfulRideCreation() {
        // Log in as seed driver d1 (Chisomo Banda - verified driver)
        composeRule.onNodeWithText("Phone number").performTextInput("991000001")
        composeRule.onNodeWithText("Password").performTextInput("demo123")
        composeRule.onNodeWithText("Log in").performClick()
        composeRule.waitForIdle()

        // Click FAB to create ride
        composeRule.onNodeWithContentDescription("Create ride").performClick()
        composeRule.waitForIdle()

        // Fill form
        composeRule.onNodeWithText("Pickup location").performTextInput("Kasungu")
        composeRule.onNodeWithText("Destination").performTextInput("Lilongwe")
        composeRule.onNodeWithText("Date").performTextInput("Mon, 13 Oct")
        composeRule.onNodeWithText("Time").performTextInput("07:30")
        composeRule.onNodeWithText("Seats available").performTextInput("3")
        composeRule.onNodeWithText("Price per seat (MWK)").performTextInput("8000")

        composeRule.onNodeWithText("Publish ride").performClick()
        composeRule.waitForIdle()

        // Verify ride card is displayed
        composeRule.onNodeWithText("Kasungu → Lilongwe").assertIsDisplayed()
        composeRule.onNodeWithText("Mon, 13 Oct · 07:30").assertIsDisplayed()
        composeRule.onNodeWithText("MWK 8,000").assertIsDisplayed()
    }

    @Test
    fun RS_RIDE_002_emptyFieldsKeepPublishDisabled() {
        // Log in as seed driver d1
        composeRule.onNodeWithText("Phone number").performTextInput("991000001")
        composeRule.onNodeWithText("Password").performTextInput("demo123")
        composeRule.onNodeWithText("Log in").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Create ride").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Publish ride").assertIsNotEnabled()
        composeRule.onNodeWithText("Offer a ride").assertIsDisplayed()
    }

    @Test
    fun RS_RIDE_003_unverifiedDriverBlockedFromCreatingRide() {
        val repo = AppGraph.repository
        repo.register("Unverified Driver", "0999000111", "password123", UserRole.DRIVER)
        repo.logout()

        composeRule.onNodeWithText("Phone number").performTextInput("0999000111")
        composeRule.onNodeWithText("Password").performTextInput("password123")
        composeRule.onNodeWithText("Log in").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("You are not verified yet. Tap here to complete driver verification and offer rides.").assertExists()
        composeRule.onNodeWithContentDescription("Create ride").assertDoesNotExist()
    }
}
