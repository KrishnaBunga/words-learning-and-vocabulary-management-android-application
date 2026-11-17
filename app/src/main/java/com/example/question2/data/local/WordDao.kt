package com.example.question2.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.question2.data.model.SavedWord
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM saved_words ORDER BY timestamp DESC")
    fun getAllWords(): Flow<List<SavedWord>>

    @Query("SELECT * FROM saved_words WHERE isLearned = :isLearned ORDER BY timestamp DESC")
    fun getWordsByStatus(isLearned: Boolean): Flow<List<SavedWord>>

    @Query("SELECT * FROM saved_words WHERE LOWER(word) = LOWER(:word) LIMIT 1")
    suspend fun getWordByName(word: String): SavedWord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: SavedWord): Long

    @Update
    suspend fun updateWord(word: SavedWord): Int

    @Delete
    suspend fun deleteWord(word: SavedWord): Int

    @Query("DELETE FROM saved_words WHERE id = :id")
    suspend fun deleteWordById(id: Int): Int
}