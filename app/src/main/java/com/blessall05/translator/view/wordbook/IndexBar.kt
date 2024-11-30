package com.blessall05.translator.view.wordbook

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class IndexBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val letters = ('A'..'Z').map { it.toString() }.toTypedArray()

    private var onIndexChangeListener: ((String) -> Unit)? = null

    private var selectedLetter: String? = null

    private val marginBetween = 100
    private val maxLetterIndexWidth by lazy {
        var maxLetterWidth = 0
        for (letter in letters) {
            paint.getTextBounds(letter, 0, letter.length, textBounds)
            maxLetterWidth = max(maxLetterWidth, textBounds.width())
        }
        maxLetterWidth
    }
    private val maxLetterEmphasisWidth by lazy {
        var maxLetterWidth = 0
        for (letter in letters) {
            selectedPaint.getTextBounds(letter, 0, 1, textBounds)
            maxLetterWidth = max(maxLetterWidth, textBounds.width())
        }
        maxLetterWidth
    }

    fun setOnIndexChangeListener(listener: (String) -> Unit) {
        onIndexChangeListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                val y = event.y
                val index = (y / height * letters.size).toInt().coerceIn(letters.indices)
                selectedLetter = letters[index]
                onIndexChangeListener?.invoke(selectedLetter!!)
                invalidate()
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                selectedLetter = null
                invalidate()
            }
        }
        return true
    }

    private val paint = Paint().apply {
        isAntiAlias = true //抗锯齿
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }

    private val selectedPaint = Paint().apply {
        isAntiAlias = true
        textSize = 100f
        textAlign = Paint.Align.CENTER
    }

    private val textBounds = Rect()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 获取宽度和高度的测量模式和大小
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)


        // 计算IndexBar的宽度
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize  // 如果宽度是精确值或者match_parent，直接使用测量的宽度
            MeasureSpec.AT_MOST -> min(
                widthSize,
                maxLetterIndexWidth / 2 + maxLetterEmphasisWidth / 2 + marginBetween
            )  // 如果宽度是wrap_content，取字母索引的最大宽度和大字母的宽度之和与测量的宽度的最小值
            else -> maxLetterIndexWidth / 2 + maxLetterEmphasisWidth / 2 + marginBetween  // 如果宽度没有指定，取字母索引的最大宽度和大字母的宽度之和
        }

        // 设置测量的宽度和高度
        setMeasuredDimension(width, heightSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val letterHeight = height / letters.size
        val widthRight = width - maxLetterIndexWidth / 2

        for (i in letters.indices) {
            val letter = letters[i]

            paint.getTextBounds(letter, 0, letter.length, textBounds)

            val xPos = widthRight
            val yPos = letterHeight * i + letterHeight / 2 + (textBounds.height() / 2)

            canvas.drawText(letter, xPos.toFloat(), yPos.toFloat(), paint)
        }

        selectedLetter?.let {
            val xPos = widthRight - marginBetween
            val yPos =
                letterHeight * letters.indexOf(selectedLetter) + letterHeight / 2 + (textBounds.height() / 2)
            canvas.drawText(it, xPos.toFloat(), yPos.toFloat(), selectedPaint)
        }
    }
}