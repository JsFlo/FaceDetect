package com.fhc.emotionrec.facedetect.detector.facetracker

import com.fhc.emotionrec.facedetect.models.FvFaceImage
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Tracker
import java.util.*


class FirebaseVisionFaceTracker(private vararg val listeners: Listener) : Tracker<FvFaceImage>() {

    interface Listener {
        fun newItem(uuid: UUID, faceImage: FvFaceImage)
        fun onUpdateItem(uuid: UUID, faceImage: FvFaceImage)
        fun onDestroyItem(uuid: UUID)
    }

    var uuid: UUID = UUID.randomUUID()

    override fun onNewItem(id: Int, faceImage: FvFaceImage?) {
        faceImage?.let { listeners.forEach { it.newItem(uuid, faceImage) } }
    }

    override fun onUpdate(detRes: Detector.Detections<FvFaceImage>?, faceImage: FvFaceImage?) {
        faceImage?.let { listeners.forEach { it.onUpdateItem(uuid, faceImage) } }
    }

    override fun onMissing(detectionResult: Detector.Detections<FvFaceImage>?) {}

    override fun onDone() {
        listeners.forEach { it.onDestroyItem(uuid) }
    }
}