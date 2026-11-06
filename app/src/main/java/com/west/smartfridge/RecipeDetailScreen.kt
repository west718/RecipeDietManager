package com.west.smartfridge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBack: () -> Unit
) {
    val bgWarmGray = Color(0xFFF5F3F0)
    val textDark = Color(0xFF1E1E1E)
    val textGray = Color(0xFF757575)
    val themeOrange = Color(0xFFE65100)

    val recipeImageRes = when {
        recipe.name.contains("西红柿炒鸡蛋") -> R.drawable.xihongshichaojidan
        recipe.name.contains("青椒肉丝") -> R.drawable.qingjiaorousi
        recipe.name.contains("紫菜蛋花汤") -> R.drawable.zicaiodanhuatang
        recipe.name.contains("牛奶炖蛋") -> R.drawable.niunaidundan
        recipe.name.contains("蒜蓉炒青菜") -> R.drawable.suanrongchaocai
        recipe.name.contains("土豆炖牛肉") -> R.drawable.tudoudunniurou
        recipe.name.contains("鸡蛋羹") -> R.drawable.jidangeng
        recipe.name.contains("洋葱炒肉") -> R.drawable.yangcongchaorou
        recipe.name.contains("黄瓜炒鸡蛋") -> R.drawable.huangguachaojidan
        recipe.name.contains("酸辣土豆丝") -> R.drawable.suanlatudousi
        recipe.name.contains("红烧茄子") -> R.drawable.hongshaoqiezi
        recipe.name.contains("上汤菠菜") -> R.drawable.shangtangbocai
        recipe.name.contains("白灼虾") -> R.drawable.baizhuoxia
        recipe.name.contains("红烧排骨") -> R.drawable.hongshaopaigu
        recipe.name.contains("清蒸鱼") -> R.drawable.qingzhengyu
        recipe.name.contains("香菇炖鸡") -> R.drawable.xianggudunji
        recipe.name.contains("西兰花炒虾仁") -> R.drawable.xilanhuachaoxianren
        recipe.name.contains("萝卜排骨汤") -> R.drawable.luobupaigutang
        recipe.name.contains("牛奶燕麦粥") -> R.drawable.niunaiyanmaizhou
        else -> R.drawable.xihongshichaojidan
    }

    Scaffold(
        topBar = {}
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgWarmGray)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. 头部
            Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                Image(
                    painter = painterResource(id = recipeImageRes),
                    contentDescription = recipe.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0x66000000), Color.Transparent, bgWarmGray.copy(alpha = 0.8f), bgWarmGray)
                        )
                    )
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(top = 40.dp, start = 20.dp).size(40.dp).background(Color(0x66000000), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                }
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 24.dp, vertical = 20.dp)) {
                    Surface(color = Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(6.dp)) {
                        Text("✨ 主厨推荐", fontSize = 12.sp, color = themeOrange, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = recipe.name, fontSize = 36.sp, fontWeight = FontWeight.Black, color = textDark)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccessTime, contentDescription = null, tint = textGray, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("15 分钟", fontSize = 13.sp, color = textGray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = themeOrange, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("简单", fontSize = 13.sp, color = textGray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = textGray, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("1-2 人", fontSize = 13.sp, color = textGray)
                        }
                    }
                }
            }

            // 2. 所需食材（Emoji 图标版）
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("所需食材", fontSize = 20.sp, fontWeight = FontWeight.Black, color = textDark)
                Spacer(modifier = Modifier.height(16.dp))

                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recipe.requiredIngredients.size) { index ->
                        val ingredient = recipe.requiredIngredients[index]

                        // 【核心修改】根据食材名字匹配生动的 Emoji
                        val ingredientEmoji = when {
                            ingredient.contains("肉") -> "🥩"
                            ingredient.contains("排骨") -> "🍖"
                            ingredient.contains("鸡") -> "🍗"
                            ingredient.contains("牛") -> "🥩"
                            ingredient.contains("鱼") -> "🐟"
                            ingredient.contains("虾") -> "🦐"
                            ingredient.contains("蛋") -> "🥚"
                            ingredient.contains("葱") -> "🌿"
                            ingredient.contains("蒜") -> "🧄"
                            ingredient.contains("姜") -> "🫚"
                            ingredient.contains("菜") || ingredient.contains("菠菜") -> "🥬"
                            ingredient.contains("土豆") -> "🥔"
                            ingredient.contains("萝卜") -> "🥕"
                            ingredient.contains("西红柿") || ingredient.contains("番茄") -> "🍅"
                            ingredient.contains("茄子") -> "🍆"
                            ingredient.contains("蘑菇") || ingredient.contains("香菇") -> "🍄"
                            ingredient.contains("牛奶") -> "🥛"
                            ingredient.contains("豆腐") -> "🧊"
                            ingredient.contains("燕麦") -> "🌾"
                            else -> "🍽️" // 兜底图标
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.width(90.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                // 用 Emoji 代替文字
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            when {
                                                ingredient.contains("肉") || ingredient.contains("排骨") -> Color(0xFFFFEBEE)
                                                ingredient.contains("菜") || ingredient.contains("葱") -> Color(0xFFE8F5E9)
                                                else -> Color(0xFFFFF3E0)
                                            },
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = ingredientEmoji, fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = ingredient, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textDark, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // 3. 制作步骤（Apple 卡片版）
            Spacer(modifier = Modifier.height(40.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("制作步骤", fontSize = 20.sp, fontWeight = FontWeight.Black, color = textDark)
                Spacer(modifier = Modifier.height(20.dp))

                recipe.steps.forEachIndexed { index, step ->
                    // 【核心修改】把每一步包进一个纯白的大圆角卡片里
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(20.dp), // 苹果风大圆角
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 左侧：深色数字 + 橙色小圆点
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(themeOrange, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "0${index + 1}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF2C3E50)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // 右侧：步骤文字
                            Text(
                                text = step,
                                fontSize = 16.sp,
                                color = textDark,
                                lineHeight = 26.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}