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

class RoleSelectionTests {

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
    fun RS_ROLE_001_chooseDriver() {
        composeRule.loginAs("Grace Mwale", "0991234567")
        composeRule.chooseDriver()

        composeRule.onNodeWithText("Driver verification").assertIsDisplayed()
        composeRule.onNodeWithText("Pending review").assertIsDisplayed()
        composeRule.onNodeWithText("Vehicle make & model").assertIsDisplayed()
        composeRule.onNodeWithText("Vehicle registration number").assertIsDisplayed()
        composeRule.onNodeWithText("Attach a photo of your registration document").assertIsDisplayed()
        composeRule.onNodeWithText("Submit for verification").assertIsNotEnabled()
    }


    @Test
    fun RS_ROLE_002_choosePassenger() {
        composeRule.loginAs("Grace Mwale", "0991234567")
        composeRule.choosePassenger()

        composeRule.onNodeWithText("Find a ride").assertIsDisplayed()
        composeRule.onNodeWithText("e.g. Lilongwe").assertIsDisplayed()
        composeRule.onNodeWithText("5 rides available").assertIsDisplayed()
    }


    @Test
    fun RS_ROLE_003_backFromRoleSelection() {
        composeRule.loginAs("Grace Mwale", "0991234567")
        composeRule.onNodeWithText("Welcome, Grace Mwale.").assertIsDisplayed()

        composeRule.pressBack()

        composeRule.onNodeWithText("Continue").assertIsDisplayed()
        composeRule.onNodeWithText("Welcome, Grace Mwale.").assertDoesNotExist()
    }
}
