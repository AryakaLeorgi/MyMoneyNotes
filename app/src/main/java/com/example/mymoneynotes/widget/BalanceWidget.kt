package com.example.mymoneynotes.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.mymoneynotes.MyMoneyApplication
import com.example.mymoneynotes.R
import com.example.mymoneynotes.ui.util.FormatUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BalanceWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_balance)
        
        CoroutineScope(Dispatchers.IO).launch {
            val repository = (context.applicationContext as MyMoneyApplication).repository
            val income = repository.totalIncome.first()
            val expense = repository.totalExpense.first()
            val balance = income - expense

            views.setTextViewText(R.id.widget_balance, FormatUtils.formatCurrency(balance))
            views.setTextViewText(R.id.widget_income, FormatUtils.formatCurrency(income))
            views.setTextViewText(R.id.widget_expense, FormatUtils.formatCurrency(expense))

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
