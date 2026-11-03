package com.oyku.mezzemaster
import org.junit.Assert.*
import org.junit.Test
class RulesTest {
    @Test fun allTenRecipesHaveCompleteContent() {
        assertEquals(10, Menu.recipes.size)
        Menu.recipes.forEach { assertTrue(it.ingredients.isNotEmpty()); assertEquals(3,it.steps.size); assertTrue(it.nutrition.kcal > 0) }
    }
    @Test fun allOrdersCanEarnFullPoints() {
        val solutions = listOf(listOf(0,4,5), listOf(2,5), listOf(1,8), listOf(3,6,5), listOf(7,9,8))
        Orders.all.forEachIndexed { i, order -> assertEquals("Order $i",100, Rules.score(order,solutions[i],0,true).total) }
    }
    @Test fun repeatedRecipeCannotEarnRequestedPointsTwice() {
        val score = Rules.score(Orders.all[0],listOf(0,0),0,false)
        assertEquals(20,score.recipes)
    }
    @Test fun penaltyAndExpiredBonusAreApplied() {
        val score=Rules.score(Orders.all[0],listOf(0,4,5),2,false)
        assertEquals(80,score.total)
        assertEquals(0,Rules.score(Orders.all[0],emptyList(),30,true).total)
    }
    @Test fun wrongOrMissingIngredientsAreRejected() {
        val correct=Menu.recipes[0].ingredients.toSet()
        assertTrue(Rules.ingredientsMatch(0,correct))
        assertFalse(Rules.ingredientsMatch(0,correct - "Tahini"))
        assertFalse(Rules.ingredientsMatch(0,correct + "Beef"))
    }
    @Test fun portionNutritionAddsAndRemovesExactly() {
        assertEquals(Nutrition(470,17,37,28),Rules.nutrition(listOf(0,4,5)))
        assertEquals(Menu.recipes[0].nutrition,Rules.nutrition(listOf(0,5).filterIndexed { index,_ -> index!=1 }))
        assertEquals(Nutrition(),Rules.nutrition(emptyList()))
    }
}
