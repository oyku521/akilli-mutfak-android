package com.oyku.mezzemaster

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import java.io.File

class GameFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private fun pause() { Thread.sleep(850) }
    private fun shot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = File(context.getExternalFilesDir(null),"evidence").apply { mkdirs() }
        check(UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(File(dir,name)))
    }
    private fun cook(id: Int) {
        rule.onNodeWithTag("kitchen").performScrollToNode(hasTestTag("cook-$id"))
        rule.onNodeWithTag("cook-$id").performClick()
        pause()
        Menu.recipes[id].ingredients.forEach { ingredient ->
            rule.onNodeWithTag("ingredient-$ingredient").performScrollTo().performClick()
        }
        rule.onNodeWithTag("prepare").performScrollTo().performClick()
        rule.waitForIdle()
        pause()
    }
    @Test fun playAllFiveOrdersAndSaveBestScore() {
        pause(); shot("01-home.png")
        rule.onNodeWithTag("start").performScrollTo().performClick()
        pause()
        rule.onNodeWithTag("kitchen").performScrollToNode(hasTestTag("read-0"))
        rule.onNodeWithTag("read-0").performClick()
        pause(); shot("02-recipe-nutrition.png")
        rule.onNodeWithTag("closeRecipe").performClick()
        // A wrong attempt must not add food or award points.
        rule.onNodeWithTag("cook-0").performClick()
        rule.onNodeWithTag("prepare").performScrollTo().performClick()
        rule.onNodeWithTag("feedback").assertTextContains("−5", substring=true)
        rule.onNodeWithTag("closeRecipe").performScrollTo().performClick()
        val solutions=listOf(listOf(0,4,5),listOf(2,5),listOf(1,8),listOf(3,6,5),listOf(7,9,8))
        solutions.forEachIndexed { round,ids ->
            ids.forEach { cook(it) }
            if(round==0) {
                rule.onNodeWithTag("plateCalories").assertTextEquals("470 / 350–500 kcal")
                rule.activityRule.scenario.recreate()
                rule.waitForIdle()
                rule.onNodeWithTag("plateCalories").assertTextEquals("470 / 350–500 kcal")
                rule.onNodeWithTag("kitchen").performScrollToIndex(0)
                rule.onNodeWithTag("restaurant").assertExists()
                shot("03-gameplay-balanced-plate.png")
            }
            rule.onNodeWithTag("serve").performClick()
            pause()
            rule.onNodeWithTag("roundScore").assertTextEquals(if(round==0) "+95" else "+100")
            if(round==0) shot("04-scoring.png")
            rule.onNodeWithTag("next").performScrollTo().performClick()
        }
        pause()
        rule.onNodeWithText("495 / 500").assertExists()
        shot("05-final-score.png")
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        rule.onNodeWithText("Best score: 495").assertExists()
        pause()
    }
}
