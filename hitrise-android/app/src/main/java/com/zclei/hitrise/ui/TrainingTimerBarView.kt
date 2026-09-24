package com.zclei.hitrise.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

class TrainingTimerBarView(context: Context) : View(context) {
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#DFF8F2") }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2E75B6") }
    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.LEFT; isFakeBoldText = true }
    private val captionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.RIGHT }
    private val track = RectF()
    private var progress = 1f
    private var centerText = "00:00"
    private var captionText = ""
    private var centerColor = Color.BLACK

    fun setTimerState(progressFraction: Float, center: String, caption: String, color: Int) {
        progress = progressFraction.coerceIn(0f, 1f)
        centerText = center
        captionText = caption
        progressPaint.color = color
        timePaint.color = if (color == Color.TRANSPARENT) centerColor else color
        invalidate()
    }

    fun setPalette(trackColor: Int, captionColor: Int, centerColor: Int) {
        trackPaint.color = trackColor
        captionPaint.color = captionColor
        this.centerColor = centerColor
        timePaint.color = centerColor
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val left = 4f * density
        val right = width - left
        timePaint.textSize = 24f * density
        captionPaint.textSize = 12f * density
        val baseline = 27f * density
        canvas.drawText(centerText, left, baseline, timePaint)
        canvas.drawText(captionText, right, baseline - 3f * density, captionPaint)
        val top = height - 19f * density
        val bottom = height - 7f * density
        val radius = (bottom - top) / 2f
        track.set(left, top, right, bottom)
        canvas.drawRoundRect(track, radius, radius, trackPaint)
        if (progress > 0f) {
            val fillRight = left + (right - left) * progress
            track.set(left, top, fillRight.coerceAtLeast(left + 1f), bottom)
            canvas.drawRoundRect(track, radius, radius, progressPaint)
        }
    }
}
