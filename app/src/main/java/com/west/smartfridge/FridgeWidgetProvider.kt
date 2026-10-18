package com.west.smartfridge

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class FridgeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // 【关键修改】这里改成了新名字 widget_new_layout
            val views = RemoteViews(context.packageName, R.layout.widget_new_layout)

            try {
                val dbHelper = IngredientDbHelper(context)
                val ingredients = dbHelper.getAllIngredients()
                val now = System.currentTimeMillis()
                val threeDaysLater = now + 3 * 24 * 60 * 60 * 1000L

                val expired = ingredients.filter { it.expirationDate < now }
                val expiring = ingredients.filter { it.expirationDate in now..threeDaysLater }

                // 1. 显示总数
                views.setTextViewText(R.id.widget_count, "共 ${ingredients.size} 种")

                // 2. 拼接倒计时状态
                val sb = StringBuilder()
                if (expired.isNotEmpty()) {
                    val names = expired.joinToString("、") {
                        val days = (now - it.expirationDate) / (24 * 60 * 60 * 1000L)
                        "${it.name}(过期${days}天)"
                    }
                    sb.append("🚨 已过期：$names\n")
                }
                if (expiring.isNotEmpty()) {
                    val names = expiring.joinToString("、") {
                        val days = (it.expirationDate - now) / (24 * 60 * 60 * 1000L)
                        "${it.name}(剩${days}天)"
                    }
                    sb.append("⚠️ 快过期：$names")
                }
                if (sb.isEmpty()) {
                    sb.append("冰箱里的食材都很新鲜 ✨")
                }
                views.setTextViewText(R.id.widget_content, sb.toString().trim())

                // 3. 智能菜谱推荐
                val recommended = RecipeRecommender.recommendRecipes(ingredients)
                val bestRecipe = recommended.firstOrNull()
                if (bestRecipe != null) {
                    views.setTextViewText(R.id.widget_recommend, "🍳 推荐：${bestRecipe.first.name}")
                } else {
                    views.setTextViewText(R.id.widget_recommend, "🍳 冰箱没食材啦，去逛逛吧！")
                }

            } catch (e: Exception) {
                views.setTextViewText(R.id.widget_content, "⚠️ 加载失败，请打开App检查")
            }

            // 4. 极简交互：点击整个小组件，直接打开主App
            val mainIntent = Intent(context, MainActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, mainPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_content, mainPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_recommend, mainPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}