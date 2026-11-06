package com.west.smartfridge

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews

class NewFridgeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.west.smartfridge.ACTION_WIDGET_REFRESH_NEW") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, NewFridgeWidgetProvider::class.java)
            )
            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.widget_new_layout)
                views.setTextViewText(R.id.widget_refresh, "⏳")
                appWidgetManager.updateAppWidget(id, views)

                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    updateAppWidget(context, appWidgetManager, id)
                }, 500)
            }
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_new_layout)

            try {
                val dbHelper = IngredientDbHelper(context)
                val ingredients = dbHelper.getAllIngredients()
                val now = System.currentTimeMillis()
                val threeDaysLater = now + 3 * 24 * 60 * 60 * 1000L

                val expired = ingredients.filter { it.expirationDate < now }
                val expiring = ingredients.filter { it.expirationDate in now..threeDaysLater }

                views.setTextViewText(R.id.widget_count, "共 ${ingredients.size} 种")

                val sb = StringBuilder()
                if (expired.isNotEmpty()) {
                    val names = expired.joinToString("、") { "${it.name}(过期${(now - it.expirationDate) / (24 * 60 * 60 * 1000L)}天)" }
                    sb.append("🚨 已过期：$names\n")
                    views.setTextColor(R.id.widget_content, Color.parseColor("#E53935"))
                } else if (expiring.isNotEmpty()) {
                    val names = expiring.joinToString("、") { "${it.name}(剩${(it.expirationDate - now) / (24 * 60 * 60 * 1000L)}天)" }
                    sb.append("⚠️ 快过期：$names")
                    views.setTextColor(R.id.widget_content, Color.parseColor("#F57C00"))
                } else {
                    sb.append("✅ 冰箱里的食材都很新鲜")
                    views.setTextColor(R.id.widget_content, Color.parseColor("#43A047"))
                }
                views.setTextViewText(R.id.widget_content, sb.toString().trim())

                val recommended = RecipeRecommender.recommendRecipes(ingredients)
                val bestRecipe = recommended.firstOrNull()
                if (bestRecipe != null) {
                    views.setTextViewText(R.id.widget_recommend, "🍳 推荐：${bestRecipe.first.name}")
                } else {
                    views.setTextViewText(R.id.widget_recommend, "🍳 冰箱没食材啦，去逛逛吧！")
                }

                views.setTextViewText(R.id.widget_refresh, "🔄")
            } catch (e: Exception) {
                views.setTextViewText(R.id.widget_content, "⚠️ 加载失败")
            }

            val mainIntent = Intent(context, MainActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_title, mainPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_content, mainPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_recommend, mainPendingIntent)

            val refreshIntent = Intent(context, NewFridgeWidgetProvider::class.java).apply {
                action = "com.west.smartfridge.ACTION_WIDGET_REFRESH_NEW"
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(context, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}