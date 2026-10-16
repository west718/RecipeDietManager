package com.west.smartfridge

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
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
            // 1. 获取数据库数据
            val dbHelper = IngredientDbHelper(context)
            val ingredients = dbHelper.getAllIngredients()
            val threeDaysLater = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L
            val expiring = ingredients.filter { it.expirationDate in System.currentTimeMillis()..threeDaysLater }

            // 2. 构建显示文本
            val contentText = if (expiring.isEmpty()) {
                "冰箱里没有即将过期的食材"
            } else {
                "⚠️ 即将过期：\n" + expiring.joinToString("\n") { "• ${it.name}" }
            }

            // 3. 获取布局并设置文本
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            views.setTextViewText(R.id.widget_content, contentText)

            // 4. 更新小组件
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}