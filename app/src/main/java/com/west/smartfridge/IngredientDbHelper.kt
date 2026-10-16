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

class IngredientDbHelper(context: Context) : SQLiteOpenHelper(context, "smart_fridge.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, category TEXT, quantity INTEGER, expirationDate INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ingredients")
        onCreate(db)
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

    // 统计冰箱里的食材种类总数
    fun getTotalItemCount(): Int {
        return getAllIngredients().size
    }
}