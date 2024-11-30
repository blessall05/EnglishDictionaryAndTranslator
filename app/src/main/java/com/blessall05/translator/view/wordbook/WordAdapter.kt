package com.blessall05.translator.view.wordbook

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blessall05.translator.App.Companion.app
import com.blessall05.translator.R
import com.blessall05.translator.databinding.ItemWordBookBinding
import com.blessall05.translator.model.data.Word
import com.blessall05.translator.util.FileUtil
import com.blessall05.translator.util.ThemeUtil

class WordAdapter : ListAdapter<Word.Book, WordAdapter.ViewHolder>(DiffCallback) {
    val selectedList = mutableListOf<Word.Book>()
    private lateinit var activity: Activity

    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null
    private var onPnCardClickListener: OnPnCardClickListener? = null

    inner class ViewHolder(binding: ItemWordBookBinding) : RecyclerView.ViewHolder(binding.root) {
        private val word = binding.word
        private val part = binding.part
        private val means = binding.means

        private val pnAmCard = binding.pnAmCard.root
        private val pnAmLabel = binding.pnAmCard.pnLabel
        private val pnAm = binding.pnAmCard.pnText
        private val pnAmPlayIcon = binding.pnAmCard.playIcon

        private val pnEnCard = binding.pnEnCard.root
        private val pnEnLabel = binding.pnEnCard.pnLabel
        private val pnEn = binding.pnEnCard.pnText
        private val pnEnPlayIcon = binding.pnEnCard.playIcon

        private val rootCard = binding.root

        fun bind(item: Word.Book) {
            word.text = item.word
            part.text = item.means.joinToString("/") { it.part }
            means.text = item.means.joinToString("/") { it.means.joinToString(",") }

            pnEnCard.isClickable = false
            pnEnCard.visibility = View.GONE
            if (item.pnEn.isNotBlank()) {
                pnEnCard.visibility = View.VISIBLE
                pnEnPlayIcon.visibility = View.GONE
                pnEnLabel.text = app.getString(R.string.pronunciation_English)
                pnEn.text = item.pnEn
                if (FileUtil.isExistInCache(item.word + "-uk.mp3")) {
                    pnEnPlayIcon.visibility = View.VISIBLE
                    pnEnCard.isClickable = true
                    pnEnCard.setOnClickListener {
                        onPnCardClickListener?.onPnCardClick(item.word, true)
                    }
                }
            }

            pnAmCard.isClickable = false
            pnAmCard.visibility = View.GONE
            if (item.pnAm.isNotBlank()) {
                pnAmCard.visibility = View.VISIBLE
                pnAmPlayIcon.visibility = View.GONE
                pnAmLabel.text = app.getString(R.string.pronunciation_American)
                pnAm.text = item.pnAm
                if (FileUtil.isExistInCache(item.word + "-us.mp3")) {
                    pnAmPlayIcon.visibility = View.VISIBLE
                    pnAmCard.isClickable = true
                    pnAmCard.setOnClickListener {
                        onPnCardClickListener?.onPnCardClick(item.word, false)
                    }
                }
            }

            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(item, adapterPosition)
            }
            itemView.setOnLongClickListener {
                onItemLongClickListener?.onItemLongClick(item, adapterPosition)
                true
            }

            if (item in selectedList) {
                rootCard.setCardBackgroundColor(
                    ThemeUtil.getColorByAttr(
                        activity, com.google.android.material.R.attr.colorSurfaceVariant
                    )
                )
            } else {
                rootCard.setCardBackgroundColor(
                    ThemeUtil.getColorByAttr(
                        activity, com.google.android.material.R.attr.backgroundColor
                    )
                )
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        activity = recyclerView.context as Activity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWordBookBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object DiffCallback : DiffUtil.ItemCallback<Word.Book>() {
        override fun areItemsTheSame(oldItem: Word.Book, newItem: Word.Book): Boolean {
            return oldItem.word == newItem.word
        }

        override fun areContentsTheSame(oldItem: Word.Book, newItem: Word.Book): Boolean {
            return oldItem == newItem
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        onItemLongClickListener = listener
    }

    fun setOnPnCardClickListener(listener: OnPnCardClickListener) {
        onPnCardClickListener = listener
    }

    fun interface OnItemClickListener {
        fun onItemClick(item: Word.Book, position: Int)
    }

    fun interface OnItemLongClickListener {
        fun onItemLongClick(item: Word.Book, position: Int)
    }

    fun interface OnPnCardClickListener {
        fun onPnCardClick(word: String, isBritish: Boolean)
    }
}