package com.west.smartfridge

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddIngredientScreen(
    onSave: (Ingredient) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { IngredientDbHelper(context) }
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("蔬菜") }
    var shelfLifeDays by remember { mutableStateOf("") }
    var purchaseDateLong by remember { mutableStateOf(System.currentTimeMillis()) }

    var errorMessage by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    // 拍照 Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) errorMessage = "📷 拍照成功！"
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch(null) else errorMessage = "需要相机权限"
    }

    val categories = listOf("蔬菜", "水果", "生鲜", "肉类", "饮品", "零食", "其他")

    // 颜色定义
    val bgColor = Color(0xFFFAF9F6) // 温暖的米色背景
    val textDark = Color(0xFF2C3E50)
    val textGray = Color(0xFF8E9EAB)
    val themeBlue = Color(0xFF4A90E2)

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val purchaseDateText = sdf.format(Date(purchaseDateLong))

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = purchaseDateLong)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { purchaseDateLong = it }
                    showDatePicker = false
                }) { Text("确定", color = themeBlue) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消", color = Color.Gray) } }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加食材", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textDark) },
                navigationIcon = { TextButton(onClick = onBack) { Text("< 返回", color = textGray) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部大标题区
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text("添加食材", fontSize = 28.sp, fontWeight = FontWeight.Black, color = textDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text("选择录入方式", fontSize = 14.sp, color = textGray)
            }

            // 顶部两个录入卡片
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 拍照识别（浅粉紫）
                EntryOptionCard(
                    icon = Icons.Filled.PhotoCamera,
                    label = "拍照识别",
                    bgColor = Color(0xFFF3E5F5),
                    iconColor = Color(0xFFAB47BC),
                    modifier = Modifier.weight(1f),
                    onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) }
                )

                // 手动输入（浅蓝）
                EntryOptionCard(
                    icon = Icons.Filled.Edit,
                    label = "手动输入",
                    bgColor = Color(0xFFE3F2FD),
                    iconColor = Color(0xFF4A90E2),
                    isSelected = true,
                    modifier = Modifier.weight(1f),
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 表单区
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 食材名称
                InputCard(title = "食材名称", iconEmoji = "🏷️") {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = "" },
                        placeholder = { Text("输入食材名称...", color = Color.LightGray, fontSize = 15.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }

                // 数量（含加减号）
                InputCard(title = "数量", iconEmoji = "⚖️") {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { if (it.all { char -> char.isDigit() }) quantity = it; errorMessage = "" },
                            placeholder = { Text("例: 1", color = Color.LightGray, fontSize = 15.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalIconButton(
                            onClick = { val current = quantity.toIntOrNull() ?: 0; if (current > 1) quantity = (current - 1).toString() },
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Color(0xFFF0F0F0), contentColor = textDark)
                        ) { Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledIconButton(
                            onClick = { val current = quantity.toIntOrNull() ?: 0; quantity = (current + 1).toString() },
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = themeBlue)
                        ) { Text("+", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }

                // 食物分类（糖果色标签）
                Column {
                    Text("食物分类", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textDark, modifier = Modifier.padding(bottom = 12.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        categories.forEach { cat ->
                            val isSelected = category == cat
                            // 根据分类分配不同的马卡龙底色
                            val catColor = when (cat) {
                                "蔬菜" -> Color(0xFFE8F5E9)
                                "水果" -> Color(0xFFFFF3E0)
                                "生鲜", "肉类" -> Color(0xFFFFEBEE)
                                "饮品" -> Color(0xFFE3F2FD)
                                else -> Color(0xFFF3E5F5)
                            }
                            val catIcon = when (cat) {
                                "蔬菜" -> "🥬"
                                "水果" -> "🍎"
                                "生鲜" -> "🥚"
                                "肉类" -> "🥩"
                                "饮品" -> "🥛"
                                "零食" -> "🍪"
                                else -> "🍽️"
                            }

                            Surface(
                                color = if (isSelected) catColor else Color.White,
                                shape = RoundedCornerShape(12.dp),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, themeBlue) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                shadowElevation = if (isSelected) 2.dp else 0.dp,
                                modifier = Modifier.clickable { category = cat }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = catIcon, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cat,
                                        color = if (isSelected) themeBlue else textGray,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // 购买日期
                InputCard(title = "购买日期", iconEmoji = "📅") {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = purchaseDateText, fontSize = 15.sp, color = textDark)
                        Icon(Icons.Filled.DateRange, contentDescription = "日历", tint = textGray, modifier = Modifier.size(20.dp))
                    }
                }

                // 保质期
                InputCard(title = "保质期（天）", iconEmoji = "⏳") {
                    OutlinedTextField(
                        value = shelfLifeDays,
                        onValueChange = { if (it.all { char -> char.isDigit() }) shelfLifeDays = it; errorMessage = "" },
                        placeholder = { Text("例: 7", color = Color.LightGray, fontSize = 15.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = if (errorMessage.contains("成功")) Color(0xFF43A047) else Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 底部保存按钮
            Button(
                onClick = {
                    if (name.isEmpty() || quantity.isEmpty() || shelfLifeDays.isEmpty()) {
                        errorMessage = "请完整填写所有信息"
                        return@Button
                    }

                    val days = shelfLifeDays.toIntOrNull() ?: 0
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = purchaseDateLong
                    calendar.add(Calendar.DAY_OF_YEAR, days)
                    val expirationDateLong = calendar.timeInMillis

                    val tempIngredient = Ingredient(
                        name = name, category = category, quantity = quantity.toIntOrNull() ?: 1,
                        expirationDate = expirationDateLong, imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80"
                    )
                    onSave(tempIngredient)

                    coroutineScope.launch {
                        val realImageUrl = dbHelper.getSmartImageUrl(name)
                        val inserted = dbHelper.getAllIngredients().lastOrNull { it.name == name }
                        if (inserted != null) {
                            dbHelper.updateIngredientImage(inserted.id, realImageUrl)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeBlue),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("保存食材", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

// =========================================================================
// 辅助组件：顶部彩色录入卡片
// =========================================================================
@Composable
fun EntryOptionCard(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    iconColor: Color,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4A90E2)) else null
    Card(
        modifier = modifier.height(80.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = if (isSelected) Color(0xFF4A90E2) else iconColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// =========================================================================
// 辅助组件：独立悬浮的纯白输入卡片
// =========================================================================
@Composable
fun InputCard(
    title: String,
    iconEmoji: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Text(text = iconEmoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
            }
            content()
        }
    }
}