package com.oyku.mezzemaster

import java.io.Serializable

data class Nutrition(val kcal: Int = 0, val protein: Int = 0, val carbs: Int = 0, val fat: Int = 0) : Serializable {
    operator fun plus(other: Nutrition) = Nutrition(kcal + other.kcal, protein + other.protein, carbs + other.carbs, fat + other.fat)
}
data class Recipe(val name: String, val subtitle: String, val serving: String, val nutrition: Nutrition, val ingredients: List<String>, val steps: List<String>)
object Menu {
    val recipes = listOf(
        Recipe("Hummus", "Silky chickpea & tahini dip", "80 g", Nutrition(170,5,15,10),
            listOf("Chickpeas", "Tahini", "Lemon", "Garlic", "Olive oil"),
            listOf("Drain cooked chickpeas and reserve a little cooking water.", "Blend with tahini, lemon juice and garlic until smooth.", "Loosen with water, spoon into a bowl and finish with olive oil.")),
        Recipe("Baba Ghanoush", "Smoky roasted eggplant dip", "80 g", Nutrition(100,2,8,7),
            listOf("Eggplant", "Tahini", "Lemon", "Garlic", "Olive oil"),
            listOf("Roast eggplant until soft and the skin is charred.", "Cool, peel and drain the flesh.", "Mash with tahini, lemon and garlic. Drizzle with olive oil.")),
        Recipe("Tabbouleh", "Parsley, bulgur & lemon salad", "100 g", Nutrition(120,3,15,5),
            listOf("Parsley", "Tomato", "Bulgur", "Mint", "Lemon", "Olive oil"),
            listOf("Soak fine bulgur until tender, then drain well.", "Finely chop parsley, mint and tomato.", "Toss with bulgur, lemon juice and olive oil.")),
        Recipe("Fattoush", "Crisp garden salad with sumac", "120 g", Nutrition(150,3,18,7),
            listOf("Lettuce", "Tomato", "Cucumber", "Pita", "Sumac", "Olive oil"),
            listOf("Toast pita until crisp and break into pieces.", "Chop lettuce, tomato and cucumber.", "Toss vegetables with sumac and olive oil; add pita just before serving.")),
        Recipe("Falafel", "Golden chickpea & herb bites", "3 pieces", Nutrition(180,6,18,9),
            listOf("Chickpeas", "Parsley", "Onion", "Garlic", "Spices"),
            listOf("Soak dried chickpeas overnight, drain and dry thoroughly.", "Pulse with parsley, onion, garlic and spices into a coarse mixture.", "Rest the mixture, shape small balls and fry in hot oil until golden and cooked through.")),
        Recipe("Labneh", "Creamy strained yogurt", "70 g", Nutrition(120,6,4,9),
            listOf("Strained yogurt", "Olive oil", "Dried mint"),
            listOf("Strain plain yogurt in a cloth in the refrigerator until thick.", "Spoon the strained yogurt into a shallow bowl.", "Make a swirl and finish with olive oil and dried mint.")),
        Recipe("Muhammara", "Roasted pepper & walnut dip", "60 g", Nutrition(180,4,14,12),
            listOf("Red pepper", "Walnuts", "Breadcrumbs", "Pomegranate molasses", "Olive oil", "Spices"),
            listOf("Roast red peppers, then peel and remove the seeds.", "Pulse peppers with walnuts, breadcrumbs, pomegranate molasses and spices.", "Stir in olive oil and serve with a walnut garnish.")),
        Recipe("Warak Enab", "Vine leaves with rice & herbs", "4 rolls", Nutrition(160,3,25,5),
            listOf("Vine leaves", "Rice", "Parsley", "Tomato", "Lemon", "Olive oil"),
            listOf("Rinse preserved vine leaves and blanch if needed.", "Mix rinsed uncooked rice with chopped parsley, tomato and olive oil.", "Roll small portions in the leaves. Pack in a saucepan, cover with water and lemon juice, and simmer until the rice is tender.")),
        Recipe("Kibbeh", "Bulgur shells with spiced beef", "2 small pieces", Nutrition(240,14,19,12),
            listOf("Bulgur", "Beef", "Onion", "Pine nuts", "Spices"),
            listOf("Soak fine bulgur and squeeze dry. Blend with some lean beef and spices to form a paste.", "Cook the remaining beef with onion, pine nuts and spices.", "Shape filled oval shells, seal and fry until golden and cooked through.")),
        Recipe("Batata Harra", "Spicy potatoes with coriander", "100 g", Nutrition(170,3,26,6),
            listOf("Potato", "Garlic", "Coriander", "Chili", "Lemon", "Olive oil"),
            listOf("Dice potatoes, coat with a little olive oil and roast until crisp.", "Warm garlic, coriander and chili in the remaining oil.", "Toss with the potatoes and finish with lemon juice."))

    )
    val pantry = recipes.flatMap { it.ingredients }.distinct().sorted()
}
data class Order(val guest: String, val title: String, val requested: List<Int>, val minKcal: Int, val maxKcal: Int, val minProtein: Int)
object Orders {
    val all = listOf(
        Order("Maya", "A balanced welcome", listOf(0,4), 350,500,12),
        Order("Karim", "Fresh from the garden", listOf(2,5), 220,300,9),
        Order("Nour", "A smoky lunch", listOf(1,8), 330,400,16),
        Order("Rami", "Crunch & cream", listOf(3,6), 440,480,13),
        Order("Leila", "The sharing table", listOf(7,9), 550,600,20)
    )
}
data class ScoreBreakdown(val recipes: Int, val calories: Int, val protein: Int, val time: Int, val penalty: Int) : Serializable {
    val total get() = (recipes + calories + protein + time - penalty).coerceAtLeast(0)
}
data class GameState(
    val round: Int = 0,
    val total: Int = 0,
    val plate: List<Int> = emptyList(),
    val mistakes: Int = 0,
    val deadline: Long = 0,
    val result: ScoreBreakdown? = null
) : Serializable
object Rules {
    fun nutrition(plate: List<Int>) = plate.fold(Nutrition()) { sum, id -> sum + Menu.recipes[id].nutrition }
    fun ingredientsMatch(id: Int, selected: Set<String>) = selected == Menu.recipes[id].ingredients.toSet()
    fun score(order: Order, plate: List<Int>, mistakes: Int, inTime: Boolean): ScoreBreakdown {
        val n = nutrition(plate)
        return ScoreBreakdown(
            order.requested.count { it in plate } * 20,
            if (n.kcal in order.minKcal..order.maxKcal) 30 else 0,
            if (n.protein >= order.minProtein) 20 else 0,
            if (inTime && plate.isNotEmpty()) 10 else 0,
            mistakes * 5
        )
    }
}
