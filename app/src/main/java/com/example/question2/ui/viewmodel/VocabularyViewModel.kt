package com.example.question2.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.example.question2.data.local.AppDatabase
import com.example.question2.data.model.SavedWord
import com.example.question2.data.repository.VocabularyRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

enum class FilterStatus {
    ALL, LEARNED, UNLEARNED
}

class VocabularyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VocabularyRepository(
        AppDatabase.getDatabase(application).wordDao()
    )

    private val _filterStatus = MutableLiveData(FilterStatus.ALL)
    val filterStatus: LiveData<FilterStatus> = _filterStatus

    val savedWords: LiveData<List<SavedWord>> = _filterStatus.switchMap { filter ->
        when (filter) {
            FilterStatus.ALL -> repository.getAllWords().asLiveData()
            FilterStatus.LEARNED -> repository.getWordsByStatus(true).asLiveData()
            FilterStatus.UNLEARNED -> repository.getWordsByStatus(false).asLiveData()
            else -> repository.getAllWords().asLiveData()
        }
    }

    fun setFilter(filter: FilterStatus) {
        _filterStatus.value = filter
    }

    fun toggleLearnedStatus(word: SavedWord) {
        viewModelScope.launch {
            val updated = word.copy(isLearned = !word.isLearned)
            repository.updateWord(updated)
        }
    }

    fun deleteWord(word: SavedWord) {
        viewModelScope.launch {
            repository.deleteWord(word)
        }
    }
}