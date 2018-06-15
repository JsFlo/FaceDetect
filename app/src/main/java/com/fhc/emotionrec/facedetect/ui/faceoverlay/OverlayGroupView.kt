package com.fhc.emotionrec.facedetect.ui.faceoverlay

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.fhc.emotionrec.facedetect.ui.camera.CameraInfo
import com.fhc.emotionrec.facedetect.ui.camera.PreviewSurfaceListener
import com.google.android.gms.vision.CameraSource

interface OverlayTransformations {
    fun scaleX(horizontal: Float): Float
    fun scaleY(vertical: Float): Float
    fun translateX(x: Float): Float
    fun translateY(y: Float): Float
    fun postInvalidate()
}

abstract class Overlay {
    abstract fun onCreate(overlayTransformations: OverlayTransformations)
    abstract fun draw(canvas: Canvas)
    abstract fun clear()
    abstract fun destroy()
}

class OverlayGroupView(context: Context, attrs: AttributeSet?) : View(context, attrs),
    OverlayTransformations, PreviewSurfaceListener {

    private val lock = Object()
    private var previewCameraInfo: CameraInfo? = null
    private var widthScaleFactor = 1.0f
    private var heightScaleFactor = 1.0f

    private val overlays = HashSet<Overlay>()

    private fun syncAndInvalidate(invalidate: Boolean = true, synced: () -> Unit) {
        synchronized(lock, synced)
        if (invalidate) postInvalidate()
    }

    override fun clear() {
        overlays.forEach { it.clear() }
        syncAndInvalidate { overlays.clear() }
    }

    override fun onCameraInfo(cameraInfo: CameraInfo) {
        syncAndInvalidate { previewCameraInfo = cameraInfo }
    }

    override fun stop() {
        clear()
    }

    fun addOverlay(overlay: Overlay) {
        syncAndInvalidate {
            overlay.onCreate(this)
            overlays.add(overlay)
        }
    }

    fun removeOverlay(overlay: Overlay) {
        syncAndInvalidate { overlays.remove(overlay) }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        syncAndInvalidate(false) {
            canvas?.let {
                updateScaleFactors(previewCameraInfo, canvas)
                overlays.forEach { it.draw(canvas) }
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

    override fun translateX(x: Float): Float {
        return if (previewCameraInfo?.facing == CameraSource.CAMERA_FACING_FRONT) {
            width - scaleX(x)
        } else {
            scaleX(x)
        }
    }

    override fun translateY(y: Float): Float = scaleY(y)

}