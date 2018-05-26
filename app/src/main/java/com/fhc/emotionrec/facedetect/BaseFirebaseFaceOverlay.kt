package com.fhc.emotionrec.facedetect

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.withTranslation
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

class GraphicFaceOverlay(faceImage: EmotionDetectionActivity.FvFaceImage) : BaseFirebaseFaceOverlay(faceImage.firebaseVisionFace) {

    private val facePositionPaint = Paint()
    private val idPaint = Paint()
    private val boxPaint = Paint()

    init {
        val selectedColor = faceImage.color
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

            canvas.drawCircle(translateX(centerX), translateY(centerY),
                    FACE_POSITION_RADIUS, facePositionPaint)

//            canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint)
//            canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint)
//            canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET * 2, y - ID_Y_OFFSET * 2, mIdPaint)
            canvas.drawCircle(translateX(centerX), translateY(centerY),face.boundingBox.left.toFloat() - translateX(centerX), boxPaint)
//            canvas.drawRect(translateX(face.boundingBox.left.toFloat()),
//                    translateY(face.boundingBox.top.toFloat()),
//                    translateX(face.boundingBox.right.toFloat()),
//                    translateY(face.boundingBox.bottom.toFloat()), boxPaint)
        }
    }

    companion object {
        private val FACE_POSITION_RADIUS = 10.0f
        private val ID_TEXT_SIZE = 40.0f
        private val ID_Y_OFFSET = 50.0f
        private val ID_X_OFFSET = -50.0f
        private val BOX_STROKE_WIDTH = 5.0f
    }
}