package com.fhc.emotionrec.facedetect.ui.camera

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.google.android.gms.vision.CameraSource
import kotlinx.coroutines.experimental.async

data class CameraInfo(val width: Int, val height: Int, val facing: Int)

interface PreviewSurfaceListener {
    fun onCameraInfo(cameraInfo: CameraInfo)
    fun clear()
    fun stop()
}

interface CameraSurface {
    fun start(cameraSource: CameraSource)
    fun addListeners(vararg previewSurfaceListeners: PreviewSurfaceListener)
    fun clear()
    fun stop()
}

class PreviewCameraSurface(context: Context, attrs: AttributeSet?) :
        SurfaceView(context, attrs),
        SurfaceHolder.Callback, CameraSurface {


    private var surfaceAvailable = false
    private var startRequested = false
    private var previewListeners: List<PreviewSurfaceListener>? = null
    private var cameraSource: CameraSource? = null

    init {
        holder.addCallback(this)
    }

    @SuppressLint("MissingPermission")
    private fun startRequest() {
        if (startRequested && surfaceAvailable && cameraSource != null) {
            cameraSource?.start(holder)
            cameraSource?.let {
                val size = it.previewSize
                val (min, max) = listOf(size.width, size.height).sorted().let { Pair(it[0], it[1]) }
                val cameraInfo =
                    CameraInfo(min, max, it.cameraFacing)
                previewListeners?.forEach { it.onCameraInfo(cameraInfo) }
            }
            startRequested = false
        }
    }

    override fun addListeners(vararg previewSurfaceListeners: PreviewSurfaceListener) {
        previewListeners = previewSurfaceListeners.toList()
    }

    override fun start(cameraSource: CameraSource) {
        stop()
        startRequested = true
        this.cameraSource = cameraSource
        startRequest()
    }

    override fun stop() {
        cameraSource?.let { async { it.release() } }
        previewListeners?.forEach { it.stop() }
    }

    fun pause() {
        cameraSource?.let { async { it.stop() } }
        previewListeners?.forEach { it.stop() }
    }

    override fun clear() {
        previewListeners?.forEach { it.clear() }
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