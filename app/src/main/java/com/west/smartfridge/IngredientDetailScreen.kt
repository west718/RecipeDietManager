package com.west.smartfridge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDetailScreen(
    ingredient: Ingredient,
    onEat: () -> Unit,
    onDiscard: () -> Unit,
    onDelay: () -> Unit,
    onBack: () -> Unit
) {
    val bgGray = Color(0xFFF8F9FA)
    val textDark = Color(0xFF2C3E50)
    val themeBlue = Color(0xFF1565C0)
    val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    val dateStr = sdf.format(ingredient.expirationDate)
    val isExpiring = ingredient.expirationDate < System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("食材详情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = textDark) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgGray)
            )
        },
        containerColor = bgGray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 实物大图
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(180.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(ingredient.imageUrl).crossfade(true).build(),
                    contentDescription = ingredient.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery),
                    placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = ingredient.name, fontSize = 32.sp, fontWeight = FontWeight.Black, color = textDark)
            Spacer(modifier = Modifier.height(32.dp))

            // 信息卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    InfoRow(label = "分类", value = ingredient.category, textDark = textDark)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))

                    InfoRow(label = "数量", value = "${ingredient.quantity} 个", textDark = textDark)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))

                    // 【新增】购买日期（用 过期日期 - 7天 作为演示）
                    val purchaseDateStr = sdf.format(ingredient.expirationDate - 7 * 24 * 60 * 60 * 1000L)
                    InfoRow(label = "购买日期", value = purchaseDateStr, textDark = textDark)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("过期日期", color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = dateStr,
                            color = if (isExpiring) Color(0xFFE53935) else textDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("请检查气味、外观和保存情况，如有异常，请勿食用。", fontSize = 12.sp, color = Color.LightGray)
            }

            Spacer(modifier = Modifier.weight(1f))

            // 底部按钮
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = onEat, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9))) {
                    Text("吃掉", color = Color(0xFF43A047), fontWeight = FontWeight.Bold)
                }
                Button(onClick = onDiscard, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE))) {
                    Text("丢弃", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
                Button(onClick = onDelay, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F4F8))) {
                    Text("延后", color = themeBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, textDark: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, color = textDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}