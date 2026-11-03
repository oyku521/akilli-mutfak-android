@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.oyku.mezzemaster

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

val Cream = Color(0xFFF8F5ED)
val Olive = Color(0xFF425B40)
val Wine = Color(0xFF822F42)
val Ink = Color(0xFF27352C)
val Muted = Color(0xFF6E756A)
val Gold = Color(0xFFC08C46)
val Paper = Color(0xFFFFFDF8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Olive, secondary = Wine, background = Cream, surface = Paper, onSurface = Ink, onBackground = Ink)) {
                MezzeApp()
            }
        }
    }
}
@Composable fun MezzeApp() {
    val prefs = LocalContext.current.getSharedPreferences("mezze", 0)
    var best by remember { mutableIntStateOf(prefs.getInt("best", 0)) }
    var screen by rememberSaveable { mutableStateOf("home") }
    var game by rememberSaveable { mutableStateOf(GameState()) }
    var recipeId by rememberSaveable { mutableIntStateOf(-1) }
    var cooking by rememberSaveable { mutableStateOf(false) }
    var showRules by rememberSaveable { mutableStateOf(false) }
    var showExit by remember { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    var feedback by rememberSaveable { mutableStateOf("") }
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(screen, game.deadline) {
        while (screen == "game") { now = SystemClock.elapsedRealtime(); delay(250) }
    }
    fun start() {
        game = GameState(deadline = SystemClock.elapsedRealtime() + 90_000)
        screen = "game"
    }
    fun openRecipe(id: Int, cook: Boolean) {
        recipeId = id; cooking = cook; selected = arrayListOf(); feedback = ""
    }
    BackHandler(screen != "home" || recipeId >= 0) {
        if (recipeId >= 0) recipeId = -1
        else if (screen == "game") showExit = true
        else screen = "home"
    }
    Surface(Modifier.fillMaxSize(), color = Cream) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("✦", color = Wine, fontSize = 27.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("MEZZE MASTER", letterSpacing = 2.sp, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("A little taste of Lebanon", color = Muted, fontSize = 11.sp)
                }
                TextButton(onClick = { showRules = true }) { Text("How to play") }
            }
            Motif(Modifier.fillMaxWidth().height(8.dp))
            when (screen) {
                "home" -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(22.dp)) {
                    Text("THE LEBANESE TABLE", color = Wine, letterSpacing = 2.sp, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Title("Small plates.\nBig possibilities.", 36)
                    Text("Cook with care. Balance your plate.\nBecome a mezze master.", color = Muted, modifier = Modifier.padding(top = 10.dp))
                    Box(Modifier.fillMaxWidth().height(235.dp).padding(vertical = 10.dp).clip(RoundedCornerShape(26.dp)).background(Color(0xFFE9E6D7))) {
                        TableArt(Modifier.fillMaxSize())
                        Surface(Modifier.align(Alignment.BottomStart).padding(12.dp), color = Paper, shape = RoundedCornerShape(20.dp)) {
                            Text("10 RECIPES  ·  5 GUESTS", Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = Olive, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SmallStat("BEST SCORE", "$best / 500", Modifier.weight(1f))
                        SmallStat("YOUR KITCHEN", "100% offline", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = { start() }, modifier = Modifier.fillMaxWidth().height(54.dp).testTag("start"), shape = RoundedCornerShape(16.dp)) { Text("Open the kitchen  →", fontSize = 16.sp) }
                    OutlinedButton(onClick = { screen = "recipes" }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(50.dp), shape = RoundedCornerShape(16.dp)) { Text("Explore 10 recipes") }
                    Text("A cooking game inspired by Lebanese hospitality.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 15.dp))
                }
                "recipes" -> Column(Modifier.weight(1f)) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Title("The recipe book", 28); Text("10 mezze · recipes & nutrition", color = Muted, fontSize = 12.sp) }
                        TextButton(onClick = { screen = "home" }) { Text("Home") }
                    }
                    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(Menu.recipes) { id, r -> RecipeCard(id, r, false, 0, { openRecipe(id, false) }, { }, showCook = false) }
                        item { NutritionNote() }
                    }
                }
                "game" -> {
                    val order = Orders.all[game.round]
                    val remaining = ((game.deadline - now).coerceAtLeast(0) / 1000).toInt()
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("ORDER ${game.round + 1} / 5", color = Wine, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${game.total} pts", fontWeight = FontWeight.Bold, modifier = Modifier.testTag("total"))
                        Surface(color = if (remaining > 15) Color(0xFFE6EBDD) else Color(0xFFF6E1DB), shape = RoundedCornerShape(20.dp)) {
                            Text(if (remaining > 0) "${remaining}s bonus" else "Bonus expired", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Olive, fontSize = 12.sp)
                        }
                    }
                    LazyColumn(Modifier.weight(1f).testTag("kitchen"), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Panel {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ChefArt(Modifier.size(62.dp), game.round)
                                    Spacer(Modifier.width(12.dp))
                                    Column { Text("${order.guest}'s table", color = Wine, fontWeight = FontWeight.Bold); Title(order.title, 22) }
                                }
                                Spacer(Modifier.height(10.dp))
                                Text("“I'd love ${order.requested.joinToString(" and ") { Menu.recipes[it].name }}. Add other mezze if needed!”", fontSize = 14.sp)
                                Text("${order.minKcal}–${order.maxKcal} kcal  ·  At least ${order.minProtein} g protein", color = Olive, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
                            }
                        }
                        item { Row(verticalAlignment = Alignment.CenterVertically) { Title("Choose your mezze", 23); Spacer(Modifier.weight(1f)); Text("10 recipes", color = Muted, fontSize = 12.sp) } }
                        itemsIndexed(Menu.recipes) { id, r ->
                            RecipeCard(id, r, id in order.requested, game.plate.count { it == id }, { openRecipe(id, false) }, { openRecipe(id, true) })
                        }
                        item { NutritionNote() }
                    }
                    PlateBar(game, order, onRemove = { index -> game = game.copy(plate = game.plate.filterIndexed { i, _ -> i != index }) }, onServe = {
                        val result = Rules.score(order, game.plate, game.mistakes, SystemClock.elapsedRealtime() <= game.deadline)
                        game = game.copy(total = game.total + result.total, result = result)
                        if (game.total > best) { best = game.total; prefs.edit().putInt("best", best).apply() }
                        screen = "result"
                    })
                }
                "result" -> {
                    val r = game.result!!
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        ChefArt(Modifier.size(110.dp), game.round)
                        Text(if (r.total >= 90) "A TABLE TO REMEMBER" else "EVERY PLATE IS PRACTICE", color = Wine, fontSize = 11.sp, letterSpacing = 1.sp, modifier = Modifier.padding(top = 14.dp))
                        Title(if (r.total >= 90) "Beautifully served!" else "Keep cooking!", 32)
                        Text("+${r.total}", fontSize = 64.sp, fontFamily = FontFamily.Serif, color = Olive, modifier = Modifier.testTag("roundScore"))
                        Text("POINTS THIS ORDER", color = Muted, letterSpacing = 2.sp, fontSize = 11.sp)
                        Spacer(Modifier.height(20.dp))
                        Panel {
                            ScoreLine("Requested recipes", r.recipes, 40)
                            ScoreLine("Calorie target", r.calories, 30)
                            ScoreLine("Protein target", r.protein, 20)
                            ScoreLine("90-second bonus", r.time, 10)
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                            Text("Ingredient mistakes: ${game.mistakes}  ·  −${r.penalty} pts", color = Wine, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(14.dp))
                        NutritionStrip(Rules.nutrition(game.plate))
                        Spacer(Modifier.height(20.dp))
                        Text("Total score: ${game.total} / 500", fontWeight = FontWeight.Bold)
                        Button(onClick = {
                            if (game.round == 4) screen = "finish"
                            else { game = GameState(round = game.round + 1, total = game.total, deadline = SystemClock.elapsedRealtime() + 90_000); screen = "game" }
                        }, modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(52.dp).testTag("next")) { Text(if (game.round == 4) "See final results" else "Next guest  →") }
                    }
                }
                "finish" -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    TableArt(Modifier.fillMaxWidth().height(220.dp))
                    Text("THE KITCHEN IS CLOSED", color = Wine, letterSpacing = 2.sp, fontSize = 12.sp)
                    Title("Shukran, chef!", 36)
                    Text("Five guests. A table full of stories.", color = Muted, modifier = Modifier.padding(vertical = 12.dp))
                    Text("${game.total} / 500", fontSize = 48.sp, color = Olive, fontFamily = FontFamily.Serif)
                    Text(if (game.total >= 450) "MASTER OF THE MEZZE" else if (game.total >= 300) "RISING CHEF" else "APPRENTICE CHEF", color = Wine, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
                    Text("Best score: $best", fontWeight = FontWeight.Bold)
                    Button(onClick = { start() }, Modifier.fillMaxWidth().padding(top = 24.dp).height(52.dp)) { Text("Play again") }
                    TextButton(onClick = { screen = "home" }) { Text("Back to home") }
                }
            }
        }
    }
    if (recipeId >= 0) {
        val id = recipeId
        val recipe = Menu.recipes[id]
        val options = remember(id) { (recipe.ingredients + Menu.pantry.filter { it !in recipe.ingredients }.shuffled(kotlin.random.Random(id)).take(3)).sorted() }
        Dialog(onDismissRequest = { recipeId = -1 }) {
            Surface(shape = RoundedCornerShape(24.dp), color = Cream) {
                Column(Modifier.fillMaxWidth().heightIn(max = 640.dp).verticalScroll(rememberScrollState()).padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (cooking) "CHEF'S CHALLENGE" else "RECIPE BOOK", color = Wine, fontSize = 11.sp, letterSpacing = 2.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { recipeId = -1 }, modifier = Modifier.testTag("closeRecipe")) { Text("Close") }
                    }
                    FoodArt(id, Modifier.fillMaxWidth().height(125.dp))
                    Title(recipe.name, 29)
                    Text(recipe.subtitle, color = Muted, fontSize = 13.sp)
                    Text("One serving: ${recipe.serving}", color = Olive, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                    NutritionStrip(recipe.nutrition)
                    if (cooking) {
                        Text("Select every recipe ingredient", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                        Text("Extra or missing ingredients cost 5 points per attempt.", fontSize = 12.sp, color = Muted)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            options.forEach { ingredient ->
                                FilterChip(selected = ingredient in selected, onClick = { selected = ArrayList(selected.toList().let { if (ingredient in it) it - ingredient else it + ingredient }); feedback = "" },
                                    label = { Text(ingredient, fontSize = 12.sp) }, modifier = Modifier.testTag("ingredient-$ingredient"))
                            }
                        }
                        if (feedback.isNotEmpty()) Text(feedback, color = Wine, fontSize = 13.sp, modifier = Modifier.testTag("feedback"))
                        Button(onClick = {
                            if (!Rules.ingredientsMatch(id, selected.toSet())) {
                                game = game.copy(mistakes = game.mistakes + 1); feedback = "Check your ingredients. −5 points. Try again!"
                            } else if (game.plate.size >= 6) feedback = "Your plate is full. Remove a serving first."
                            else { game = game.copy(plate = game.plate + id); recipeId = -1 }
                        }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("prepare")) { Text("Prepare & add to plate") }
                        Text("Need a hint? Read the recipe steps below.", fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 12.dp))
                    } else {
                        Text("Ingredients", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                        Text(recipe.ingredients.joinToString(" · "), fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
                    }
                    Text("Preparation", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                    recipe.steps.forEachIndexed { i, step -> Text("${i + 1}. $step", fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp)) }
                    Spacer(Modifier.height(14.dp))
                    NutritionNote()
                }
            }
        }
    }
    if (showRules) AlertDialog(onDismissRequest = { showRules = false }, title = { Title("Welcome, chef.", 28) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Text("Serve five guests. Each asks for two mezze and a nutrition target.\n\n1. Read a recipe, then tap Cook.\n2. Select all correct ingredients and prepare one serving.\n3. Add extra servings to balance calories and protein. Tap a plate chip to remove it.\n4. Serve your plate to earn points.\n\nSCORING PER ORDER\nRequested recipes: 20 each (max 40)\nCalories in range: 30\nProtein target reached: 20\nServe within 90 seconds: 10\nWrong ingredient attempt: −5\n\nMaximum: 100 per order, 500 per game. Timer expiry removes only the time bonus. The timer continues while the app is in the background.\n\nNutrition values are approximate per serving. No sign-in or internet is needed.", fontSize = 14.sp)
        }
    }, confirmButton = { TextButton(onClick = { showRules = false }) { Text("Let's cook") } })
    if (showExit) AlertDialog(onDismissRequest = { showExit = false }, title = { Text("Leave this kitchen?") }, text = { Text("Your current game will end. Your best score is saved.") }, confirmButton = { TextButton(onClick = { showExit = false; screen = "home" }) { Text("Leave game") } }, dismissButton = { TextButton(onClick = { showExit = false }) { Text("Keep cooking") } })
}

@Composable fun Title(text: String, size: Int) { Text(text, fontFamily = FontFamily.Serif, fontSize = size.sp, lineHeight = (size + 3).sp, color = Ink) }
@Composable fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = Paper, modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE5E3D8), RoundedCornerShape(20.dp))) {
        Column(Modifier.padding(16.dp), content = content)
    }
}
@Composable fun SmallStat(label: String, value: String, modifier: Modifier) {
    Surface(modifier, color = Paper, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(13.dp)) { Text(label, color = Muted, fontSize = 10.sp, letterSpacing = 1.sp); Text(value, color = Olive, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 5.dp)) }
    }
}
@Composable fun NutritionStrip(n: Nutrition) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEEEE3)).padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf("${n.kcal}" to "kcal", "${n.protein} g" to "protein", "${n.carbs} g" to "carbs", "${n.fat} g" to "fat").forEach { (value,label) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Olive); Text(label, fontSize = 10.sp, color = Muted) }
        }
    }
}
@Composable fun NutritionNote() { Text("Estimated nutrition per serving. Values vary with ingredients, portion size and preparation.", fontSize = 11.sp, lineHeight = 15.sp, color = Muted) }
@Composable fun RecipeCard(id: Int, r: Recipe, requested: Boolean, count: Int, onRead: () -> Unit, onCook: () -> Unit, showCook: Boolean = true) {
    Surface(color = Paper, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().border(1.dp, if (requested) Color(0xFFB6C6A5) else Color(0xFFE5E3D8), RoundedCornerShape(20.dp)).testTag("card-$id")) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FoodArt(id, Modifier.size(83.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF1EFE4)))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    if (requested) Text("GUEST'S CHOICE", color = Wine, fontSize = 9.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    Title(r.name, 22)
                    Text(r.subtitle, color = Muted, fontSize = 11.sp, lineHeight = 14.sp)
                    Text("${r.nutrition.kcal} kcal · ${r.nutrition.protein} g protein", color = Olive, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
                    Text("Per serving · ${r.serving}", color = Muted, fontSize = 10.sp)
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                if (count > 0) Text("$count on plate", color = Olive, fontSize = 12.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = onRead, modifier = Modifier.testTag("read-$id")) { Text("Recipe") }
                if (showCook) Button(onClick = onCook, contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp), modifier = Modifier.testTag("cook-$id")) { Text("Cook +") }
            }
        }
    }
}
@Composable fun PlateBar(game: GameState, order: Order, onRemove: (Int) -> Unit, onServe: () -> Unit) {
    val n = Rules.nutrition(game.plate)
    Surface(color = Paper, shadowElevation = 12.dp) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Your plate", fontFamily = FontFamily.Serif, fontSize = 20.sp, modifier = Modifier.weight(1f))
                Text("${game.plate.size}/6 servings", color = Muted, fontSize = 11.sp)
            }
            if (game.plate.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    game.plate.forEachIndexed { index, id ->
                        Surface(color = Color(0xFFE9EDDF), shape = RoundedCornerShape(15.dp), modifier = Modifier.padding(top = 4.dp).clickable { onRemove(index) }.testTag("remove-$index")) {
                            Text("${Menu.recipes[id].name} ×", fontSize = 10.sp, color = Olive, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                        }
                    }
                }
            } else Text("Cook a mezze to begin your plate.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 6.dp))
            Row(Modifier.fillMaxWidth().padding(top = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${n.kcal} / ${order.minKcal}–${order.maxKcal} kcal", color = if (n.kcal in order.minKcal..order.maxKcal) Olive else Wine, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.testTag("plateCalories"))
                Text("Protein ${n.protein}/${order.minProtein} g", color = Olive, fontSize = 12.sp)
            }
            LinearProgressIndicator(progress = { (n.kcal.toFloat() / order.maxKcal).coerceIn(0f,1f) }, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), color = if (n.kcal > order.maxKcal) Wine else Olive, trackColor = Color(0xFFE6E8DB))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Carbs ${n.carbs} g  ·  Fat ${n.fat} g", color = Muted, fontSize = 11.sp)
                    if (game.mistakes > 0) Text("Mistake penalty: −${game.mistakes * 5}", color = Wine, fontSize = 10.sp)
                }
                Button(onClick = onServe, enabled = game.plate.isNotEmpty(), modifier = Modifier.testTag("serve")) { Text("Serve plate →") }
            }
        }
    }
}
@Composable fun ScoreLine(label: String, points: Int, max: Int) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) { Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp); Text("$points / $max", fontWeight = FontWeight.Bold, color = if (points == max) Olive else Wine, fontSize = 14.sp) }
}
