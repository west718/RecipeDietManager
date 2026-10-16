package com.west.smartfridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

// 底部导航项
sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Fridge : BottomNavItem("fridge", "我的冰箱", Icons.Filled.Home)
    object Recipes : BottomNavItem("recipes", "菜谱推荐", Icons.Filled.List)
    object Settings : BottomNavItem("settings", "设置", Icons.Filled.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartFridgeTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    // 初始化数据库工具类
    val dbHelper = remember { IngredientDbHelper(context) }

    // 状态管理
    val navItems = listOf(BottomNavItem.Fridge, BottomNavItem.Recipes, BottomNavItem.Settings)
    var selectedItem by remember { mutableStateOf(navItems[0]) }
    var showAddScreen by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) } // 用于刷新列表

    // 【自动添加测试数据，如果数据库为空的话】
    LaunchedEffect(Unit) {
        if (dbHelper.getAllIngredients().isEmpty()) {
            val today = System.currentTimeMillis()
            dbHelper.addIngredient(
                Ingredient(name = "鸡蛋", category = "生鲜", quantity = 10, expirationDate = today + 24 * 60 * 60 * 1000L)
            )
            dbHelper.addIngredient(
                Ingredient(name = "牛奶", category = "饮品", quantity = 2, expirationDate = today + 30 * 24 * 60 * 60 * 1000L)
            )
            refreshTrigger++ // 通知界面刷新
        }
    }

    // 读取数据库中的食材列表
    val ingredientList = remember(refreshTrigger) { dbHelper.getAllIngredients() }

    if (showAddScreen) {
        // 显示添加食材页面
        AddIngredientScreen(
            onSave = { ingredient ->
                dbHelper.addIngredient(ingredient)
                showAddScreen = false
                refreshTrigger++ // 保存后刷新列表
            },
            onBack = { showAddScreen = false }
        )
    } else {
        // 主界面
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                when (selectedItem) {
                    // ================= 冰箱页面 =================
                    BottomNavItem.Fridge -> {
                        if (ingredientList.isEmpty()) {
                            Text("冰箱空空如也，点击右上角 + 添加食材")
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // 过期提醒横幅
                                val threeDaysLater = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L
                                val expiringSoon = ingredientList.filter {
                                    it.expirationDate in System.currentTimeMillis()..threeDaysLater
                                }

                                if (expiringSoon.isNotEmpty()) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = "⚠️ 有 ${expiringSoon.size} 种食材即将过期，请尽快食用！",
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(16.dp),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(ingredientList) { ingredient ->
                                        Card(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = ingredient.name,
                                                        style = MaterialTheme.typography.titleLarge
                                                    )
                                                    Text(
                                                        text = "类别：${ingredient.category}",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    Text(
                                                        text = "数量：${ingredient.quantity}",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    val dateText = android.text.format.DateFormat.format(
                                                        "yyyy-MM-dd",
                                                        ingredient.expirationDate
                                                    ).toString()
                                                    Text(
                                                        text = "过期日期：$dateText",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                                IconButton(onClick = {
                                                    dbHelper.deleteIngredient(ingredient.id)
                                                    refreshTrigger++
                                                }) {
                                                    Icon(
                                                        Icons.Filled.Delete,
                                                        contentDescription = "删除",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
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
                                    Text(
                                        text = "根据你的食材，为你推荐以下菜谱：",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                items(recommended) { (recipe, score) ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = recipe.name,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "匹配度：$score%",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = "所需食材：${recipe.requiredIngredients.joinToString(", ")}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ================= 设置页面 =================
                    BottomNavItem.Settings -> Text("这里是【设置】页面\n（后续开发）")
                }
            }
        }
    }
}