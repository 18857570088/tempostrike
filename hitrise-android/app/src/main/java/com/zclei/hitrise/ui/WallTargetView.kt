package com.zclei.hitrise.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.animation.LinearInterpolator
import com.zclei.hitrise.R
import kotlin.math.min

/** The six regions follow the BLE data 6 numbering, left to right on each row. */
class WallTargetView(context: Context) : View(context) {
    private val bitmap = BitmapFactory.decodeResource(resources, R.drawable.tempo_wall_target)
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val hitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(255, 155, 155) }
    private val scoreTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val source = Rect(0, 0, bitmap.width, bitmap.height)
    private val destination = RectF()
    private val levels = FloatArray(6)
    private val scores = IntArray(6)
    private val displayedCounts = IntArray(6)
    private val scoreAlpha = FloatArray(6)
    private val scoreScale = FloatArray(6)
    private var activeValuePad = -1
    private var scoreStartsVisible = false
    private val scaledPadPaths = Array(6) { Path() }
    private val metricPositions = arrayOf(
        floatArrayOf(342f, 300f, 342f, 405f),
        floatArrayOf(620f, 300f, 620f, 405f),
        floatArrayOf(903f, 300f, 903f, 405f),
        floatArrayOf(342f, 820f, 342f, 925f),
        floatArrayOf(620f, 820f, 620f, 925f),
        floatArrayOf(903f, 820f, 903f, 925f),
    )
    // These paths sit within the green faces, leaving the rounded stitched edges untouched.
    private val padOutlines = arrayOf(
        floatArrayOf(247f, 195f, 287f, 145f, 373f, 132f, 451f, 522f, 440f, 540f, 271f, 540f, 245f, 511f),
        floatArrayOf(454f, 140f, 483f, 126f, 753f, 126f, 781f, 141f, 703f, 530f, 681f, 544f, 558f, 544f, 536f, 527f),
        floatArrayOf(880f, 132f, 967f, 145f, 995f, 193f, 995f, 512f, 970f, 540f, 805f, 540f, 784f, 519f),
        floatArrayOf(246f, 683f, 272f, 655f, 441f, 655f, 451f, 673f, 370f, 1061f, 347f, 1074f, 246f, 1012f),
        floatArrayOf(555f, 656f, 680f, 656f, 701f, 674f, 782f, 1060f, 760f, 1078f, 485f, 1078f, 461f, 1060f, 537f, 676f),
        floatArrayOf(806f, 656f, 970f, 656f, 995f, 684f, 995f, 1012f, 887f, 1074f, 865f, 1062f, 784f, 675f),
    )
    private val padAnimators =
        Array(6) { index ->
            ValueAnimator.ofFloat(0.64f, 0.64f, 0f).apply {
                duration = 520L
                interpolator = LinearInterpolator()
                addUpdateListener {
                    levels[index] = it.animatedValue as Float
                    invalidate()
                }
            }
        }
    private val scoreAnimator =
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1_500L
            interpolator = LinearInterpolator()
            addUpdateListener {
                val index = activeValuePad
                if (index !in 0..5) return@addUpdateListener
                val fraction = it.animatedValue as Float
                scoreAlpha[index] =
                    if (scoreStartsVisible) {
                        if (fraction < 0.78f) 1f else (1f - fraction) / 0.22f
                    } else {
                        when {
                            fraction < 0.12f -> fraction / 0.12f
                            fraction < 0.78f -> 1f
                            else -> (1f - fraction) / 0.22f
                        }
                    }.coerceIn(0f, 1f)
                scoreScale[index] =
                    if (scoreStartsVisible) {
                        1f + 0.05f * (1f - fraction / 0.12f).coerceIn(0f, 1f)
                    } else {
                        0.82f + 0.18f * (fraction / 0.12f).coerceIn(0f, 1f)
                    }
                invalidate()
            }
        }

    fun resetMetrics() {
        displayedCounts.fill(0)
        scores.fill(0)
        padAnimators.forEach { it.cancel() }
        scoreAnimator.cancel()
        scoreStartsVisible = false
        levels.fill(0f)
        scoreAlpha.fill(0f)
        activeValuePad = -1
        invalidate()
    }

    fun flashPad(location: Int, pressureScore: Int, cumulativeCount: Int) {
        if (location !in 1..6) return
        val index = location - 1
        padAnimators[index].cancel()
        levels[index] = 0.64f
        padAnimators[index].start()

        val keepValuesVisible =
            activeValuePad in 0..5 && scoreAlpha[activeValuePad] > 0.05f
        scoreAnimator.cancel()
        for (other in 0..5) {
            if (other != index) scoreAlpha[other] = 0f
        }
        activeValuePad = index
        scores[index] = pressureScore
        displayedCounts[index] = cumulativeCount
        scoreStartsVisible = keepValuesVisible
        scoreAlpha[index] = if (keepValuesVisible) 1f else 0f
        scoreScale[index] = if (keepValuesVisible) 1.05f else 0.82f
        scoreAnimator.start()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(measuredWidth, (measuredWidth * 0.78f).toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val sx = w / 1254f
        val sy = h / 1254f
        padOutlines.forEachIndexed { index, outline ->
            scaledPadPaths[index].apply {
                reset()
                moveTo(outline[0] * sx, outline[1] * sy)
                for (point in 2 until outline.size step 2) {
                    lineTo(outline[point] * sx, outline[point + 1] * sy)
                }
                close()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        destination.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawBitmap(bitmap, source, destination, bitmapPaint)
        val sx = width / 1254f
        val sy = height / 1254f
        for (index in 0..5) {
            val level = levels[index]
            if (level > 0f) {
                hitPaint.alpha = (level * 255).toInt()
                canvas.drawPath(scaledPadPaths[index], hitPaint)
            }
            if (index == activeValuePad) drawHitValues(canvas, index, sx, sy)
        }
    }

    private fun drawHitValues(canvas: Canvas, index: Int, sx: Float, sy: Float) {
        val alpha = scoreAlpha[index]
        if (alpha <= 0f) return
        val positions = metricPositions[index]
        drawHitValue(canvas, displayedCounts[index].toString(), positions[0] * sx, positions[1] * sy, sx, sy, alpha, scoreScale[index])
        drawHitValue(canvas, scores[index].toString(), positions[2] * sx, positions[3] * sy, sx, sy, alpha, scoreScale[index])
    }

    private fun drawHitValue(canvas: Canvas, text: String, x: Float, y: Float, sx: Float, sy: Float, alpha: Float, scale: Float) {
        scoreTextPaint.textSize = 58f * min(sx, sy * 1.25f)
        val textWidth = scoreTextPaint.measureText(text)
        if (textWidth > 170f * sx) scoreTextPaint.textSize *= (170f * sx) / textWidth
        canvas.save()
        canvas.scale(scale, scale, x, y)
        scoreTextPaint.alpha = (255f * alpha).toInt()
        scoreTextPaint.setShadowLayer(6f * sx, 0f, 2f * sy, Color.argb(215, 5, 26, 27))
        val baseline = y - (scoreTextPaint.fontMetrics.ascent + scoreTextPaint.fontMetrics.descent) / 2f
        canvas.drawText(text, x, baseline, scoreTextPaint)
        scoreTextPaint.clearShadowLayer()
        canvas.restore()
    }

    override fun onDetachedFromWindow() {
        padAnimators.forEach { it.cancel() }
        scoreAnimator.cancel()
        super.onDetachedFromWindow()
    }
}
