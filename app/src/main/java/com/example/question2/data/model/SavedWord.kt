package com.example.question2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_words")
data class SavedWord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val word: String,
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val meaning: String? = null,
    val example: String? = null,
    val audioUrl: String? = null,
    val isLearned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
)