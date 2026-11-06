package com.west.smartfridge

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Ingredient(
    val id: Int = 0,
    val name: String,
    val category: String,
    val quantity: Int,
    val expirationDate: Long,
    val imageUrl: String = ""
)

data class HistoryRecord(
    val id: Int = 0,
    val name: String,
    val actionType: String,
    val actionTime: String,
    val imageUrl: String
)

class IngredientDbHelper(context: Context) : SQLiteOpenHelper(context, "smart_fridge.db", null, 3) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, category TEXT, quantity INTEGER, expirationDate INTEGER, imageUrl TEXT)")
        db.execSQL("CREATE TABLE history (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, actionType TEXT, actionTime TEXT, imageUrl TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ingredients")
        db.execSQL("DROP TABLE IF EXISTS history")
        onCreate(db)
    }

    fun addIngredient(ingredient: Ingredient) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", ingredient.name)
            put("category", ingredient.category)
            put("quantity", ingredient.quantity)
            put("expirationDate", ingredient.expirationDate)
            put("imageUrl", ingredient.imageUrl)
        }
        db.insert("ingredients", null, values)
        db.close()
    }

    fun updateIngredientImage(id: Int, imageUrl: String) {
        val db = writableDatabase
        val values = ContentValues().apply { put("imageUrl", imageUrl) }
        db.update("ingredients", values, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun getAllIngredients(): List<Ingredient> {
        val list = mutableListOf<Ingredient>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ingredients ORDER BY expirationDate ASC", null)
        while (cursor.moveToNext()) {
            list.add(
                Ingredient(
                    cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getLong(4), cursor.getString(5)
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

    fun updateIngredientExpiration(id: Int, newExpirationDate: Long) {
        val db = writableDatabase
        val values = ContentValues().apply { put("expirationDate", newExpirationDate) }
        db.update("ingredients", values, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun getTotalItemCount(): Int = getAllIngredients().size

    // ================= 【稳定图库】常见食材绝对精准 + 联网兜底 =================
    suspend fun getSmartImageUrl(foodName: String): String = withContext(Dispatchers.IO) {
        val lowerName = foodName.lowercase()

        val localUrl = when {
            lowerName.contains("茄子") -> "https://images.unsplash.com/photo-1615484477778-ca3b77940c25?w=400&q=80"
            lowerName.contains("青椒") -> "https://images.unsplash.com/photo-1563514227147-6d2ff665a6a0?w=400&q=80"
            lowerName.contains("西红柿") || lowerName.contains("番茄") -> "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=400&q=80"
            lowerName.contains("土豆") -> "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=400&q=80"
            lowerName.contains("胡萝卜") -> "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400&q=80"
            lowerName.contains("菠菜") || lowerName.contains("生菜") || lowerName.contains("青菜") -> "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=400&q=80"
            lowerName.contains("洋葱") -> "https://images.unsplash.com/photo-1508747703725-719777637510?w=400&q=80"
            lowerName.contains("黄瓜") -> "https://images.unsplash.com/photo-1604977042946-1eecc30f269e?w=400&q=80"
            lowerName.contains("猪肉") -> "https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=400&q=80"
            lowerName.contains("牛肉") -> "https://images.unsplash.com/photo-1603048297172-c92544798d5a?w=400&q=80"
            lowerName.contains("鸡蛋") -> "https://images.unsplash.com/photo-1587486913049-53fc88980cfc?w=400&q=80"
            lowerName.contains("鸡胸") || lowerName.contains("鸡肉") -> "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400&q=80"
            lowerName.contains("鱼") -> "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=400&q=80"
            lowerName.contains("虾") -> "https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?w=400&q=80"
            lowerName.contains("苹果") -> "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400&q=80"
            lowerName.contains("香蕉") -> "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400&q=80"
            lowerName.contains("西瓜") -> "https://images.unsplash.com/photo-1587049352847-8d4e2b1b7f9a?w=400&q=80"
            lowerName.contains("葡萄") -> "https://images.unsplash.com/photo-1596363505729-4190a9506133?w=400&q=80"
            lowerName.contains("草莓") -> "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=400&q=80"
            lowerName.contains("牛奶") -> "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400&q=80"
            lowerName.contains("可乐") -> "https://images.unsplash.com/photo-1629203849820-fdd70d49c38e?w=400&q=80"
            lowerName.contains("橙汁") || lowerName.contains("果汁") -> "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=400&q=80"
            lowerName.contains("咖啡") -> "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&q=80"
            lowerName.contains("面包") -> "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400&q=80"
            lowerName.contains("面条") || lowerName.contains("面") -> "https://images.unsplash.com/photo-1612929633738-8fe44f7ec841?w=400&q=80"
            else -> null
        }

        if (localUrl != null) return@withContext localUrl

        // 联网兜底：维基百科
        try {
            val searchUrl = "https://zh.wikipedia.org/w/api.php?action=query&generator=search&gsrsearch=${URLEncoder.encode(foodName, "UTF-8")}&gsrlimit=1&prop=pageimages&piprop=original&format=json"
            val connection = URL(searchUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val pages = JSONObject(response).optJSONObject("query")?.optJSONObject("pages")
                if (pages != null) {
                    val pageId = pages.keys().next()
                    val originalImage = pages.getJSONObject(pageId).optJSONObject("original")
                    if (originalImage != null && originalImage.has("source")) {
                        return@withContext originalImage.getString("source")
                    }
                }
            }
        } catch (e: Exception) { }

        // 联网兜底：必应
        try {
            val keyword = URLEncoder.encode("$foodName 食物", "UTF-8")
            return@withContext "https://tse1.mm.bing.net/th?q=$keyword"
        } catch (e: Exception) {
            return@withContext "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80"
        }
    }

    // ================= 【纯正中文版】和风天气 API =================
    suspend fun getWeatherInfo(): String = withContext(Dispatchers.IO) {
        try {
            // 你的专属 API Key
            val apiKey = "9a70b9061cd74a72b72fd0d20ab17444"

            // 【已修复】替换为你专属的 API Host
            val apiHost = "kj2k5rd76h.re.qweatherapi.com"

            val locationId = "101210101" // 杭州的城市ID

            val weatherUrl = "https://$apiHost/v7/weather/now?location=$locationId&key=$apiKey"
            val connection = URL(weatherUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            // 【已修复】必须带上 User-Agent 防止 403
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)

                // 如果返回状态码 200，说明获取成功
                if (jsonObject.getString("code") == "200") {
                    val now = jsonObject.getJSONObject("now")
                    val temp = now.getString("temp") // 温度
                    val text = now.getString("text") // 天气状况（中文）
                    return@withContext "$text ${temp}℃"
                } else {
                    return@withContext "API错误码: ${jsonObject.getString("code")}"
                }
            }
            "请求失败: ${connection.responseCode}"
        } catch (e: Exception) {
            "网络异常: ${e.message}"
        }
    }

    // ================= 处理记录相关 =================
    fun deleteAndRecord(ingredient: Ingredient, actionType: String) {
        val db = writableDatabase
        db.delete("ingredients", "id = ?", arrayOf(ingredient.id.toString()))
        val sdf = SimpleDateFormat("今天 HH:mm", Locale.getDefault())
        val timeStr = sdf.format(Date())
        val values = ContentValues().apply {
            put("name", ingredient.name)
            put("actionType", actionType)
            put("actionTime", timeStr)
            put("imageUrl", ingredient.imageUrl)
        }
        db.insert("history", null, values)
        db.close()
    }

    fun getAllHistory(): List<HistoryRecord> {
        val list = mutableListOf<HistoryRecord>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM history ORDER BY id DESC", null)
        while (cursor.moveToNext()) {
            list.add(HistoryRecord(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)))
        }
        cursor.close()
        db.close()
        return list
    }
}