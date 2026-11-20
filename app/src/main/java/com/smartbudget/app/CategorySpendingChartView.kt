package com.smartbudget.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.smartbudget.app.data.repository.CategorySpending
import kotlin.math.max

class CategorySpendingChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var categories: List<CategorySpending> = emptyList()
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f * resources.displayMetrics.scaledDensity
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10f * resources.displayMetrics.scaledDensity
    }

    init {
        barPaint.color = ContextCompat.getColor(context, R.color.deep_teal)
        labelPaint.color = ContextCompat.getColor(context, R.color.obsidian_text)
        valuePaint.color = ContextCompat.getColor(context, R.color.turquoise)
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    fun submitData(items: List<CategorySpending>) {
        categories = items.take(5)
        contentDescription = if (categories.isEmpty()) {
            "No category spending data"
        } else {
            "Category comparison: " + categories.joinToString { it.categoryName + " " + it.amount }
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (categories.isEmpty()) {
            labelPaint.textSize = 14f * resources.displayMetrics.scaledDensity
            canvas.drawText("No category spending data yet", paddingLeft.toFloat(), height / 2f, labelPaint)
            labelPaint.textSize = 11f * resources.displayMetrics.scaledDensity
            return
        }

        val maxAmount = categories.maxOf { it.amount }.coerceAtLeast(1.0)
        val density = resources.displayMetrics.density
        val plotTop = 8f * density
        val labelHeight = 34f * density
        val plotBottom = height - labelHeight
        val plotHeight = max(1f, plotBottom - plotTop)
        val slotWidth = (width - paddingLeft - paddingRight).toFloat() / categories.size
        val barWidth = minOf(34f * density, slotWidth * 0.55f)

        categories.forEachIndexed { index, item ->
            val centerX = paddingLeft + slotWidth * index + slotWidth / 2f
            val barHeight = (item.amount / maxAmount * plotHeight).toFloat()
            val top = plotBottom - barHeight
            canvas.drawRoundRect(
                centerX - barWidth / 2f, top, centerX + barWidth / 2f, plotBottom,
                7f * density, 7f * density, barPaint
            )

            val name = if (item.categoryName.length > 9) item.categoryName.take(8) + "…" else item.categoryName
            val nameWidth = labelPaint.measureText(name)
            canvas.drawText(name, centerX - nameWidth / 2f, height - 15f * density, labelPaint)
            val amountLabel = compactAmount(item.amount)
            val amountWidth = valuePaint.measureText(amountLabel)
            canvas.drawText(amountLabel, centerX - amountWidth / 2f, height - 2f * density, valuePaint)
        }
    }

    private fun compactAmount(amount: Double): String = when {
        amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000)
        amount >= 10_000 -> String.format("%.0fk", amount / 1_000)
        amount >= 1_000 -> String.format("%.1fk", amount / 1_000)
        else -> String.format("%.0f", amount)
    }
}
