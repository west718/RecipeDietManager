package com.west.smartfridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.west.smartfridge.ui.theme.SmartFridgeTheme
import kotlinx.coroutines.launch

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "首页", Icons.Filled.Home)
    object Inventory : BottomNavItem("inventory", "清单", Icons.Filled.List)
    object Add : BottomNavItem("add", "添加", Icons.Filled.Add)
    object Recipes : BottomNavItem("recipes", "菜谱", Icons.Filled.MenuBook)
    object Profile : BottomNavItem("profile", "我的", Icons.Filled.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            SmartFridgeTheme(darkTheme = isDarkMode) {
                MainScreen(isDarkMode = isDarkMode, onDarkModeChange = { isDarkMode = it })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(isDarkMode: Boolean, onDarkModeChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    val dbHelper = remember { IngredientDbHelper(context) }
    val coroutineScope = rememberCoroutineScope()

    val bgColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textDark = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF2C3E50)
    val textGray = if (isDarkMode) Color(0xFF9E9E9E) else Color(0xFF7F8C8D)

    var selectedTab by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Home) }
    var showAddScreen by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var selectedIngredient by remember { mutableStateOf<Ingredient?>(null) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var showHistoryScreen by remember { mutableStateOf(false) }
    var weatherInfo by remember { mutableStateOf("正在获取...") }

    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(refreshTrigger) {
        isLoading = true
        kotlinx.coroutines.delay(500)
        isLoading = false
    }

    LaunchedEffect(Unit) {
        if (dbHelper.getAllIngredients().isEmpty()) {
            val today = System.currentTimeMillis()
            dbHelper.addIngredient(Ingredient(name = "西红柿", category = "蔬菜", quantity = 3, expirationDate = today + 24 * 60 * 60 * 1000L, imageUrl = "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=400&q=80"))
            dbHelper.addIngredient(Ingredient(name = "鸡蛋", category = "生鲜", quantity = 10, expirationDate = today + 2 * 24 * 60 * 60 * 1000L, imageUrl = "https://images.unsplash.com/photo-1587486913049-53fc88980cfc?w=400&q=80"))
            dbHelper.addIngredient(Ingredient(name = "牛奶", category = "饮品", quantity = 2, expirationDate = today + 30 * 24 * 60 * 60 * 1000L, imageUrl = "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400&q=80"))
            refreshTrigger++
        }
        weatherInfo = dbHelper.getWeatherInfo()
    }

    val ingredientList = remember(refreshTrigger) { dbHelper.getAllIngredients() }

    if (showHistoryScreen) {
        HistoryScreen(dbHelper = dbHelper, onBack = { showHistoryScreen = false })
    } else if (selectedIngredient != null) {
        IngredientDetailScreen(
            ingredient = selectedIngredient!!,
            onEat = { dbHelper.deleteAndRecord(selectedIngredient!!, "EATEN"); selectedIngredient = null; refreshTrigger++ },
            onDiscard = { dbHelper.deleteAndRecord(selectedIngredient!!, "DISCARDED"); selectedIngredient = null; refreshTrigger++ },
            onDelay = { val newDate = selectedIngredient!!.expirationDate + 24 * 60 * 60 * 1000L; dbHelper.updateIngredientExpiration(selectedIngredient!!.id, newDate); selectedIngredient = null; refreshTrigger++ },
            onBack = { selectedIngredient = null }
        )
    } else if (selectedRecipe != null) {
        RecipeDetailScreen(recipe = selectedRecipe!!, onBack = { selectedRecipe = null })
    } else if (showAddScreen) {
        AddIngredientScreen(
            onSave = { ingredient ->
                dbHelper.addIngredient(ingredient)
                showAddScreen = false
                selectedTab = BottomNavItem.Inventory
                refreshTrigger++
                coroutineScope.launch {
                    val realUrl = dbHelper.getSmartImageUrl(ingredient.name)
                    val all = dbHelper.getAllIngredients()
                    val newItem = all.lastOrNull { it.name == ingredient.name }
                    if (newItem != null) {
                        dbHelper.updateIngredientImage(newItem.id, realUrl)
                        kotlinx.coroutines.delay(800)
                        refreshTrigger++
                    }
                }
            },
            onBack = { showAddScreen = false }
        )
    } else {
        Scaffold(
            topBar = {
                if (selectedTab != BottomNavItem.Home && selectedTab != BottomNavItem.Inventory && selectedTab != BottomNavItem.Recipes && selectedTab != BottomNavItem.Profile) {
                    TopAppBar(title = { Text(selectedTab.label) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor))
                }
            },
            containerColor = bgColor,
            bottomBar = {
                NavigationBar(containerColor = cardColor, tonalElevation = 0.dp) {
                    val tabs = listOf(BottomNavItem.Home, BottomNavItem.Inventory, BottomNavItem.Add, BottomNavItem.Recipes, BottomNavItem.Profile)
                    tabs.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                if (item == BottomNavItem.Add) {
                                    Box(modifier = Modifier.size(48.dp).background(Color(0xFF4A90E2), RoundedCornerShape(50)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Add, contentDescription = "添加", tint = Color.White) }
                                } else {
                                    Icon(item.icon, contentDescription = item.label, tint = if (selectedTab == item) Color(0xFF4A90E2) else textGray)
                                }
                            },
                            label = { if (item != BottomNavItem.Add) Text(item.label, fontSize = 10.sp, color = if (selectedTab == item) Color(0xFF4A90E2) else textGray) },
                            selected = selectedTab == item,
                            onClick = { if (item == BottomNavItem.Add) showAddScreen = true else selectedTab = item },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF4A90E2))
                } else {
                    when (selectedTab) {
                        BottomNavItem.Home -> HomePage(ingredientList, weatherInfo, bgColor, cardColor, textDark, textGray, onViewInventory = { selectedTab = BottomNavItem.Inventory }, onItemClick = { selectedIngredient = it })
                        BottomNavItem.Inventory -> InventoryPage(ingredientList, bgColor, cardColor, textDark, textGray, onItemClick = { selectedIngredient = it })
                        BottomNavItem.Recipes -> RecipesPage(ingredientList, bgColor, cardColor, textDark, textGray, onRecipeClick = { selectedRecipe = it })
                        BottomNavItem.Profile -> ProfilePage(dbHelper, isDarkMode, onDarkModeChange, bgColor, cardColor, textDark, textGray, onViewHistory = { showHistoryScreen = true })
                        else -> {}
                    }
                }
            }
        }
    }
}

// =========================================================================
// 页面 1：首页看板
// =========================================================================
@Composable
fun HomePage(ingredientList: List<Ingredient>, weatherInfo: String, bgColor: Color, cardColor: Color, textDark: Color, textGray: Color, onViewInventory: () -> Unit, onItemClick: (Ingredient) -> Unit) {
    val now = System.currentTimeMillis()
    val threeDaysLater = now + 3 * 24 * 60 * 60 * 1000L
    val expiringSoon = ingredientList.filter { it.expirationDate in now..threeDaysLater }
    val topIngredients = ingredientList.groupBy { it.name }.map { (name, items) -> Triple(name, items.size, items.first().imageUrl) }.sortedByDescending { it.second }.take(3)

    LazyColumn(modifier = Modifier.fillMaxSize().background(bgColor), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(colors = listOf(Color(0xFF2C3E50), Color(0xFF1A252F))), shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).padding(horizontal = 24.dp, vertical = 48.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("4月29日 周三 · 晴 22℃", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("早上好 👋", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Surface(color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
                            Text("🌤️ $weatherInfo", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                        }
                    }
                }
            }
        }
        item {
            if (expiringSoon.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).offset(y = (-28).dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                    Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⚠️", fontSize = 20.sp); Spacer(modifier = Modifier.width(12.dp))
                        Text("${expiringSoon.size} 种食材即将过期，请尽快食用！", color = Color(0xFFE65100), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("冰箱数据看板", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textDark)
                    Icon(imageVector = Icons.Filled.BarChart, contentDescription = "查看全部", tint = textGray, modifier = Modifier.size(22.dp).clickable { onViewInventory() })
                }
                Spacer(modifier = Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(0.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("${ingredientList.size}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF4A90E2)); Spacer(modifier = Modifier.height(4.dp)); Text("总种类", fontSize = 12.sp, color = textGray) }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFF0F0F0)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { val q = ingredientList.sumOf { it.quantity }; Text("$q", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF4A90E2)); Spacer(modifier = Modifier.height(4.dp)); Text("总数量", fontSize = 12.sp, color = textGray) }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFF0F0F0)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { val e = ingredientList.count { it.expirationDate < now }; Text("$e", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFFE53935)); Spacer(modifier = Modifier.height(4.dp)); Text("已过期", fontSize = 12.sp, color = Color(0xFFE53935)) }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFF0F0F0)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("${expiringSoon.size}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFFF57C00)); Spacer(modifier = Modifier.height(4.dp)); Text("快过期", fontSize = 12.sp, color = Color(0xFFF57C00)) }
                    }
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text("常买食材 TOP 3", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textDark)
                Spacer(modifier = Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        topIngredients.forEachIndexed { index, (name, count, imageUrl) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${index + 1}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = textGray.copy(alpha = 0.5f), modifier = Modifier.width(24.dp))
                                Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.size(32.dp)) { AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).build(), contentDescription = name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textDark)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val maxCount = topIngredients.firstOrNull()?.second ?: 1
                                    val progress = count.toFloat() / maxCount.toFloat()
                                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(bgColor, RoundedCornerShape(3.dp))) {
                                        Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(Color(0xFF4A90E2), RoundedCornerShape(3.dp)))
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("${count}次", fontSize = 12.sp, color = textGray)
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("即将过期", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textDark)
                Text("查看全部", fontSize = 12.sp, color = Color(0xFF4A90E2), modifier = Modifier.clickable { onViewInventory() })
            }
        }
        items(expiringSoon) { ingredient ->
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp).background(cardColor, RoundedCornerShape(16.dp)).clickable { onItemClick(ingredient) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.size(48.dp)) { AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(ingredient.imageUrl).crossfade(true).build(), contentDescription = ingredient.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) { Text(ingredient.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textDark); Text("数量：${ingredient.quantity}", fontSize = 12.sp, color = textGray) }
                Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) { Text("即将过期", color = Color(0xFFE53935), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
            }
        }
    }
}

// =========================================================================
// 页面 2：食材清单
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryPage(ingredientList: List<Ingredient>, bgColor: Color, cardColor: Color, textDark: Color, textGray: Color, onItemClick: (Ingredient) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("全部") }
    var isGridView by remember { mutableStateOf(false) }
    var isSortByExpiry by remember { mutableStateOf(true) }

    val dynamicCategories = listOf("全部") + ingredientList.map { it.category }.distinct()
    val filteredList = ingredientList.filter { val mC = selectedCategory == "全部" || it.category == selectedCategory; val mS = it.name.contains(searchQuery, true); mC && mS }
    val sortedList = if (isSortByExpiry) filteredList.sortedBy { it.expirationDate } else filteredList.sortedByDescending { it.id }
    val now = System.currentTimeMillis()

    Column(modifier = Modifier.fillMaxSize().background(bgColor).padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("食材清单", fontSize = 24.sp, fontWeight = FontWeight.Black, color = textDark)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { isGridView = !isGridView }, modifier = Modifier.background(cardColor, RoundedCornerShape(10.dp)).size(40.dp)) { Icon(if (isGridView) Icons.Filled.List else Icons.Filled.GridView, contentDescription = "切换视图", tint = Color(0xFF4A90E2)) }
                IconButton(onClick = { isSortByExpiry = !isSortByExpiry }, modifier = Modifier.background(cardColor, RoundedCornerShape(10.dp)).size(40.dp)) { Icon(Icons.Filled.Sort, contentDescription = "切换排序", tint = if (isSortByExpiry) Color(0xFFF57C00) else textGray) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = { Text("搜索食材...", color = textGray, fontSize = 14.sp) }, leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "搜索", tint = textGray) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = cardColor, unfocusedContainerColor = cardColor, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent), singleLine = true)
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            dynamicCategories.forEach { cat -> val isSelected = selectedCategory == cat; Surface(color = if (isSelected) Color(0xFF4A90E2) else cardColor, shape = RoundedCornerShape(20.dp), modifier = Modifier.clickable { selectedCategory = cat }) { Text(cat, color = if (isSelected) Color.White else textGray, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) } }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("共${sortedList.size}种食材（${if(isSortByExpiry) "按保质期" else "按最新添加"}）", fontSize = 12.sp, color = textGray)
        Spacer(modifier = Modifier.height(12.dp))

        if (isGridView) {
            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sortedList.size) { index ->
                    val i = sortedList[index]; val dL = (i.expirationDate - now) / (24 * 60 * 60 * 1000L); val isE = dL <= 3
                    Card(modifier = Modifier.fillMaxWidth().clickable { onItemClick(i) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(0.dp)) {
                        Column {
                            AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(i.imageUrl).crossfade(true).build(), contentDescription = i.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(110.dp).background(Color(0xFFF0F0F0)))
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(i.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textDark); Spacer(modifier = Modifier.height(4.dp))
                                Text("数量：${i.quantity}", fontSize = 12.sp, color = textGray); Spacer(modifier = Modifier.height(6.dp))
                                Text(if (dL >= 0) "剩${dL}天" else "已过期", color = if (isE) Color(0xFFE53935) else Color(0xFFF57C00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sortedList) { i ->
                    val dL = (i.expirationDate - now) / (24 * 60 * 60 * 1000L); val isE = dL <= 3
                    Row(modifier = Modifier.fillMaxWidth().background(cardColor, RoundedCornerShape(16.dp)).clickable { onItemClick(i) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)) { AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(i.imageUrl).crossfade(true).build(), contentDescription = i.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Text(i.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textDark); Spacer(modifier = Modifier.width(8.dp)); Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(4.dp)) { Text(i.category, color = Color(0xFF4A90E2), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) } }
                            Spacer(modifier = Modifier.height(4.dp)); Text("数量：${i.quantity}", fontSize = 12.sp, color = textGray)
                        }
                        Text(if (dL >= 0) "${dL}天" else "已过期", color = if (isE) Color(0xFFE53935) else Color(0xFFF57C00), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =========================================================================
// 页面 3：菜谱推荐（双维度筛选 + 缺料推荐）
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipesPage(ingredientList: List<Ingredient>, bgColor: Color, cardColor: Color, textDark: Color, textGray: Color, onRecipeClick: (Recipe) -> Unit) {
    val allRecommended = RecipeRecommender.recommendRecipes(ingredientList)

    // 状态：当前的匹配度筛选 和 食材分类筛选
    var selectedMatchType by remember { mutableStateOf("全部") }
    var selectedFoodType by remember { mutableStateOf("全部") }

    // 过滤逻辑
    val filteredRecipes = allRecommended.filter { (recipe, score) ->
        // 1. 匹配度过滤（包含缺料）
        val matchCondition = when (selectedMatchType) {
            "极配" -> score >= 80
            "推荐" -> score in 50..79
            "缺料" -> score in 1..49
            else -> true
        }

        // 2. 食材类别过滤
        val foodCondition = when (selectedFoodType) {
            "肉食" -> recipe.name.contains("肉") || recipe.name.contains("排骨") || recipe.name.contains("鸡") || recipe.name.contains("鱼") || recipe.name.contains("虾")
            "素食" -> !recipe.name.contains("肉") && !recipe.name.contains("排骨") && !recipe.name.contains("鸡") && !recipe.name.contains("鱼") && !recipe.name.contains("虾")
            "汤羹" -> recipe.name.contains("汤") || recipe.name.contains("羹") || recipe.name.contains("粥")
            else -> true
        }

        matchCondition && foodCondition
    }

    Column(modifier = Modifier.fillMaxSize().background(bgColor)) {
        // 头部标题
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) {
            Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                Text("✨ 今日智能推荐", color = Color(0xFF1565C0), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text("为你精选的", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = textDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text("菜谱", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1565C0))
            }
        }

        // ================= 筛选栏 =================
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            // 第一排：匹配度筛选
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("全部", "极配", "推荐", "缺料").forEach { type ->
                    val isSelected = selectedMatchType == type
                    Surface(
                        color = if (isSelected) Color(0xFF4A90E2) else cardColor,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { selectedMatchType = type }
                    ) {
                        Text(type, color = if (isSelected) Color.White else textGray, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 第二排：食材类别筛选
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("全部", "肉食", "素食", "汤羹").forEach { type ->
                    val isSelected = selectedFoodType == type
                    Surface(
                        color = if (isSelected) Color(0xFF5BB974) else cardColor,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { selectedFoodType = type }
                    ) {
                        Text(type, color = if (isSelected) Color.White else textGray, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= 列表区 =================
        if (filteredRecipes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🍽️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("没有找到符合条件的菜谱", color = textGray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredRecipes) { (recipe, score) ->
                    val scoreColor = when {
                        score >= 80 -> Color(0xFF43A047)
                        score >= 50 -> Color(0xFF4A90E2)
                        else -> Color(0xFFF57C00)
                    }

                    Card(modifier = Modifier.fillMaxWidth().clickable { onRecipeClick(recipe) }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                                CircularProgress(progress = score / 100f, color = scoreColor)
                                Text("$score%", fontSize = 14.sp, fontWeight = FontWeight.Black, color = scoreColor)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val emoji = when {
                                        recipe.name.contains("鸡蛋") || recipe.name.contains("蛋") -> "🥚"
                                        recipe.name.contains("肉") || recipe.name.contains("排骨") -> "🥩"
                                        recipe.name.contains("汤") || recipe.name.contains("羹") -> "🥣"
                                        recipe.name.contains("鱼") -> "🐟"
                                        recipe.name.contains("虾") -> "🦐"
                                        recipe.name.contains("菜") -> "🥬"
                                        else -> "🍽️"
                                    }
                                    Text(emoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(recipe.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textDark)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    recipe.requiredIngredients.forEach { ing ->
                                        Surface(color = bgColor, shape = RoundedCornerShape(6.dp)) {
                                            Text(ing, fontSize = 12.sp, color = textGray, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
fun CircularProgress(progress: Float, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(64.dp)) {
        drawArc(color = Color(0xFFF0F0F0), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 16f, cap = StrokeCap.Round))
        drawArc(color = color, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, style = Stroke(width = 16f, cap = StrokeCap.Round))
    }
}

// =========================================================================
// 页面 4：我的/设置
// =========================================================================
@Composable
fun ProfilePage(dbHelper: IngredientDbHelper, isDarkMode: Boolean, onDarkModeChange: (Boolean) -> Unit, bgColor: Color, cardColor: Color, textDark: Color, textGray: Color, onViewHistory: () -> Unit) {
    val allIngredients = dbHelper.getAllIngredients()
    val totalAddedTimes = allIngredients.size
    val now = System.currentTimeMillis()
    val expiredCount = allIngredients.count { it.expirationDate < now }
    val healthScore = (100 - expiredCount * 5).coerceAtLeast(0)
    val recordDays = 18
    val savedCount = allIngredients.size - expiredCount

    Column(modifier = Modifier.fillMaxSize().background(bgColor).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("我的", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = textDark)

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3E50))) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(60.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50)), contentAlignment = Alignment.Center) { Text("👨‍💻", fontSize = 30.sp) }
                Spacer(modifier = Modifier.width(16.dp))
                Column { Text("马佳佳", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White); Text("健康生活达人 · 已使用 30 天", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f)) }
            }
        }

        Text("本月数据", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textGray)
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(0.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                        CircularProgress(progress = healthScore / 100f, color = if (healthScore >= 80) Color(0xFF43A047) else if (healthScore >= 50) Color(0xFFF57C00) else Color(0xFFE53935), modifier = Modifier.fillMaxSize())
                        Text("$healthScore", fontSize = 20.sp, fontWeight = FontWeight.Black, color = textDark)
                    }
                    Spacer(modifier = Modifier.height(8.dp)); Text("健康评分", fontSize = 11.sp, color = textGray)
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        Column { Text("$totalAddedTimes", fontSize = 18.sp, fontWeight = FontWeight.Black, color = textDark); Text("添加总次数", fontSize = 11.sp, color = textGray) }
                        Column { Text("$recordDays", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF4A90E2)); Text("记录天数", fontSize = 11.sp, color = textGray) }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        Column { Text("${(savedCount * 100 / (totalAddedTimes.coerceAtLeast(1)))}%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFF57C00)); Text("减少浪费", fontSize = 11.sp, color = textGray) }
                        Column { Text("$expiredCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF9C27B0)); Text("已过期数", fontSize = 11.sp, color = textGray) }
                    }
                }
            }
        }

        Text("常用功能", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textGray, modifier = Modifier.padding(top = 8.dp))
        Card(modifier = Modifier.fillMaxWidth().clickable { onViewHistory() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(0.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) { Text("📜", fontSize = 24.sp); Spacer(modifier = Modifier.width(12.dp)); Text("查看食材处理记录", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = textDark) }
                Text(">", fontSize = 18.sp, color = textGray)
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(0.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) { Text("🌙", fontSize = 24.sp); Spacer(modifier = Modifier.width(12.dp)); Column { Text("深色模式", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = textDark); Text("切换全局主题风格", fontSize = 12.sp, color = textGray) } }
                Switch(checked = isDarkMode, onCheckedChange = { onDarkModeChange(it) })
            }
        }

        Text("关于", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textGray)
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(0.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("SmartFridge v1.0", fontSize = 14.sp, color = textGray)
                Text("学号：202403090227", fontSize = 14.sp, color = textGray)
                Text("课程：Android 移动终端应用开发技术", fontSize = 14.sp, color = textGray)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}