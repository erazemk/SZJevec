package eu.erazem.szjevec

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: GestureRecognizerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private var isCameraFacingFront = false

    fun setCameraFacingFront(isFront: Boolean) {
        isCameraFacingFront = isFront
        invalidate()
    }

    init {
        initPaints()
    }

    private fun initPaints() {
        linePaint.color = ContextCompat.getColor(context!!, R.color.color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { gestureRecognizerResult ->
            for (landmark in gestureRecognizerResult.landmarks()) {
                for (normalizedLandmark in landmark) {
                    val x = if (isCameraFacingFront) {
                        (1 - normalizedLandmark.x()) * imageWidth * scaleFactor
                    } else {
                        normalizedLandmark.x() * imageWidth * scaleFactor
                    }
                    val y = normalizedLandmark.y() * imageHeight * scaleFactor
                    canvas.drawPoint(x, y, pointPaint)
                }

                HandLandmarker.HAND_CONNECTIONS.forEach {
                    val startX = if (!isCameraFacingFront) {
                        gestureRecognizerResult.landmarks()[0][it!!.start()].x() * imageWidth * scaleFactor
                    } else {
                        (1 - gestureRecognizerResult.landmarks()[0][it!!.start()].x()) * imageWidth * scaleFactor
                    }

                    val endX = if (!isCameraFacingFront) {
                        gestureRecognizerResult.landmarks()[0][it.end()].x() * imageWidth * scaleFactor
                    } else {
                        (1 - gestureRecognizerResult.landmarks()[0][it.end()].x()) * imageWidth * scaleFactor
                    }

                    val startY =
                        gestureRecognizerResult.landmarks()[0][it.start()].y() * imageHeight * scaleFactor
                    val endY =
                        gestureRecognizerResult.landmarks()[0][it.end()].y() * imageHeight * scaleFactor

                    canvas.drawLine(startX, startY, endX, endY, linePaint)
                }
            }
        }
    }

    fun setResults(
        gestureRecognizerResult: GestureRecognizerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = gestureRecognizerResult

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }

            RunningMode.LIVE_STREAM -> {
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8F
    }
}
