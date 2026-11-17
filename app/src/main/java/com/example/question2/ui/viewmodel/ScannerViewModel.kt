package com.example.question2.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.question2.data.local.AppDatabase
import com.example.question2.data.model.SavedWord
import com.example.question2.data.model.WordEntry
import com.example.question2.data.repository.DictionaryRepository
import com.example.question2.data.repository.VocabularyRepository
import com.example.question2.utils.NetworkResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val dictionaryRepository = DictionaryRepository()
    private val vocabularyRepository = VocabularyRepository(
        AppDatabase.getDatabase(application).wordDao()
    )

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val _recognizedWords = MutableLiveData<List<String>>()
    val recognizedWords: LiveData<List<String>> = _recognizedWords

    private val _isRecognizing = MutableLiveData<Boolean>()
    val isRecognizing: LiveData<Boolean> = _isRecognizing

    private val _wordDetailResult = MutableLiveData<NetworkResult<WordEntry>?>()
    val wordDetailResult: LiveData<NetworkResult<WordEntry>?> = _wordDetailResult

    private val _saveStatus = MutableLiveData<String?>()
    val saveStatus: LiveData<String?> = _saveStatus

    private val _scannedImageUri = MutableLiveData<Uri?>()
    val scannedImageUri: LiveData<Uri?> = _scannedImageUri

    fun setScannedImageUri(uri: Uri?) {
        _scannedImageUri.value = uri
    }

    fun processImageFromUri(uri: Uri) {
        try {
            val image = InputImage.fromFilePath(getApplication(), uri)
            runTextRecognition(image)
        } catch (e: Exception) {
            _recognizedWords.value = emptyList()
            _saveStatus.value = "Failed to load image: ${e.localizedMessage}"
        }
    }

    fun processImageFromBitmap(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        runTextRecognition(image)
    }

    private fun runTextRecognition(image: InputImage) {
        _isRecognizing.value = true
        recognizer.process(image)
            .addOnSuccessListener { text ->
                _isRecognizing.value = false
                val words = text.text
                    .split("\\s+".toRegex())
                    .map { it.replace("[^a-zA-Z]".toRegex(), "").trim() }
                    .filter { it.length >= 2 }
                    .distinct()
                _recognizedWords.value = words
            }
            .addOnFailureListener { e ->
                _isRecognizing.value = false
                _recognizedWords.value = emptyList()
                _saveStatus.value = "Text recognition failed: ${e.localizedMessage}"
            }
    }

    fun fetchWordDetail(word: String) {
        viewModelScope.launch {
            _wordDetailResult.value = NetworkResult.Loading()
            val result = dictionaryRepository.getWordDefinition(word)
            _wordDetailResult.value = result
        }
    }

    fun saveWordToVocabulary(wordEntry: WordEntry) {
        viewModelScope.launch {
            try {
                val existing = vocabularyRepository.getWordByName(wordEntry.word ?: "")
                if (existing != null) {
                    _saveStatus.value = "'${wordEntry.word}' is already in your vocabulary."
                    return@launch
                }

                val savedWord = SavedWord(
                    word = wordEntry.word ?: "",
                    phonetic = wordEntry.displayPhonetic,
                    partOfSpeech = wordEntry.displayPartOfSpeech,
                    meaning = wordEntry.displayMeaning,
                    example = wordEntry.displayExample,
                    audioUrl = wordEntry.audioUrl,
                    isLearned = false
                )
                vocabularyRepository.saveWord(savedWord)
                _saveStatus.value = "Saved '${savedWord.word}' to vocabulary!"
            } catch (e: Exception) {
                _saveStatus.value = "Error saving word: ${e.localizedMessage}"
            }
        }
    }

    fun clearSaveStatus() {
        _saveStatus.value = null
    }

    fun clearWordDetail() {
        _wordDetailResult.value = null
    }
}