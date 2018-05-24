package com.fhc.emotionrec.facedetect

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.google.android.gms.vision.CameraSource

class OverlayGroupView(context: Context, attrs: AttributeSet?) : View(context, attrs),
    OverlayTransformations {

    private val lock = Object()
    private var previewCameraInfo: CameraInfo? = null
    private var widthScaleFactor = 1.0f
    private var heightScaleFactor = 1.0f

    private val overlays = HashSet<Overlay>()

    private fun sync(invalidate: Boolean = true, synced: () -> Unit) {
        synchronized(lock, synced)
        if (invalidate) postInvalidate()
    }

    fun clear() {
        sync { overlays.clear() }
    }

    fun addOverlay(overlay: Overlay) {
        sync { overlays.add(overlay) }
    }

    fun removeOverlay(overlay: Overlay) {
        sync { overlays.remove(overlay) }
    }

    fun setCameraInfo(cameraInfo: CameraInfo) {
        sync { previewCameraInfo = cameraInfo }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        sync(false) {
            canvas?.let {
                updateScaleFactors(previewCameraInfo, canvas)
                overlays.forEach { it.draw(canvas, this as OverlayTransformations) }
            }
        }
    }

    private fun updateScaleFactors(cameraInfo: CameraInfo?, canvas: Canvas) {
        cameraInfo?.let {
            if (it.width != 0 && it.height != 0) {
                widthScaleFactor = canvas.width.toFloat() / it.width.toFloat()
                heightScaleFactor = canvas.height.toFloat() / it.height.toFloat()
            }
        }
    }

    override fun scaleX(horizontal: Float): Float = horizontal * widthScaleFactor

    override fun scaleY(vertical: Float): Float = vertical * heightScaleFactor

    override fun translateX(x: Float, overlayWidth: Float): Float {
        return if (previewCameraInfo?.facing == CameraSource.CAMERA_FACING_FRONT) {
            overlayWidth - scaleX(x)
        } else {
            scaleX(x)
        }
    }

    override fun translateY(y: Float, overlayHeight: Float): Float = scaleY(y)

}

interface OverlayTransformations {
    fun scaleX(horizontal: Float): Float
    fun scaleY(vertical: Float): Float
    fun translateX(x: Float, overlayWidth: Float): Float
    fun translateY(y: Float, overlayHeight: Float): Float
    fun postInvalidate()
}

abstract class Overlay {
    abstract fun draw(canvas: Canvas, overlayTransformations: OverlayTransformations)
}