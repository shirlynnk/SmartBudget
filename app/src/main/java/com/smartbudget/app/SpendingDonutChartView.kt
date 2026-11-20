package com.smartbudget.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.smartbudget.app.data.repository.CategorySpending

/** Draws a category donut from the expense totals supplied by DashboardViewModel. */
class SpendingDonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val palette = intArrayOf(
        R.color.deep_teal,
        R.color.turquoise,
        R.color.chart_blue,
        R.color.chart_amber,
        R.color.chart_mint
    ).map { ContextCompat.getColor(context, it) }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }
    private val bounds = RectF()
    private var categories: List<CategorySpending> = emptyList()

    fun submitData(items: List<CategorySpending>) {
        categories = items.filter { it.amount > 0.0 }
        contentDescription = if (categories.isEmpty()) {
            "No spending recorded"
        } else {
            "Spending by category: " + categories.joinToString { "${it.categoryName}, ${it.amount}" }
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val inset = 8f * density
        val diameter = minOf(width, height).toFloat() - inset * 2
        if (diameter <= 0f) return
        val left = (width - diameter) / 2f
        val top = (height - diameter) / 2f
        bounds.set(left, top, left + diameter, top + diameter)
        paint.strokeWidth = 18f * density
        paint.color = ContextCompat.getColor(context, R.color.chart_track)
        canvas.drawArc(bounds, 0f, 360f, false, paint)

        val total = categories.sumOf { it.amount }
        if (total <= 0.0) return
        var startAngle = -90f
        categories.forEachIndexed { index, category ->
            val sweep = (category.amount / total * 360f).toFloat()
            paint.color = palette[index % palette.size]
            canvas.drawArc(bounds, startAngle, sweep, false, paint)
            startAngle += sweep
        }
    }
}
