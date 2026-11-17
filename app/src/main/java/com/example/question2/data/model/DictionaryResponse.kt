package com.example.question2.data.model

import com.google.gson.annotations.SerializedName

class DictionaryResponse : ArrayList<WordEntry>()

data class WordEntry(
    @SerializedName("word")
    val word: String? = null,
    @SerializedName("phonetic")
    val phonetic: String? = null,
    @SerializedName("phonetics")
    val phonetics: List<PhoneticDto>? = null,
    @SerializedName("meanings")
    val meanings: List<MeaningDto>? = null
) {
    val displayPhonetic: String
        get() {
            if (!phonetic.isNullOrBlank()) return phonetic
            val found = phonetics?.firstOrNull { !it.text.isNullOrBlank() }?.text
            return found ?: ""
        }

    val audioUrl: String?
        get() {
            return phonetics?.firstOrNull { !it.audio.isNullOrBlank() }?.audio
        }

    val displayPartOfSpeech: String
        get() {
            return meanings?.firstOrNull { !it.partOfSpeech.isNullOrBlank() }?.partOfSpeech ?: "word"
        }

    val displayMeaning: String
        get() {
            meanings?.forEach { meaning ->
                val def = meaning.definitions?.firstOrNull { !it.definition.isNullOrBlank() }?.definition
                if (!def.isNullOrBlank()) return def
            }
            return "No definition found."
        }

    val displayExample: String?
        get() {
            meanings?.forEach { meaning ->
                meaning.definitions?.forEach { def ->
                    if (!def.example.isNullOrBlank()) return def.example
                }
            }
            return null
        }
}

data class PhoneticDto(
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("audio")
    val audio: String? = null
)

data class MeaningDto(
    @SerializedName("partOfSpeech")
    val partOfSpeech: String? = null,
    @SerializedName("definitions")
    val definitions: List<DefinitionDto>? = null
)

data class DefinitionDto(
    @SerializedName("definition")
    val definition: String? = null,
    @SerializedName("example")
    val example: String? = null
)