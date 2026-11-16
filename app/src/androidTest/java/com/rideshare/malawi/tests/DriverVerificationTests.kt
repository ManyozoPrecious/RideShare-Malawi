package com.rideshare.malawi.tests

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rideshare.malawi.data.AppState
import com.rideshare.malawi.data.VerificationStatus
import com.rideshare.malawi.navigation.RideShareNavHost
import com.rideshare.malawi.ui.theme.RideShareMalawiTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DriverVerificationTests {

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
    fun RS_VERIFY_001_verificationFormDisplayed() {
        composeRule.loginAs()
        composeRule.chooseDriver()

        composeRule.onNodeWithText("Pending review").assertIsDisplayed()
        composeRule.onNodeWithText("Vehicle make & model").assertIsDisplayed()
        composeRule.onNodeWithText("Vehicle registration number").assertIsDisplayed()
        composeRule.onNodeWithText("Submit for verification").assertIsNotEnabled()
    }


    @Test
    fun RS_VERIFY_002_successfulVerification() {
        composeRule.loginAs()
        composeRule.chooseDriver()

        composeRule.verifyDriverThroughUi("Toyota Hiace", "BT 4521")

        composeRule.onNodeWithText("Verified").assertIsDisplayed()
        composeRule.onNodeWithText("You're verified and ready to offer rides.").assertIsDisplayed()
        composeRule.onNodeWithText("Go to driver home").assertIsDisplayed()

        assertEquals(VerificationStatus.VERIFIED, AppState.driverVerification.value)
    }


    @Test
    fun RS_VERIFY_003_missingDocumentKeepsButtonDisabled() {
        composeRule.loginAs()
        composeRule.chooseDriver()

        composeRule.onNodeWithText("Vehicle make & model").performTextInput("Toyota Hiace")
        composeRule.onNodeWithText("Vehicle registration number").performTextInput("BT 4521")

        composeRule.onNodeWithText("Submit for verification").assertIsNotEnabled()
        composeRule.onNodeWithText("Pending review").assertIsDisplayed()
    }
}
