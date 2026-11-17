package com.example.question2.ui.scanner

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.question2.R
import com.example.question2.data.model.WordEntry
import com.example.question2.databinding.DialogWordDetailBinding
import com.example.question2.databinding.FragmentScannerBinding
import com.example.question2.ui.viewmodel.ScannerViewModel
import com.example.question2.utils.AudioPlayer
import com.example.question2.utils.NetworkResult
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import java.io.File

class ScannerFragment : Fragment() {

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScannerViewModel by activityViewModels()
    private val audioPlayer = AudioPlayer()

    private var cameraTempImageUri: Uri? = null

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraTempImageUri != null) {
            val uri = cameraTempImageUri!!
            binding.ivImagePreview.setImageURI(uri)
            binding.tvImagePlaceholder.visibility = View.GONE
            viewModel.processImageFromUri(uri)
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            binding.ivImagePreview.setImageURI(uri)
            binding.tvImagePlaceholder.visibility = View.GONE
            viewModel.processImageFromUri(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCaptureCamera.setOnClickListener {
            launchCamera()
        }

        binding.btnPickGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        observeViewModel()
    }

    private fun launchCamera() {
        try {
            val context = requireContext()
            val tempFile = File.createTempFile("scanned_image_", ".jpg", context.cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }
            cameraTempImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            cameraLauncher.launch(cameraTempImageUri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to prepare camera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.isRecognizing.observe(viewLifecycleOwner) { isRecognizing ->
            binding.progressBar.visibility = if (isRecognizing) View.VISIBLE else View.GONE
        }

        viewModel.recognizedWords.observe(viewLifecycleOwner) { words ->
            populateWordChips(words)
        }

        viewModel.wordDetailResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val wordEntry = result.data
                    if (wordEntry != null) {
                        showWordDetailDialog(wordEntry)
                    } else {
                        Toast.makeText(requireContext(), "No definition details found.", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.clearWordDetail()
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message ?: "Failed to fetch word details.", Toast.LENGTH_SHORT).show()
                    viewModel.clearWordDetail()
                }
                null -> {
                    // Idle state
                }
            }
        }

        viewModel.saveStatus.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearSaveStatus()
            }
        }
    }

    private fun populateWordChips(words: List<String>) {
        binding.chipGroupWords.removeAllViews()

        if (words.isEmpty()) {
            binding.tvEmptyChips.visibility = View.VISIBLE
            return
        }

        binding.tvEmptyChips.visibility = View.GONE

        val context = requireContext()
        for (word in words) {
            val chip = Chip(context).apply {
                text = word
                isClickable = true
                isCheckable = false
                setChipBackgroundColorResource(R.color.chip_bg)
                setTextColor(resources.getColor(R.color.text_primary, null))
                setOnClickListener {
                    viewModel.fetchWordDetail(word)
                }
            }
            binding.chipGroupWords.addView(chip)
        }
    }

    private fun showWordDetailDialog(wordEntry: WordEntry) {
        val dialogBinding = DialogWordDetailBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogBinding.root)

        dialogBinding.dialogTvWord.text = wordEntry.word
        dialogBinding.dialogTvPhonetic.text = wordEntry.displayPhonetic
        dialogBinding.dialogTvPhonetic.visibility =
            if (wordEntry.displayPhonetic.isBlank()) View.GONE else View.VISIBLE

        dialogBinding.dialogChipPartOfSpeech.text = wordEntry.displayPartOfSpeech
        dialogBinding.dialogTvMeaning.text = wordEntry.displayMeaning

        val example = wordEntry.displayExample
        if (!example.isNullOrBlank()) {
            dialogBinding.dialogTvExampleLabel.visibility = View.VISIBLE
            dialogBinding.dialogTvExample.visibility = View.VISIBLE
            dialogBinding.dialogTvExample.text = "\"$example\""
        } else {
            dialogBinding.dialogTvExampleLabel.visibility = View.GONE
            dialogBinding.dialogTvExample.visibility = View.GONE
        }

        val audioUrl = wordEntry.audioUrl
        if (!audioUrl.isNullOrBlank()) {
            dialogBinding.dialogBtnPlayAudio.visibility = View.VISIBLE
            dialogBinding.dialogBtnPlayAudio.setOnClickListener {
                audioPlayer.playAudio(
                    requireContext(),
                    audioUrl,
                    onStart = {
                        Toast.makeText(requireContext(), "Playing audio...", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        } else {
            dialogBinding.dialogBtnPlayAudio.visibility = View.GONE
        }

        dialogBinding.dialogBtnSave.setOnClickListener {
            viewModel.saveWordToVocabulary(wordEntry)
            dialog.dismiss()
        }

        dialogBinding.dialogBtnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            audioPlayer.stopAudio()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioPlayer.stopAudio()
        _binding = null
    }
}