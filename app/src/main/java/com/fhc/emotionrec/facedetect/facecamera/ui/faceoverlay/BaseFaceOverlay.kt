package com.fhc.emotionrec.facedetect.facecamera.ui.faceoverlay

import android.graphics.Canvas
import android.graphics.Paint
import com.fhc.emotionrec.facedetect.models.FvFaceImage


abstract class BaseFaceOverlay(@Volatile var face: FvFaceImage) : Overlay() {

    private var overlayTransformations: OverlayTransformations? = null
    override fun onCreate(overlayTransformations: OverlayTransformations) {
        this.overlayTransformations = overlayTransformations
    }

    fun updateFace(newFace: FvFaceImage) {
        face = newFace
        overlayTransformations?.postInvalidate()
    }

    override fun draw(canvas: Canvas) {
        overlayTransformations?.let { onDraw(canvas, face, it) }
    }

    abstract fun onDraw(canvas: Canvas, face: FvFaceImage, overlayTransformations: OverlayTransformations)
}

class GraphicFaceOverlay(faceImage: FvFaceImage, val selectedColor: Int) : BaseFaceOverlay(faceImage) {

    private val facePositionPaint = Paint()
    private val boxPaint = Paint()

    init {
        facePositionPaint.color = selectedColor

        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH
    }

    private var translatedCenterX: Float = 0f
    private var translatedCenterY: Float = 0f

    override fun onDraw(canvas: Canvas, face: FvFaceImage, overlayTransformations: OverlayTransformations) {
        with(overlayTransformations) {

            val centerX = face.boundingBox.exactCenterX()
            val centerY = face.boundingBox.exactCenterY()

            translatedCenterX = translateX(centerX)
            translatedCenterY = translateY(centerY)

            canvas.drawCircle(translatedCenterX, translatedCenterY,
                    FACE_POSITION_RADIUS, facePositionPaint)

            canvas.drawCircle(translatedCenterX, translatedCenterY, translatedCenterX - translateX(face.boundingBox.left.toFloat()), boxPaint)

//            canvas.drawRect(translateX(face.boundingBox.left.toFloat()),
//                    translateY(face.boundingBox.top.toFloat()),
//                    translateX(face.boundingBox.right.toFloat()),
//                    translateY(face.boundingBox.bottom.toFloat()), boxPaint)
        }
    }

    override fun clear() {
        //
    }

    companion object {
        private val FACE_POSITION_RADIUS = 10.0f
        private val BOX_STROKE_WIDTH = 5.0f
    }
}