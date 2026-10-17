package com.west.smartfridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.west.smartfridge.ui.theme.SmartFridgeTheme

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Fridge : BottomNavItem("fridge", "我的冰箱", Icons.Filled.Home)
    object Recipes : BottomNavItem("recipes", "菜谱推荐", Icons.Filled.List)
    object Settings : BottomNavItem("settings", "设置", Icons.Filled.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }

            SmartFridgeTheme(darkTheme = isDarkMode) {
                MainScreen(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = { isDarkMode = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { IngredientDbHelper(context) }

    val navItems = listOf(BottomNavItem.Fridge, BottomNavItem.Recipes, BottomNavItem.Settings)
    var selectedItem by remember { mutableStateOf(navItems[0]) }
    var showAddScreen by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // 【新增】菜谱详情页的状态
    var selectedIngredient by remember { mutableStateOf<Ingredient?>(null) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }

    // 加载态
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(refreshTrigger) {
        isLoading = true
        kotlinx.coroutines.delay(1000)
        isLoading = false
    }

    // 自动填充测试数据
    LaunchedEffect(Unit) {
        if (dbHelper.getAllIngredients().isEmpty()) {
            val today = System.currentTimeMillis()
            dbHelper.addIngredient(Ingredient(name = "鸡蛋", category = "生鲜", quantity = 10, expirationDate = today + 24 * 60 * 60 * 1000L))
            dbHelper.addIngredient(Ingredient(name = "牛奶", category = "饮品", quantity = 2, expirationDate = today + 30 * 24 * 60 * 60 * 1000L))
            refreshTrigger++
        }
    }

    val ingredientList = remember(refreshTrigger) { dbHelper.getAllIngredients() }

    // ================= 页面跳转逻辑 =================
    if (selectedIngredient != null) {
        // 食材详情页
        IngredientDetailScreen(
            ingredient = selectedIngredient!!,
            onEat = {
                dbHelper.deleteIngredient(selectedIngredient!!.id)
                selectedIngredient = null
                refreshTrigger++
            },
            onBack = { selectedIngredient = null }
        )
    } else if (selectedRecipe != null) {
        // 【新增】菜谱详情页
        RecipeDetailScreen(
            recipe = selectedRecipe!!,
            onBack = { selectedRecipe = null }
        )
    } else if (showAddScreen) {
        // 添加食材页
        AddIngredientScreen(
            onSave = { ingredient ->
                dbHelper.addIngredient(ingredient)
                showAddScreen = false
                refreshTrigger++
            },
            onBack = { showAddScreen = false }
        )
    } else {
        // 主界面 Scaffold ...
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedItem.label) },
                    actions = {
                        if (selectedItem == BottomNavItem.Fridge) {
                            IconButton(onClick = { showAddScreen = true }) {
                                Icon(Icons.Filled.Add, contentDescription = "添加食材")
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selectedItem == item,
                            onClick = { selectedItem = item }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    when (selectedItem) {
                        // ================= 冰箱页面 =================
                        BottomNavItem.Fridge -> {
                            if (ingredientList.isEmpty()) {
                                Text("冰箱空空如也，点击右上角 + 添加食材")
                            } else {
                                var selectedCategory by remember { mutableStateOf("全部") }
                                val allCategories = listOf("全部") + ingredientList.map { it.category }.distinct()
                                val filteredList = if (selectedCategory == "全部") ingredientList
                                else ingredientList.filter { it.category == selectedCategory }

                                val threeDaysLater = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L
                                val expiringSoon = ingredientList.filter {
                                    it.expirationDate in System.currentTimeMillis()..threeDaysLater
                                }
                                val totalQuantity = ingredientList.sumOf { it.quantity }
                                val categoryStats = ingredientList.groupBy { it.category }.mapValues { it.value.size }

                                Column(modifier = Modifier.fillMaxSize()) {
                                    // 数据看板
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(text = "📊 冰箱数据看板", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(text = "${ingredientList.size}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                                                    Text(text = "食材种类", style = MaterialTheme.typography.bodySmall)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(text = "$totalQuantity", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                                                    Text(text = "食材总数", style = MaterialTheme.typography.bodySmall)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(text = "${expiringSoon.size}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error)
                                                    Text(text = "即将过期", style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                            if (categoryStats.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(text = "分类：" + categoryStats.entries.joinToString("  ") { "${it.key} ${it.value}" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                    }

                                    // 分类筛选
                                    ScrollableTabRow(
                                        selectedTabIndex = allCategories.indexOf(selectedCategory).coerceAtLeast(0),
                                        modifier = Modifier.fillMaxWidth(),
                                        edgePadding = 16.dp
                                    ) {
                                        allCategories.forEach { category ->
                                            Tab(selected = selectedCategory == category, onClick = { selectedCategory = category }, text = { Text(category) })
                                        }
                                    }

                                    // 列表
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(filteredList) { ingredient ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth().clickable { selectedIngredient = ingredient }
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(text = ingredient.name, style = MaterialTheme.typography.titleLarge)
                                                        Text(text = "类别：${ingredient.category}", style = MaterialTheme.typography.bodyMedium)
                                                        Text(text = "数量：${ingredient.quantity}", style = MaterialTheme.typography.bodyMedium)
                                                        val dateText = android.text.format.DateFormat.format("yyyy-MM-dd", ingredient.expirationDate).toString()
                                                        Text(text = "过期日期：$dateText", style = MaterialTheme.typography.bodyMedium)
                                                    }
                                                    IconButton(onClick = {
                                                        dbHelper.deleteIngredient(ingredient.id)
                                                        refreshTrigger++
                                                    }) {
                                                        Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ================= 菜谱推荐页面 =================
                        BottomNavItem.Recipes -> {
                            val recommended = RecipeRecommender.recommendRecipes(ingredientList)
                            if (recommended.isEmpty()) {
                                Text("冰箱里还没有食材，去添加一些吧！")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item {
                                        Text(text = "根据你的食材，为你推荐以下菜谱：", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                                    }
                                    items(recommended) { (recipe, score) ->
                                        // 【修改】给卡片加上点击事件，跳转到菜谱详情页
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedRecipe = recipe }
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(text = recipe.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                                Text(text = "匹配度：$score%", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
                                                Text(text = "所需食材：${recipe.requiredIngredients.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ================= 设置页面 =================
                        BottomNavItem.Settings -> {
                            var message by remember { mutableStateOf("") }

                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "⚙️ 高级设置", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = "📊 冰箱数据统计", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = "食材种类：${dbHelper.getTotalItemCount()} 种", style = MaterialTheme.typography.bodyLarge)
                                        val totalQuantity = dbHelper.getAllIngredients().sumOf { it.quantity }
                                        Text(text = "食材总数：${totalQuantity} 个", style = MaterialTheme.typography.bodyLarge)
                                    }
                                }

                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(text = "🌙 深色模式", style = MaterialTheme.typography.titleMedium)
                                            Text(text = "切换全局主题风格", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                        }
                                        Switch(
                                            checked = isDarkMode,
                                            onCheckedChange = {
                                                onDarkModeChange(it)
                                                message = if (it) "✅ 已切换到深色模式" else "✅ 已切换到浅色模式"
                                            }
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        val today = System.currentTimeMillis()
                                        dbHelper.addIngredient(Ingredient(name = "西红柿", category = "蔬菜", quantity = 3, expirationDate = today + 5 * 24 * 60 * 60 * 1000L))
                                        dbHelper.addIngredient(Ingredient(name = "猪肉", category = "肉类", quantity = 1, expirationDate = today + 2 * 24 * 60 * 60 * 1000L))
                                        refreshTrigger++
                                        message = "✅ 测试数据已添加！请去【我的冰箱】查看"
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("📥 添加测试数据") }

                                Button(
                                    onClick = {
                                        val all = dbHelper.getAllIngredients()
                                        for (item in all) { dbHelper.deleteIngredient(item.id) }
                                        refreshTrigger++
                                        message = "🗑️ 冰箱已清空！"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("🗑️ 清空所有食材") }

                                if (message.isNotEmpty()) {
                                    Text(text = message, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = "👨‍💻 关于本作品", style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = "SmartFridge v1.0", style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "学号：202403090227", style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "姓名：马佳佳", style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "课程：Android 移动终端应用开发技术", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}