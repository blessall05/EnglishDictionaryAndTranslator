package com.blessall05.translator.view.dictionary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blessall05.translator.databinding.ItemWordHistoryBinding
import com.blessall05.translator.model.data.Word

class WordHistoryAdapter : ListAdapter<Word.History, WordHistoryAdapter.ViewHolder>(DiffCallback) {
    private var onItemClickListener: OnItemClickListener? = null
    private var onDeleteClickListener: OnDeleteClickListener? = null

    class ViewHolder(binding: ItemWordHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        val word = binding.word
        val part = binding.part
        val means = binding.means
        val delete = binding.delete
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemWordHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            val item = getItem(position)
            word.text = item.word
            part.text = item.means.joinToString("/") { it.part }
            means.text = item.means.joinToString("/ ") { it.means.joinToString(", ") }
            delete.setOnClickListener {
                onDeleteClickListener?.onDeleteClick(position)
            }
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(item)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Word.History>() {
        override fun areItemsTheSame(oldItem: Word.History, newItem: Word.History): Boolean {
            return oldItem.word == newItem.word
        }

        override fun areContentsTheSame(oldItem: Word.History, newItem: Word.History): Boolean {
            return oldItem == newItem
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun setOnDeleteClickListener(listener: OnDeleteClickListener) {
        onDeleteClickListener = listener
    }

    fun interface OnItemClickListener {
        fun onItemClick(history: Word.History)
    }

    fun interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }
}