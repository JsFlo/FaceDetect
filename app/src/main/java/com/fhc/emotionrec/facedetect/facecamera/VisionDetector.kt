package com.fhc.emotionrec.facedetect.facecamera

import android.graphics.Color
import android.util.Log
import android.util.SparseArray
import com.fhc.emotionrec.facedetect.facecamera.ui.faceoverlay.GraphicFaceOverlay
import com.fhc.emotionrec.facedetect.facecamera.ui.faceoverlay.OverlayGroupView
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import kotlinx.coroutines.experimental.runBlocking
import java.util.*
import kotlin.coroutines.experimental.suspendCoroutine


class GraphicFaceTrackerFactory(private val overlayGroupView: OverlayGroupView,
                                private val faceTrackerListener: FaceTrackerListener) : MultiProcessor.Factory<FvFaceImage> {

    interface FaceTrackerListener {
        fun newItem(uuid: UUID, faceImage: FvFaceImage)
        fun onUpdateItem(uuid: UUID, faceImage: FvFaceImage)
        fun onMissingItem(uuid: UUID)
        fun onDestroyItem(uuid: UUID)
    }

    override fun create(faceImage: FvFaceImage?): Tracker<FvFaceImage> {
        return FirebaseVisionFaceTracker(
                GraphicFaceOverlay(faceImage!!),
                overlayGroupView, faceTrackerListener)
    }
}

class FirebaseVisionFaceTracker(
        private val graphicFaceOverlay: GraphicFaceOverlay,
        private val overlayGroupView: OverlayGroupView,
        private var faceTrackerListener: GraphicFaceTrackerFactory.FaceTrackerListener?
) :
        Tracker<FvFaceImage>() {
    var uuid: UUID = UUID.randomUUID()


    override fun onNewItem(id: Int, faceImage: FvFaceImage?) {
        "new item".debug("FACE_TRACKER")
        faceImage?.let {
            overlayGroupView.addOverlay(graphicFaceOverlay)
            graphicFaceOverlay.updateFace(faceImage)
            faceTrackerListener?.newItem(uuid, faceImage)
        }
    }

    override fun onUpdate(
            detectionResult: Detector.Detections<FvFaceImage>?,
            faceImage: FvFaceImage?
    ) {
        "onUdpate".debug("FACE_TRACKER")
        faceImage?.let {
            graphicFaceOverlay.updateFace(faceImage)
            faceTrackerListener?.onUpdateItem(uuid, faceImage)
        }
    }

    override fun onMissing(detectionResult: Detector.Detections<FvFaceImage>?) {
        "onMIssing".debug("FACE_TRACKER")
        overlayGroupView.removeOverlay(graphicFaceOverlay)
        faceTrackerListener?.onMissingItem(uuid)
    }

    override fun onDone() {
        "onDone".debug("FACE_TRACKER")
        overlayGroupView.removeOverlay(graphicFaceOverlay)
        faceTrackerListener?.onDestroyItem(uuid)
        faceTrackerListener = null
    }
}

class FirebaseVisionDetectorWrapper(private val firebaseVisionFaceDetector: FirebaseVisionFaceDetector) :
        Detector<FvFaceImage>() {

    private var releaseCalled = false

    companion object {
        private val COLOR_CHOICES = intArrayOf(Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED, Color.WHITE, Color.YELLOW)
        private var mCurrentColorIndex = 0
    }

    override fun isOperational(): Boolean {
        return !releaseCalled
    }

    override fun release() {
        releaseCalled = true
        super.release()
    }

    // TODO: Vision and bitmap
    override fun detect(frame: Frame?): SparseArray<FvFaceImage> {
        Log.d("test", "detect")
        if (frame != null) {
            Log.d("test", "frame not null")
            val fvImage =
                    FirebaseVisionImage.fromByteBuffer(frame.grayscaleImageData, frame.metadata.toFirebaseVisionMetaData())

            val result = runBlocking {
                firebaseVisionFaceDetector.detectImageSync(fvImage)
            }

            // TODO: color out of here
            mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.size
            val selectedColor: Int = COLOR_CHOICES[mCurrentColorIndex]
            val sparseArray = SparseArray<FvFaceImage>()
            result?.forEachIndexed { index, fvFace ->
                Log.d("test", "fvImage!")
                sparseArray.put(index, FvFaceImage.create(fvFace, fvImage, selectedColor))
            }
            return sparseArray
        } else {
            return SparseArray()
        }
    }
}

fun Frame.Metadata.toFirebaseVisionMetaData(): FirebaseVisionImageMetadata {
    return FirebaseVisionImageMetadata.Builder()
            .setWidth(width)
            .setHeight(height)
//                    .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setFormat(format)
            .setRotation(rotation)
            .build()
}

suspend fun FirebaseVisionFaceDetector.detectImageSync(firebaseVisionImage: FirebaseVisionImage): List<FirebaseVisionFace>? {
    return suspendCoroutine<List<FirebaseVisionFace>> { continuation ->
        detectInImage(firebaseVisionImage)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
    }
}
