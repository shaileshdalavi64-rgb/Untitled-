package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey
    val symbol: String,
    val exchange: String,
    val companyName: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "alert_rules")
data class AlertRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,
    val exchange: String,
    val alertType: String, // "BOUNCE_SMA", "BREAKOUT_SMA", "REJECTION_SMA", "PROXIMITY"
    val tolerancePercent: Float = 0.5f,
    val isEnabled: Boolean = true,
    val lastTriggeredAt: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notification_history")
data class NotificationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val symbol: String,
    val exchange: String,
    val setupType: String,
    val price: Float,
    val sma44: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
