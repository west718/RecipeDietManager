package com.west.smartfridge

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDetailScreen(
    ingredient: Ingredient,
    onEat: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("食材详情") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "类别：${ingredient.category}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "数量：${ingredient.quantity}", style = MaterialTheme.typography.bodyLarge)
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val dateStr = sdf.format(ingredient.expirationDate)
                    val isExpiring = ingredient.expirationDate < System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L
                    Text(
                        text = "过期日期：$dateStr",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isExpiring) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onEat,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("🍽️ 立即食用（从冰箱移除）")
            }
        }
    }
}