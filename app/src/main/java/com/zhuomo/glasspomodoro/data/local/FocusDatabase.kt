package com.zhuomo.glasspomodoro.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "focus_records")
data class FocusRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 25,
    val completed: Boolean = true
)

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<FocusRecordEntity>>

    @Query("SELECT COUNT(*) FROM focus_records WHERE completed = 1 AND timestamp >= :since")
    fun countCompletedSince(since: Long): Flow<Int>

    @Query("SELECT SUM(durationMinutes) FROM focus_records WHERE completed = 1 AND timestamp >= :since")
    fun totalMinutesSince(since: Long): Flow<Int?>

    @Insert
    suspend fun insert(record: FocusRecordEntity)

    @Query("DELETE FROM focus_records WHERE timestamp < :before")
    suspend fun deleteOld(before: Long)
}

@Database(entities = [FocusRecordEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun focusDao(): FocusDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "glasspomodoro.db")
                    .fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
