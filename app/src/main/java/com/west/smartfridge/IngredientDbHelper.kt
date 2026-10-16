package com.west.smartfridge

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// 食材数据类
data class Ingredient(
    val id: Int = 0,
    val name: String,
    val category: String,
    val quantity: Int,
    val expirationDate: Long
)

class IngredientDbHelper(context: Context) : SQLiteOpenHelper(context, "smart_fridge_v2.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, category TEXT, quantity INTEGER, expirationDate INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ingredients")
        onCreate(db)
    }
    // 自动填充测试数据（仅用于演示）
    fun addTestData() {
        // 只有数据库为空时才添加，防止重复
        if (getAllIngredients().isEmpty()) {
            val today = System.currentTimeMillis()
            // 添加一条明天过期的（会触发提醒）
            addIngredient(Ingredient(name = "鸡蛋", category = "生鲜", quantity = 10, expirationDate = today + 24 * 60 * 60 * 1000L))
            // 添加一条很久以后过期的（不会触发提醒）
            addIngredient(Ingredient(name = "牛奶", category = "饮品", quantity = 2, expirationDate = today + 30 * 24 * 60 * 60 * 1000L))
        }
    }
    fun addIngredient(ingredient: Ingredient) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", ingredient.name)
            put("category", ingredient.category)
            put("quantity", ingredient.quantity)
            put("expirationDate", ingredient.expirationDate)
        }
        db.insert("ingredients", null, values)
        db.close()
    }

    fun getAllIngredients(): List<Ingredient> {
        val list = mutableListOf<Ingredient>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ingredients ORDER BY expirationDate ASC", null)
        while (cursor.moveToNext()) {
            list.add(
                Ingredient(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getLong(4)
                )
            )
        }
        cursor.close()
        db.close()
        return list
    }

    fun deleteIngredient(id: Int) {
        val db = writableDatabase
        db.delete("ingredients", "id = ?", arrayOf(id.toString()))
        db.close()
    }
}