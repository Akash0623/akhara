package com.akhara.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akhara.data.db.entity.BodyWeight
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyWeightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BodyWeight)

    @Query("SELECT * FROM body_weight ORDER BY date DESC")
    fun getAll(): Flow<List<BodyWeight>>

    @Query("SELECT * FROM body_weight ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): BodyWeight?

    @Query("SELECT * FROM body_weight WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getForRange(startDate: Long, endDate: Long): List<BodyWeight>
}
