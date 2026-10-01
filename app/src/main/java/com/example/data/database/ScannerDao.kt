package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannerDao {
    // Watchlist
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAllWatchlist(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE symbol = :symbol")
    suspend fun removeFromWatchlist(symbol: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE symbol = :symbol)")
    suspend fun isWatched(symbol: String): Boolean

    // Alert Rules
    @Query("SELECT * FROM alert_rules ORDER BY createdAt DESC")
    fun getAllAlertRules(): Flow<List<AlertRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlertRule(rule: AlertRuleEntity): Long

    @Update
    suspend fun updateAlertRule(rule: AlertRuleEntity)

    @Query("DELETE FROM alert_rules WHERE id = :id")
    suspend fun deleteAlertRule(id: Long)

    @Query("DELETE FROM alert_rules WHERE symbol = :symbol")
    suspend fun deleteAlertRulesForSymbol(symbol: String)

    @Query("SELECT * FROM alert_rules WHERE isEnabled = 1")
    suspend fun getActiveRulesList(): List<AlertRuleEntity>

    // Notification History
    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationHistoryEntity): Long

    @Query("UPDATE notification_history SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: Long)

    @Query("UPDATE notification_history SET isRead = 1")
    suspend fun markAllNotificationsRead()

    @Query("DELETE FROM notification_history")
    suspend fun clearAllNotifications()

    @Query("SELECT COUNT(*) FROM notification_history WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>
}
