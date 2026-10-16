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
fun AddIngredientScreen(
    onSave: (Ingredient) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var expirationDateText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加食材") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("取消")
                    }
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("食材名称（如：鸡蛋）") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("类别（如：蔬菜、肉类）") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("数量") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = expirationDateText,
                onValueChange = { expirationDateText = it },
                label = { Text("过期日期（格式：2026-12-31）") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (name.isNotEmpty() && quantity.isNotEmpty() && expirationDateText.isNotEmpty()) {
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val date = sdf.parse(expirationDateText)?.time ?: 0L
                            val newIngredient = Ingredient(
                                name = name,
                                category = category.ifEmpty { "其他" },
                                quantity = quantity.toIntOrNull() ?: 1,
                                expirationDate = date
                            )
                            onSave(newIngredient)
                        } catch (e: Exception) {
                            // 日期格式错误
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存食材")
            }
        }
    }
}