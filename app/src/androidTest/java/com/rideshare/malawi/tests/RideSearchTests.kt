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

class RideSearchTests {

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
    fun RS_SEARCH_001_searchLilongwe() {
        composeRule.loginAs()
        composeRule.choosePassenger()

        composeRule.onNodeWithText("e.g. Lilongwe").performTextInput("Lilongwe")

        composeRule.onNodeWithText("4 rides available").assertIsDisplayed()
        composeRule.onNodeWithText("Blantyre  →  Zomba").assertDoesNotExist()
    }


    @Test
    fun RS_SEARCH_002_searchZomba() {
        composeRule.loginAs()
        composeRule.choosePassenger()

        composeRule.onNodeWithText("e.g. Lilongwe").performTextInput("Zomba")

        composeRule.onNodeWithText("2 rides available").assertIsDisplayed()
        composeRule.onNodeWithText("Blantyre  →  Zomba").assertIsDisplayed()
        composeRule.onNodeWithText("Lilongwe  →  Zomba").assertIsDisplayed()
    }


    @Test
    fun RS_SEARCH_003_searchNoMatches() {
        composeRule.loginAs()
        composeRule.choosePassenger()

        composeRule.onNodeWithText("e.g. Lilongwe").performTextInput("Karonga")

        composeRule.onNodeWithText("0 rides available").assertIsDisplayed()
        composeRule.onNodeWithText("No rides match that search yet. Try a different town.").assertIsDisplayed()
    }
}
