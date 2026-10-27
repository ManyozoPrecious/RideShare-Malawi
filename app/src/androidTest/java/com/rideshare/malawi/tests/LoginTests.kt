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

class LoginTests {

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
    fun RS_LOGIN_001_successfulLogin() {
        composeRule.onNodeWithText("Full name").performTextInput("Grace Mwale")
        composeRule.onNodeWithText("Phone number").performTextInput("0991234567")
        composeRule.onNodeWithText("Continue").assertIsEnabled().performClick()

        composeRule.onNodeWithText("Welcome, Grace Mwale.").assertIsDisplayed()
        composeRule.onNodeWithText("I'm a driver").assertIsDisplayed()
        composeRule.onNodeWithText("I'm a passenger").assertIsDisplayed()
    }


    @Test
    fun RS_LOGIN_002_loginWithBothFieldsEmpty() {
        composeRule.onNodeWithText("Continue").assertIsNotEnabled()
        composeRule.onNodeWithText("Full name").assertIsDisplayed()
        composeRule.onNodeWithText("Phone number").assertIsDisplayed()
    }


    @Test
    fun RS_LOGIN_003_loginWithPhoneEmpty() {
        composeRule.onNodeWithText("Full name").performTextInput("Grace Mwale")
        composeRule.onNodeWithText("Continue").assertIsNotEnabled()
    }

    @Test
    fun RS_LOGIN_004_loginWithShortPhone() {
        composeRule.onNodeWithText("Full name").performTextInput("Grace Mwale")
        composeRule.onNodeWithText("Phone number").performTextInput("09912345")
        composeRule.onNodeWithText("Continue").assertIsNotEnabled()
    }
}
