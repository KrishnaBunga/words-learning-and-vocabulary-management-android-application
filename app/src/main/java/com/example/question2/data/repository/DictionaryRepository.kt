package com.example.question2.data.repository

import com.example.question2.data.model.WordEntry
import com.example.question2.data.remote.RetrofitClient
import com.example.question2.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DictionaryRepository {

    suspend fun getWordDefinition(word: String): NetworkResult<WordEntry> {
        return withContext(Dispatchers.IO) {
            try {
                val cleanWord = word.trim().lowercase().filter { it.isLetter() || it == '-' }
                if (cleanWord.isBlank()) {
                    return@withContext NetworkResult.Error<WordEntry>("Invalid word query.")
                }

                val response = RetrofitClient.apiService.getWordDefinition(cleanWord)
                if (response.isSuccessful) {
                    val list = response.body()
                    val entry = list?.firstOrNull()
                    if (entry != null) {
                        NetworkResult.Success<WordEntry>(entry)
                    } else {
                        NetworkResult.Error<WordEntry>("No definition found for '$word'.")
                    }
                } else {
                    val code = response.code()
                    if (code == 404) {
                        NetworkResult.Error<WordEntry>("Word '$word' not found in dictionary.")
                    } else {
                        NetworkResult.Error<WordEntry>("API Error ($code): ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                NetworkResult.Error<WordEntry>("Network error: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }
}