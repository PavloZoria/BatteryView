package com.pavlo.zoria.batteryview.battery

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat.getColor
import com.pavlo.zoria.batteryview.R

class BatteryLevelIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {
    private var radius: Float = 4f

    // end
    private var endingPath = Path()
    private var endRectPaint =
        Paint().apply {
            style = Paint.Style.STROKE
            pathEffect = CornerPathEffect(radius)
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
    private var endPaintHeightPercent = 40
    private var endPaintWidthPercent = 2

    // Border
    private var borderPaint = Paint().apply {
        color = getColor(context, R.color.battery_border)
        style = Paint.Style.STROKE
    }
    private var borderRect = RectF()
    private var borderStroke: Float = 4.0f

    // Percent
    private var progressMaxEndingPosition = 0f
    private var percentagePaint = Paint().apply {
        pathEffect = CornerPathEffect(radius)
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private lateinit var colorBounds: BatteryColorBoundaries

    @ColorInt
    var borderColor: Int = Color.WHITE
        set(@ColorInt value) {
            borderPaint.color = value
            endRectPaint.color = value
            field = value
            invalidate()
        }

    var borderWidth: Float = 0f
        set(value) {
            borderPaint.strokeWidth = value
            endRectPaint.strokeWidth = value
            field = value
            invalidate()
        }

    private var percentRect = RectF()
    var percent: Float = 50f
        set(value) {
            field = when {
                percent > 100f -> {
                    100f
                }
                percent < 0f -> {
                    0f
                }
                else -> {
                    value
                }
            }
            invalidate()
        }

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val typedValue =
            context.obtainStyledAttributes(attrs, R.styleable.BatteryLevelIndicatorView)
        try {
            percent = typedValue.getFloat(R.styleable.BatteryLevelIndicatorView_percent, 0f)
            radius =
                typedValue.getDimension(R.styleable.BatteryLevelIndicatorView_cornerRadius, 14f)
                    .also {
                        endRectPaint.pathEffect = CornerPathEffect(it * 1.5f)
                    }

            borderStroke =
                typedValue.getDimension(R.styleable.BatteryLevelIndicatorView_borderStrokeSize, 4f)
                    .also {
                        borderPaint.strokeWidth = it
                        endRectPaint.strokeWidth = it
                    }

            typedValue.getColor(
                R.styleable.BatteryLevelIndicatorView_borderStrokeColor,
                Color.BLACK
            ).also {
                borderPaint.color = it
                endRectPaint.color = it
            }

            val lowLevelColor = typedValue.getColorStateList(
                R.styleable.BatteryLevelIndicatorView_lowLevelColor
            )?.defaultColor ?: Color.RED
            val normalLevelColor = typedValue.getColorStateList(
                R.styleable.BatteryLevelIndicatorView_normalLevelColor
            )?.defaultColor ?: Color.MAGENTA
            val excellentLevelColor = typedValue.getColorStateList(
                R.styleable.BatteryLevelIndicatorView_excellentLevelColor
            )?.defaultColor ?: Color.GREEN
            colorBounds = BatteryColorBoundaries(
                BatteryColorBoundary(20, lowLevelColor),
                BatteryColorBoundary(50, normalLevelColor),
                BatteryColorBoundary(100, excellentLevelColor)
            )
        } finally {
            typedValue.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureHeight = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val measureWidth = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        setMeasuredDimension(measureWidth, measureHeight)

        // End
        val rightRectStart = measureWidth - endPaintWidthPercent * measureWidth / 100
        val rightRectTop = measureHeight * ((100 - endPaintHeightPercent) / 2) / 100
        val rightRectBottom = measureHeight - rightRectTop
        val rightRectWidth = measureWidth - rightRectStart
        endingPath = endingPath.apply {
            moveTo(rightRectStart.toFloat(), rightRectTop.toFloat())
            lineTo(measureWidth.toFloat(), rightRectTop.toFloat())
            lineTo(measureWidth.toFloat(), rightRectBottom.toFloat())
            lineTo(rightRectStart.toFloat(), rightRectBottom.toFloat())
        }

        // Border
        val borderLeft = 0f
        val borderTop = 0f
        val borderRight = (measureWidth - borderStroke / 2) - rightRectWidth.toFloat()
        val borderBottom = measureHeight - borderStroke / 2
        borderRect.set(borderLeft, borderTop, borderRight, borderBottom)

        // Progress
        val progressLeft = borderStroke / 2
        val progressTop = borderStroke / 2
        progressMaxEndingPosition = measureWidth - rightRectWidth.toFloat() - borderStroke
        val progressBottom = measureHeight - borderStroke / 2
        percentRect.set(progressLeft, progressTop, progressMaxEndingPosition, progressBottom)
    }

    override fun onDraw(canvas: Canvas) {
        drawProgress(canvas, percent)
        drawBody(canvas)
        drawEndRect(canvas)
    }

    private fun drawBody(canvas: Canvas) {
        canvas.drawRoundRect(borderRect, radius, radius, borderPaint)
    }

    private fun drawProgress(canvas: Canvas, percent: Float) {
        percentagePaint.color = getPercentColor(percent)
        //calculate right position to display the percentage of battery
        percentRect.right =
            progressMaxEndingPosition + (percentRect.left - progressMaxEndingPosition) * (100 - percent) / 100
        canvas.drawRoundRect(percentRect, radius / 1.5f, radius / 1.5f, percentagePaint)
    }

    private fun drawEndRect(canvas: Canvas) {
        canvas.drawPath(endingPath, endRectPaint)
    }

    private fun getPercentColor(percent: Float): Int {
        return colorBounds.boundaries.sortedBy {
            it.topBound
        }.firstOrNull {
            it.topBound >= percent
        }?.color ?: Color.TRANSPARENT
    }

    fun setColorBoundary(boundary: BatteryColorBoundary) {
        colorBounds.boundaries.add(boundary)
        colorBounds.boundaries.sortBy { it.topBound }
    }
}