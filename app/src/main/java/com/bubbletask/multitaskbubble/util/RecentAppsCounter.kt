package com.bubbletask.multitaskbubble.util

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.*

class RecentAppsCounter(private val context: Context) {

    fun getRecentAppsCount(): Int {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 60 * 2 // Last 2 hours

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTime,
            endTime
        )

        return stats?.filter { it.lastTimeUsed > startTime }?.map { it.packageName }?.distinct()?.size ?: 0
    }
}