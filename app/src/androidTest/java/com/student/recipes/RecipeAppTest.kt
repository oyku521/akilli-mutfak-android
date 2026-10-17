package com.student.recipes

import android.os.SystemClock
import android.webkit.WebView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class RecipeAppTest {
    @Test fun bundledRecipesAreComplete() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val recipes = JSONArray(context.assets.open("recipes.json").bufferedReader().use { it.readText() })
        assertEquals(50, recipes.length())
        for (i in 0 until recipes.length()) {
            val recipe = recipes.getJSONObject(i)
            assertTrue(recipe.getString("name").isNotBlank())
            assertTrue(recipe.getJSONArray("ingredients").length() > 0)
            assertTrue(recipe.getInt("calories") > 0)
            recipe.getBoolean("vegan")
        }
    }

    @Test fun bundledPageSearchAndVeganFilterWork() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val deadline = SystemClock.elapsedRealtime() + 15000
            while (evaluate(scenario, "document.querySelectorAll('.recipe-card').length > 0") != "true") {
                assertTrue("Recipe interface did not load", SystemClock.elapsedRealtime() < deadline)
                SystemClock.sleep(150)
            }
            assertEquals("true", evaluate(scenario, "(() => { const input = document.getElementById('recipeSearch'); input.value = 'Menemen'; input.dispatchEvent(new Event('input')); return document.querySelectorAll('.recipe-card').length === 1 && document.querySelector('.recipe-card h3').textContent === 'Menemen'; })()"))
            assertEquals("true", evaluate(scenario, "(() => { const input = document.getElementById('vegan'); input.checked = true; input.dispatchEvent(new Event('change')); return document.querySelectorAll('.recipe-card').length === 0; })()"))
        }
    }

    private fun evaluate(scenario: ActivityScenario<MainActivity>, script: String): String {
        val latch = CountDownLatch(1)
        var result = ""
        scenario.onActivity { activity ->
            activity.findViewById<WebView>(R.id.recipeWebView).evaluateJavascript(script) {
                result = it
                latch.countDown()
            }
        }
        assertTrue("JavaScript response timed out", latch.await(5, TimeUnit.SECONDS))
        return result
    }
}
