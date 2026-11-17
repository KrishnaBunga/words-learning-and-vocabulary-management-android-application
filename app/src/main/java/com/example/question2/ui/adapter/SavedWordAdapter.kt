package com.example.question2.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.question2.R
import com.example.question2.data.model.SavedWord
import com.example.question2.databinding.ItemSavedWordBinding

class SavedWordAdapter(
    private val onPlayAudioClicked: (String) -> Unit,
    private val onToggleLearnedClicked: (SavedWord) -> Unit,
    private val onDeleteClicked: (SavedWord) -> Unit
) : ListAdapter<SavedWord, SavedWordAdapter.WordViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = ItemSavedWordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = getItem(position)
        holder.bind(word)
    }

    inner class WordViewHolder(private val binding: ItemSavedWordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SavedWord) {
            val context = itemView.context
            binding.tvWord.text = item.word
            binding.tvPhonetic.text = item.phonetic ?: ""
            binding.tvPhonetic.visibility = if (item.phonetic.isNullOrBlank()) View.GONE else View.VISIBLE

            binding.chipPartOfSpeech.text = item.partOfSpeech ?: "word"

            binding.tvMeaning.text = item.meaning ?: "No meaning provided."

            if (!item.example.isNullOrBlank()) {
                binding.tvExample.visibility = View.VISIBLE
                binding.tvExample.text = "Example: ${item.example}"
            } else {
                binding.tvExample.visibility = View.GONE
            }

            if (!item.audioUrl.isNullOrBlank()) {
                binding.btnPlayAudio.visibility = View.VISIBLE
                binding.btnPlayAudio.setOnClickListener {
                    onPlayAudioClicked(item.audioUrl)
                }
            } else {
                binding.btnPlayAudio.visibility = View.GONE
            }

            if (item.isLearned) {
                binding.chipLearnedStatus.text = "Learned ✓"
                binding.chipLearnedStatus.chipBackgroundColor = ContextCompat.getColorStateList(context, android.R.color.holo_green_light)
                binding.chipLearnedStatus.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                binding.chipLearnedStatus.isChecked = true
            } else {
                binding.chipLearnedStatus.text = "Not Learned"
                binding.chipLearnedStatus.chipBackgroundColor = ContextCompat.getColorStateList(context, android.R.color.darker_gray)
                binding.chipLearnedStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                binding.chipLearnedStatus.isChecked = false
            }

            binding.chipLearnedStatus.setOnClickListener {
                onToggleLearnedClicked(item)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClicked(item)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SavedWord>() {
        override fun areItemsTheSame(oldItem: SavedWord, newItem: SavedWord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavedWord, newItem: SavedWord): Boolean {
            return oldItem == newItem
        }
    }
}