package com.example.labexam3

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.labexam3.databinding.DialogMoodEmojiSelectorBinding
import com.google.android.material.button.MaterialButton

/**
 * Dialog fragment for selecting mood emoji and adding optional note
 * Provides a grid of emoji options with mood names
 */
class MoodEmojiSelectorDialog : DialogFragment() {

    private var _binding: DialogMoodEmojiSelectorBinding? = null
    private val binding get() = _binding!!

    private var selectedEmoji: String = ""
    private var selectedMoodName: String = ""
    private var onMoodSelectedListener: ((String, String, String) -> Unit)? = null

    /**
     * Sets the callback for when a mood is selected
     * @param listener Callback function with emoji, mood name, and note parameters
     */
    fun setOnMoodSelectedListener(listener: (String, String, String) -> Unit) {
        onMoodSelectedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMoodEmojiSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDialog()
        setupEmojiGrid()
        setupButtons()
    }

    /**
     * Sets up the dialog properties
     */
    private fun setupDialog() {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.setCancelable(true)
    }

    /**
     * Sets up the emoji grid with all available mood options
     */
    private fun setupEmojiGrid() {
        val adapter = EmojiGridAdapter(MoodEntry.MOOD_OPTIONS) { emoji, moodName ->
            selectedEmoji = emoji
            selectedMoodName = moodName
            updateSelectedMoodDisplay()
            updateSaveButtonState()
        }

        binding.recyclerViewEmojis.apply {
            layoutManager = GridLayoutManager(context, 3)
            this.adapter = adapter
        }
    }

    /**
     * Sets up the dialog buttons
     */
    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            if (selectedEmoji.isNotEmpty()) {
                val note = binding.editTextNote.text.toString().trim()
                onMoodSelectedListener?.invoke(selectedEmoji, selectedMoodName, note)
                dismiss()
            }
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        // Initially disable save button
        updateSaveButtonState()
    }

    /**
     * Updates the display showing the selected mood
     */
    private fun updateSelectedMoodDisplay() {
        if (selectedEmoji.isNotEmpty()) {
            binding.textViewSelectedMood.text = "$selectedEmoji $selectedMoodName"
            binding.textViewSelectedMood.visibility = View.VISIBLE
        } else {
            binding.textViewSelectedMood.visibility = View.GONE
        }
    }

    /**
     * Updates the save button enabled state
     */
    private fun updateSaveButtonState() {
        binding.buttonSave.isEnabled = selectedEmoji.isNotEmpty()
        binding.buttonSave.alpha = if (selectedEmoji.isNotEmpty()) 1.0f else 0.5f
    }

    override fun onStart() {
        super.onStart()
        // Set dialog size
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Inner adapter class for the emoji grid
     */
    private class EmojiGridAdapter(
        private val moodOptions: List<Pair<String, String>>,
        private val onEmojiSelected: (String, String) -> Unit
    ) : RecyclerView.Adapter<EmojiGridAdapter.EmojiViewHolder>() {

        private var selectedPosition = -1

        inner class EmojiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textViewEmoji: TextView = itemView.findViewById(R.id.textViewEmoji)
            val textViewMoodName: TextView = itemView.findViewById(R.id.textViewMoodName)

            fun bind(position: Int) {
                val (emoji, moodName) = moodOptions[position]
                textViewEmoji.text = emoji
                textViewMoodName.text = moodName

                // Update selection state
                itemView.isSelected = position == selectedPosition
                itemView.alpha = if (position == selectedPosition) 1.0f else 0.7f

                // Handle click
                itemView.setOnClickListener {
                    val previousPosition = selectedPosition
                    selectedPosition = position

                    // Notify changes
                    if (previousPosition != -1) {
                        notifyItemChanged(previousPosition)
                    }
                    notifyItemChanged(selectedPosition)

                    // Trigger callback
                    onEmojiSelected(emoji, moodName)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_emoji_mood, parent, false)
            return EmojiViewHolder(view)
        }

        override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
            holder.bind(position)
        }

        override fun getItemCount(): Int = moodOptions.size
    }
}