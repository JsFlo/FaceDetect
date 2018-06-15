package com.fhc.emotionrec.facedetect.detector.facetracker

import com.fhc.emotionrec.facedetect.models.FvFaceImage
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Tracker
import java.util.*


class FirebaseVisionFaceTracker(
    initFaceImage: FvFaceImage,
    private vararg val listeners: Listener
) : Tracker<FvFaceImage>() {

    interface Listener {
        fun initItem(uuid: UUID, faceImage: FvFaceImage)
        fun newItem(uuid: UUID, faceImage: FvFaceImage)
        fun onUpdateItem(uuid: UUID, faceImage: FvFaceImage)
        fun onMissingItem(uuid: UUID)
        fun onDestroyItem(uuid: UUID)
    }

    var uuid: UUID = UUID.randomUUID()

    init {
        initItem(initFaceImage)
    }

    private fun initItem(faceImage: FvFaceImage?) {
        faceImage?.let { listeners.forEach { it.initItem(uuid, faceImage) } }
    }


    override fun onNewItem(id: Int, faceImage: FvFaceImage?) {
        faceImage?.let { listeners.forEach { it.newItem(uuid, faceImage) } }
    }

    override fun onUpdate(detRes: Detector.Detections<FvFaceImage>?, faceImage: FvFaceImage?) {
        faceImage?.let { listeners.forEach { it.onUpdateItem(uuid, faceImage) } }
    }

    override fun onMissing(detectionResult: Detector.Detections<FvFaceImage>?) {
        listeners.forEach { it.onMissingItem(uuid) }
    }

    override fun onDone() {
        listeners.forEach { it.onDestroyItem(uuid) }
    }
}