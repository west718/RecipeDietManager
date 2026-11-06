package com.west.smartfridge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    dbHelper: IngredientDbHelper,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("全部") }
    val tabs = listOf("全部", "已吃掉", "已丢弃")
    val allHistory = remember { dbHelper.getAllHistory() }

    val filteredHistory = when (selectedTab) {
        "已吃掉" -> allHistory.filter { it.actionType == "EATEN" }
        "已丢弃" -> allHistory.filter { it.actionType == "DISCARDED" }
        else -> allHistory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("处理记录", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("< 返回", color = Color.Gray) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Color(0xFFEBEBEB), RoundedCornerShape(10.dp))
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (isSelected) Color.White else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .wrapContentSize(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = { selectedTab = tab },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = tab,
                                color = if (isSelected) Color.Black else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("最近", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (filteredHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无记录", color = Color.LightGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredHistory) { record ->
                        HistoryItem(record)
                    }
                    item { Spacer(modifier = Modifier.height(30.dp)) }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(record: HistoryRecord) {
    val isEaten = record.actionType == "EATEN"
    val tagBgColor = if (isEaten) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val tagTextColor = if (isEaten) Color(0xFF43A047) else Color(0xFFE53935)
    val tagText = if (isEaten) "已吃掉" else "已丢弃"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(record.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = record.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFF0F0F0))
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = record.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = record.actionTime, fontSize = 12.sp, color = Color.Gray)
        }

        Surface(
            color = tagBgColor,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = tagText,
                color = tagTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}