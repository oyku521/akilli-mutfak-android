package com.student.recipes

import android.os.SystemClock
import android.webkit.WebView
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal fun evaluate(scenario: ActivityScenario<MainActivity>, script: String): String {
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

internal fun waitForRecipes(scenario: ActivityScenario<MainActivity>, timeout: Long = 30000) {
    val deadline = SystemClock.elapsedRealtime() + timeout
    while (evaluate(scenario, "Boolean(document.querySelector('.recipe-card'))") != "true") {
        assertTrue("Recipe interface did not load", SystemClock.elapsedRealtime() < deadline)
        SystemClock.sleep(200)
    }
}

@RunWith(AndroidJUnit4::class)
class RecipeAppTest {
    @Test fun bundledRecipesAreComplete() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val recipes = JSONArray(context.assets.open("recipes.json").bufferedReader().use { it.readText() })
        val foods = JSONObject(context.assets.open("food-catalog.json").bufferedReader().use { it.readText() })
        assertEquals(80, recipes.length())
        for (i in 0 until recipes.length()) {
            val recipe = recipes.getJSONObject(i)
            assertTrue(recipe.getJSONObject("name").getString("en").isNotBlank())
            assertTrue(recipe.getJSONObject("name").getString("tr").isNotBlank())
            val ingredients = recipe.getJSONArray("ingredients")
            assertTrue(ingredients.length() > 0)
            for (j in 0 until ingredients.length()) {
                val ingredient = ingredients.getJSONObject(j)
                assertTrue(ingredient.getDouble("grams") > 0)
                assertTrue(foods.has(ingredient.getString("id")))
            }
        }
    }

    @Test fun bundledSearchDietaryFilterAndLanguageWork() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.showBundled() }
            waitForRecipes(scenario)
            assertEquals("true", evaluate(scenario, "(() => { const input = document.getElementById('recipeSearch'); input.value = 'Menemen'; input.dispatchEvent(new Event('input')); return document.querySelectorAll('.recipe-card').length === 1 && document.querySelector('.recipe-card h3').textContent === 'Menemen'; })()"))
            assertEquals("true", evaluate(scenario, "(() => { const input = document.getElementById('vegan'); input.checked = true; input.dispatchEvent(new Event('change')); return document.querySelectorAll('.recipe-card').length === 0; })()"))
            assertEquals("true", evaluate(scenario, "(() => { document.getElementById('resetFilters').click(); const gf = document.getElementById('glutenFree'); gf.checked = true; gf.dispatchEvent(new Event('change')); document.querySelector('input[value=tomato]').click(); return document.querySelectorAll('.recipe-card').length > 1; })()"))
            assertEquals("true", evaluate(scenario, "(() => { const language = document.getElementById('language'); language.value = 'tr'; language.dispatchEvent(new Event('change')); return document.documentElement.lang === 'tr' && document.querySelector('.view-recipe').textContent.includes('Tarifi gör'); })()"))
        }
    }

    @Test fun liveWebsiteLoadsInsideTheApp() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitForRecipes(scenario)
            scenario.onActivity { activity ->
                assertTrue("Live mode must show the actual GitHub Pages website", MainActivity.isLiveSite(android.net.Uri.parse(activity.findViewById<WebView>(R.id.recipeWebView).url.orEmpty())))
            }
            assertEquals("true", evaluate(scenario, "navigator.userAgent.includes('SmartKitchenAndroid/') && document.getElementById('installApp').hidden && recipes.length >= 80"))
        }
    }

    @Test fun internalNavigationIsRestrictedToOwnedHttpsLocations() {
        fun live(url: String) = MainActivity.isLiveSite(android.net.Uri.parse(url))
        assertTrue(live(MainActivity.LIVE_URL))
        assertFalse(live("http://oyku521.github.io/gastronomy-task3-yaz./"))
        assertFalse(live("https://oyku521.github.io/some-other-project/"))
        assertFalse(live("https://oyku521.github.io.evil.example/gastronomy-task3-yaz./"))
        assertFalse(live("https://other.github.io/gastronomy-task3-yaz./"))
    }
}

/** Run only on the CI emulator after its Wi-Fi and mobile data have been disabled. */
@RunWith(AndroidJUnit4::class)
class OfflineFallbackTest {
    @Test fun unreachableLiveWebsiteAutomaticallyFallsBackToBundledRecipes() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitForRecipes(scenario, 40000)
            scenario.onActivity { activity ->
                assertTrue(MainActivity.isBundledSite(android.net.Uri.parse(activity.findViewById<WebView>(R.id.recipeWebView).url.orEmpty())))
            }
            assertEquals("true", evaluate(scenario, "recipes.length === 80 && document.querySelectorAll('.recipe-card').length > 0"))
        }
    }
}
