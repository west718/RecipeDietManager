package com.west.smartfridge

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
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

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.west.smartfridge.ACTION_WIDGET_REFRESH") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, FridgeWidgetProvider::class.java)
            )
            for (id in ids) {
                // 1. 先显示“正在刷新...”
                val views = RemoteViews(context.packageName, R.layout.widget_layout)
                views.setTextViewText(R.id.widget_refresh, "🔄 正在刷新...")
                appWidgetManager.updateAppWidget(id, views)

                // 2. 延迟 500 毫秒后，执行真正的刷新
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    updateAppWidget(context, appWidgetManager, id)
                }, 500)
            }
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            try {
                val dbHelper = IngredientDbHelper(context)
                val ingredients = dbHelper.getAllIngredients()
                val threeDaysLater = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L
                val expiring = ingredients.filter {
                    it.expirationDate in System.currentTimeMillis()..threeDaysLater
                }

                // 显示食材总数
                views.setTextViewText(R.id.widget_count, "共 ${ingredients.size} 种")

                val contentText = if (expiring.isEmpty()) {
                    "冰箱里没有即将过期的食材 ✨"
                } else {
                    "⚠️ 即将过期：\n" + expiring.joinToString("\n") { "• ${it.name}" }
                }
                views.setTextViewText(R.id.widget_content, contentText)

                // 恢复刷新按钮文字
                views.setTextViewText(R.id.widget_refresh, "🔄 刷新")

            } catch (e: Exception) {
                views.setTextViewText(R.id.widget_content, "⚠️ 加载失败，请打开App检查")
            }

            // 点击 "🔄 刷新" 按钮 -> 发送广播刷新
            val refreshIntent = Intent(context, FridgeWidgetProvider::class.java).apply {
                action = "com.west.smartfridge.ACTION_WIDGET_REFRESH"
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context, 1, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)

            // 点击标题 -> 打开主App
            val mainIntent = Intent(context, MainActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, mainPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}