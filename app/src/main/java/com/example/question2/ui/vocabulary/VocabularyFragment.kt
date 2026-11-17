package com.example.question2.ui.vocabulary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.question2.R
import com.example.question2.databinding.FragmentVocabularyBinding
import com.example.question2.ui.adapter.SavedWordAdapter
import com.example.question2.ui.viewmodel.FilterStatus
import com.example.question2.ui.viewmodel.VocabularyViewModel
import com.example.question2.utils.AudioPlayer

class VocabularyFragment : Fragment() {

    private var _binding: FragmentVocabularyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VocabularyViewModel by activityViewModels()
    private val audioPlayer = AudioPlayer()
    private lateinit var adapter: SavedWordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVocabularyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = SavedWordAdapter(
            onPlayAudioClicked = { audioUrl ->
                audioPlayer.playAudio(
                    requireContext(),
                    audioUrl,
                    onStart = {
                        Toast.makeText(requireContext(), "Playing pronunciation...", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onToggleLearnedClicked = { savedWord ->
                viewModel.toggleLearnedStatus(savedWord)
            },
            onDeleteClicked = { savedWord ->
                viewModel.deleteWord(savedWord)
                Toast.makeText(requireContext(), "Deleted '${savedWord.word}'", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvVocabulary.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVocabulary.adapter = adapter
    }

    private fun setupFilters() {
        binding.chipFilterAll.setOnClickListener {
            viewModel.setFilter(FilterStatus.ALL)
        }
        binding.chipFilterLearned.setOnClickListener {
            viewModel.setFilter(FilterStatus.LEARNED)
        }
        binding.chipFilterUnlearned.setOnClickListener {
            viewModel.setFilter(FilterStatus.UNLEARNED)
        }
    }

    private fun observeViewModel() {
        viewModel.savedWords.observe(viewLifecycleOwner) { words ->
            adapter.submitList(words)
            if (words.isNullOrEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvVocabulary.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvVocabulary.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioPlayer.stopAudio()
        _binding = null
    }
}