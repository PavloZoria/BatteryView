package com.pavlo.zoria.batteryview.battery

import android.content.Context
import android.content.res.TypedArray
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

class BatteryLevelIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {
    //region internatl state
    private var cornerRadius: Float = 4f
    private var colorBounds: MutableList<BatteryColorBoundary> = mutableListOf()
    //endregion

    //region Drawing
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
        pathEffect = CornerPathEffect(cornerRadius)
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    // end
    private var endingPath = Path()
    private var endRectPaint =
        Paint().apply {
            style = Paint.Style.STROKE
            pathEffect = CornerPathEffect(cornerRadius)
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
    private var endPaintHeightPercent = 40
    private var endPaintWidthPercent = 2
    //enderegion

    //region Public interface
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
    var percentage: Float = 50f
        set(value) {
            field = when {
                percentage > 100f -> {
                    100f
                }
                percentage < 0f -> {
                    0f
                }
                else -> {
                    value
                }
            }
            invalidate()
        }

    fun setColorBoundary(boundary: BatteryColorBoundary) {
        colorBounds.add(boundary)
        colorBounds.sortBy { it.topBound }
        invalidate()
    }
    //endregion

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val typedValue =
            context.obtainStyledAttributes(attrs, R.styleable.BatteryLevelIndicator)
        try {
            readAttributes(typedValue)
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
        drawPercentage(canvas, percentage)
        drawBatteryBody(canvas)
        drawEndPath(canvas)
    }

    private fun drawBatteryBody(canvas: Canvas) {
        canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, borderPaint)
    }

    private fun drawPercentage(canvas: Canvas, percentage: Float) {
        percentagePaint.color = getPercentColor(percentage)
        //calculate right position to display the percentage of battery
        percentRect.right =
            progressMaxEndingPosition + (percentRect.left - progressMaxEndingPosition) * (100 - percentage) / 100
        canvas.drawRoundRect(percentRect, cornerRadius / 1.5f, cornerRadius / 1.5f, percentagePaint)
    }

    private fun drawEndPath(canvas: Canvas) {
        canvas.drawPath(endingPath, endRectPaint)
    }

    private fun getPercentColor(percent: Float): Int {
        return colorBounds.sortedBy {
            it.topBound
        }.firstOrNull {
            it.topBound >= percent
        }?.color ?: Color.TRANSPARENT
    }
    private fun readAttributes(typedValue: TypedArray) {
        percentage = typedValue.getFloat(R.styleable.BatteryLevelIndicator_percent, 0f)
        cornerRadius =
            typedValue.getDimension(R.styleable.BatteryLevelIndicator_cornerRadius, 14f)
                .also {
                    endRectPaint.pathEffect = CornerPathEffect(it * 1.5f)
                }

        borderStroke =
            typedValue.getDimension(R.styleable.BatteryLevelIndicator_borderStrokeSize, 4f)
                .also {
                    borderPaint.strokeWidth = it
                    endRectPaint.strokeWidth = it
                }

        typedValue.getColor(
            R.styleable.BatteryLevelIndicator_borderStrokeColor,
            Color.BLACK
        ).also {
            borderPaint.color = it
            endRectPaint.color = it
        }

        val lowLevelColor = typedValue.getColorStateList(
            R.styleable.BatteryLevelIndicator_lowLevelColor
        )?.defaultColor ?: Color.RED
        val normalLevelColor = typedValue.getColorStateList(
            R.styleable.BatteryLevelIndicator_normalLevelColor
        )?.defaultColor ?: Color.YELLOW
        val excellentLevelColor = typedValue.getColorStateList(
            R.styleable.BatteryLevelIndicator_excellentLevelColor
        )?.defaultColor ?: Color.GREEN
        colorBounds = mutableListOf(
            BatteryColorBoundary(25, lowLevelColor),
            BatteryColorBoundary(75, normalLevelColor),
            BatteryColorBoundary(100, excellentLevelColor)
        )
    }
}