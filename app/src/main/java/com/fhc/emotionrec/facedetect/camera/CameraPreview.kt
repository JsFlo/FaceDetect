package com.fhc.emotionrec.facedetect.camera

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.google.android.gms.vision.CameraSource

data class CameraInfo(val width: Int, val height: Int, val facing: Int)

interface PreviewSurfaceListener {
    fun onHolderReady(holder: SurfaceHolder)
    fun stop()
}

class CameraOverlaySurfaceListener(
    private val cameraSource: CameraSource,
    private val overlay: OverlayGroupView?
) :
        PreviewSurfaceListener {

    @SuppressLint("MissingPermission")
    override fun onHolderReady(holder: SurfaceHolder) {
        cameraSource.start(holder)
        overlay?.let {
            val size = cameraSource.previewSize
            val (min, max) = listOf(size.width, size.height).sorted().let { Pair(it[0], it[1]) }
            overlay.setCameraInfo(CameraInfo(min, max, cameraSource.cameraFacing))
            overlay.clear()
        }
    }

    override fun stop() {
        cameraSource.stop()
    }

}

interface PreviewCameraSurface {
    fun start(previewSurfaceListener: PreviewSurfaceListener)
    fun stop()
}

class PreviewSurfaceView(context: Context, attrs: AttributeSet?) :
    SurfaceView(context, attrs),
    SurfaceHolder.Callback, PreviewCameraSurface {

    private var surfaceAvailable = false
    private var startRequested = false
    private var previewListener: PreviewSurfaceListener? = null

    init {
        holder.addCallback(this)
    }

    override fun start(previewSurfaceListener: PreviewSurfaceListener) {
        stop()
        startRequested = true
        previewListener = previewSurfaceListener
        startRequest()
    }

    override fun stop() {
        previewListener?.stop()
    }

    private fun startRequest() {
        if (startRequested && surfaceAvailable) {
            previewListener?.onHolderReady(holder)
            startRequested = false
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        surfaceAvailable = true
        startRequest()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        surfaceAvailable = false
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}
}