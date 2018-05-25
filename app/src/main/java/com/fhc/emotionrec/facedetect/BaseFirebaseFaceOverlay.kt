package com.fhc.emotionrec.facedetect

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.fhc.emotionrec.facedetect.camera.Overlay
import com.fhc.emotionrec.facedetect.camera.OverlayTransformations
import com.google.firebase.ml.vision.face.FirebaseVisionFace


abstract class BaseFirebaseFaceOverlay(@Volatile var face: FirebaseVisionFace) : Overlay() {

    private var overlayTransformations: OverlayTransformations? = null
    override fun onCreate(overlayTransformations: OverlayTransformations) {
        this.overlayTransformations = overlayTransformations
    }

    fun updateFace(newFace: FirebaseVisionFace) {
        face = newFace
        overlayTransformations?.postInvalidate()
    }

    override fun draw(canvas: Canvas) {
        overlayTransformations?.let { onDraw(canvas, face, it) }
    }

    abstract fun onDraw(canvas: Canvas, face: FirebaseVisionFace, overlayTransformations: OverlayTransformations)
}

class GraphicFaceOverlay(face: FirebaseVisionFace) : BaseFirebaseFaceOverlay(face) {

    private val facePositionPaint = Paint()
    private val idPaint = Paint()
    private val boxPaint = Paint()

    init {

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.size
        val selectedColor = COLOR_CHOICES[mCurrentColorIndex]

        facePositionPaint.color = selectedColor

        idPaint.color = selectedColor
        idPaint.textSize = ID_TEXT_SIZE

        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH
    }

    override fun onDraw(canvas: Canvas, face: FirebaseVisionFace, overlayTransformations: OverlayTransformations) {
        with(overlayTransformations) {

            val centerX = face.boundingBox.exactCenterX()
            val centerY = face.boundingBox.exactCenterY()

            // Draws a circle at the position of the detected face, with the face's track id below.
            canvas.drawCircle(centerX, centerY,
                    FACE_POSITION_RADIUS, facePositionPaint)

//            canvas.drawText("id: ${face.trackingId}", face.boundingBox.left + ID_X_OFFSET, face.boundingBox.top + ID_Y_OFFSET, idPaint)
//            canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint)
//            canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint)
//            canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET * 2, y - ID_Y_OFFSET * 2, mIdPaint)

//            // Draws a bounding box around the face.
//            val xOffset = scaleX(face.getWidth() / 2.0f)
//            val yOffset = scaleY(face.getHeight() / 2.0f)
//            val left = x - xOffset
//            val top = y - yOffset
//            val right = x + xOffset
//            val bottom = y + yOffset
//            canvas.drawRect(left, top, right, bottom, mBoxPaint)
        }
    }

    companion object {
        private val FACE_POSITION_RADIUS = 10.0f
        private val ID_TEXT_SIZE = 40.0f
        private val ID_Y_OFFSET = 50.0f
        private val ID_X_OFFSET = -50.0f
        private val BOX_STROKE_WIDTH = 5.0f

        private val COLOR_CHOICES = intArrayOf(Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED, Color.WHITE, Color.YELLOW)
        private var mCurrentColorIndex = 0
    }
}