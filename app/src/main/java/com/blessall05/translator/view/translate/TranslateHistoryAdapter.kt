package com.blessall05.translator.view.translate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blessall05.translator.databinding.ItemTranslateHistoryBinding
import com.blessall05.translator.model.data.Translation

class TranslateHistoryAdapter :
    ListAdapter<Translation.History, TranslateHistoryAdapter.ViewHolder>(DiffCallback) {
    private var onItemClickListener: OnItemClickListener? = null
    private var onDeleteClickListener: OnDeleteClickListener? = null

    class ViewHolder(binding: ItemTranslateHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        val srcText = binding.srcText
        val destText = binding.destText
        val delete = binding.delete
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemTranslateHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            val item = getItem(position)
            srcText.text = item.src
            destText.text = item.dst
            delete.setOnClickListener {
                onDeleteClickListener?.onDeleteClick(position)
            }
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(item)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Translation.History>() {
        override fun areItemsTheSame(oldItem: Translation.History, newItem: Translation.History) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Translation.History,
            newItem: Translation.History
        ) =
            oldItem == newItem
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun setDeleteClickListener(listener: OnDeleteClickListener) {
        onDeleteClickListener = listener
    }

    fun interface OnItemClickListener {
        fun onItemClick(translation: Translation.History)
    }

    fun interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }
}