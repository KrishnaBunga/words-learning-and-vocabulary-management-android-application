package com.example.question2.data.repository

import com.example.question2.data.local.WordDao
import com.example.question2.data.model.SavedWord
import kotlinx.coroutines.flow.Flow

class VocabularyRepository(private val wordDao: WordDao) {

    fun getAllWords(): Flow<List<SavedWord>> = wordDao.getAllWords()

    fun getWordsByStatus(isLearned: Boolean): Flow<List<SavedWord>> = wordDao.getWordsByStatus(isLearned)

    suspend fun getWordByName(word: String): SavedWord? = wordDao.getWordByName(word)

    suspend fun saveWord(savedWord: SavedWord): Long = wordDao.insertWord(savedWord)

    suspend fun updateWord(savedWord: SavedWord) = wordDao.updateWord(savedWord)

    suspend fun deleteWord(savedWord: SavedWord) = wordDao.deleteWord(savedWord)

    suspend fun deleteWordById(id: Int) = wordDao.deleteWordById(id)
}