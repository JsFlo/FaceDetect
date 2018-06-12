package com.fhc.emotionrec.facedetect.facecamera

import android.graphics.Color
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
import kotlin.collections.HashMap
import kotlin.coroutines.experimental.suspendCoroutine

interface ColorFactory {
    fun getColor(uuid: UUID): Int
}

object ColorController : ColorFactory, FirebaseVisionFaceTracker.Listener {
    private val COLOR_CHOICES = intArrayOf(
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    )
    private var mCurrentColorIndex = 0

    private val colorMap = HashMap<UUID, Int>()
    override fun getColor(uuid: UUID): Int {
        return colorMap[uuid] ?: 0
    }

    override fun initItem(uuid: UUID, faceImage: FvFaceImage) {}

    override fun newItem(uuid: UUID, faceImage: FvFaceImage) {
        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.size
        val selectedColor: Int = COLOR_CHOICES[mCurrentColorIndex]
        colorMap[uuid] = selectedColor
    }

    override fun onUpdateItem(uuid: UUID, faceImage: FvFaceImage) {}

    override fun onMissingItem(uuid: UUID) {}

    override fun onDestroyItem(uuid: UUID) {
        colorMap.remove(uuid)
    }
}

class GraphicFaceOverlayController(private val overlayGroupView: OverlayGroupView, private val colorFactory: ColorFactory)
    : FirebaseVisionFaceTracker.Listener {

    private var graphicFaceOverlay: GraphicFaceOverlay? = null
    override fun initItem(uuid: UUID, faceImage: FvFaceImage) {

    }

    override fun newItem(uuid: UUID, faceImage: FvFaceImage) {
        graphicFaceOverlay = GraphicFaceOverlay(faceImage, colorFactory.getColor(uuid))
                .also { overlayGroupView.addOverlay(it) }
    }

    override fun onUpdateItem(uuid: UUID, faceImage: FvFaceImage) {
        graphicFaceOverlay?.updateFace(faceImage)
    }

    override fun onMissingItem(uuid: UUID) {
//        graphicFaceOverlay?.updateFace(faceImage)
    }

    override fun onDestroyItem(uuid: UUID) {
        if (graphicFaceOverlay != null) {
            overlayGroupView.removeOverlay(graphicFaceOverlay!!)
            graphicFaceOverlay?.destroy()
            graphicFaceOverlay = null
        }
    }

}

class GraphicFaceTrackerFactory(
        private val overlayGroupView: OverlayGroupView,
        private val faceTrackerListener: FirebaseVisionFaceTracker.Listener
) : MultiProcessor.Factory<FvFaceImage> {


    override fun create(faceImage: FvFaceImage?): Tracker<FvFaceImage> {

        return FirebaseVisionFaceTracker(faceImage!!,
                ColorController,
                GraphicFaceOverlayController(overlayGroupView, ColorController),
                faceTrackerListener)
    }
}


class FirebaseVisionFaceTracker(initFaceImage: FvFaceImage, private vararg val listeners: Listener) : Tracker<FvFaceImage>() {

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

class FirebaseVisionDetectorWrapper(private val firebaseVisionFaceDetector: FirebaseVisionFaceDetector) :
        Detector<FvFaceImage>() {

    private var releaseCalled = false

    override fun isOperational(): Boolean {
        return !releaseCalled
    }

    override fun release() {
        releaseCalled = true
        super.release()
    }

    override fun detect(frame: Frame?): SparseArray<FvFaceImage> {
        return if (frame != null) {
            val fvImage = frame.toFirebaseVisionImage()
            val result = runBlocking { firebaseVisionFaceDetector.detectImageSync(fvImage) }

            val sparseArray = SparseArray<FvFaceImage>()
            result?.map { fvFace -> FvFaceImage.create(fvFace, fvImage) }
                    ?.forEachIndexed { index, fvFaceImage ->
                        sparseArray.put(index, fvFaceImage)
                    }
            sparseArray
        } else {
            SparseArray()
        }
    }
}

private fun Frame.toFirebaseVisionImage(): FirebaseVisionImage {
    return FirebaseVisionImage.fromByteBuffer(
            grayscaleImageData,
            metadata.toFirebaseVisionMetaData()
    )
}

private fun Frame.Metadata.toFirebaseVisionMetaData(): FirebaseVisionImageMetadata {
    return FirebaseVisionImageMetadata.Builder()
            .setWidth(width)
            .setHeight(height)
            .setFormat(format)
            .setRotation(rotation)
            .build()
}

private suspend fun FirebaseVisionFaceDetector.detectImageSync(firebaseVisionImage: FirebaseVisionImage): List<FirebaseVisionFace>? {
    return suspendCoroutine<List<FirebaseVisionFace>> { continuation ->
        detectInImage(firebaseVisionImage)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
    }
}
