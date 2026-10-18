package com.freechart.signalai.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "signals")
data class SignalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String, val market: String, val direction: String,
    val entry: Double, val sl: Double, val tp1: Double, val tp2: Double,
    val score: Int, val confirmations: Int, val probability: Double,
    val createdAt: Long = System.currentTimeMillis(), val status: String = "OPEN"
)

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val signalId: Long, val symbol: String, val direction: String, val mode: String,
    val entry: Double, val exit: Double?, val riskPct: Double,
    val pnlUsd: Double = 0.0, val pnlPct: Double = 0.0, val rMultiple: Double = 0.0,
    val openedAt: Long = System.currentTimeMillis(), val closedAt: Long? = null
)

@Dao
interface AppDao {
    @Insert suspend fun insertSignal(s: SignalEntity): Long
    @Insert suspend fun insertTrade(t: TradeEntity): Long
    @Query("SELECT * FROM signals ORDER BY createdAt DESC") fun signals(): Flow<List<SignalEntity>>
    @Query("SELECT * FROM trades ORDER BY openedAt DESC") fun trades(): Flow<List<TradeEntity>>
    @Query("SELECT COUNT(*) FROM signals") fun signalCount(): Flow<Int>
    @Query("SELECT COUNT(*) FROM trades WHERE closedAt IS NULL") fun openTradeCount(): Flow<Int>
    @Query("SELECT COALESCE(SUM(pnlUsd),0) FROM trades") fun totalPnl(): Flow<Double>
}

@Database(entities = [SignalEntity::class, TradeEntity::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun dao(): AppDao
    companion object {
        @Volatile private var I: AppDb? = null
        fun get(c: Context): AppDb = I ?: synchronized(this) {
            I ?: Room.databaseBuilder(c.applicationContext, AppDb::class.java, "signalai.db").build().also { I = it }
        }
    }
}
