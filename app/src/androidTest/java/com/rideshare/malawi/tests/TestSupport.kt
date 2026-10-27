package com.rideshare.malawi.tests

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule

typealias ComposeRule = AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>

fun ComposeRule.loginAs(name: String = "Grace Mwale", phone: String = "0991234567") {
    onNodeWithText("Full name").performTextInput(name)
    onNodeWithText("Phone number").performTextInput(phone)
    onNodeWithText("Continue").performClick()
    waitForIdle()
}

fun ComposeRule.chooseDriver() {
    onNodeWithText("I'm a driver").performClick()
    waitForIdle()
}

fun ComposeRule.choosePassenger() {
    onNodeWithText("I'm a passenger").performClick()
    waitForIdle()
}

fun ComposeRule.verifyDriverThroughUi(model: String = "Toyota Hiace", reg: String = "BT 4521") {
    onNodeWithText("Vehicle make & model").performTextInput(model)
    onNodeWithText("Vehicle registration number").performTextInput(reg)
    onNodeWithText("Attach a photo of your registration document").performClick()
    onNodeWithText("Submit for verification").performClick()
    waitForIdle()
}

fun ComposeRule.pressBack() {
    runOnUiThread {
        activity.onBackPressedDispatcher.onBackPressed()
    }
    waitForIdle()
}

fun ComposeRule.pressBackUntilText(text: String, maxTries: Int = 5) {
    var tries = 0
    while (tries < maxTries && onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()) {
        pressBack()
        tries++
    }
}

fun ComposeRule.openFirst(text: String) {
    onAllNodesWithText(text).onFirst().performClick()
    waitForIdle()
}

fun ComposeRule.waitForText(text: String, timeoutMs: Long = 5000) {
    waitUntil(timeoutMs) {
        onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }
}
