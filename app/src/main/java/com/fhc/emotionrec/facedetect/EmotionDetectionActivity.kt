package com.fhc.emotionrec.facedetect

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.util.SparseArray
import com.fhc.emotionrec.facedetect.camera.CameraOverlaySurfaceListener
import com.fhc.emotionrec.facedetect.camera.OverlayGroupView
import com.google.android.gms.vision.*
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.activity_emotion_detection.*
import kotlinx.coroutines.experimental.runBlocking
import kotlin.coroutines.experimental.suspendCoroutine


fun String?.debug(tag: String = "test") {
    Log.d(tag, this)
}

class EmotionDetectionActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_detection)

        face_id_recycler_view.layoutManager = LinearLayoutManager(this)
        val adapter = FaceIdAdapter(object : FaceIdAdapter.Listener {
            override fun onFaceImageClicked(faceImage: FvFaceImage) {
                startActivity(FaceDetail.newIntent(this@EmotionDetectionActivity))
            }

        })
        face_id_recycler_view.adapter = adapter

        val options = FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build()

        val context = applicationContext
        val detector = FirebaseVisionDetectorWrapper(
                FirebaseVision.getInstance()
                        .getVisionFaceDetector(options)
        )

        detector.setProcessor(
                MultiProcessor.Builder<FvFaceImage>(GraphicFaceTrackerFactory(overlay_group_view, adapter))
                        .build()
        )

        val cameraSource = CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .build()

        preview_surface_view.start(CameraOverlaySurfaceListener(cameraSource, overlay_group_view))
    }

    data class FvFaceImage(val firebaseVisionFace: FirebaseVisionFace, val firebaseVisionImage: FirebaseVisionImage, val color: Int)

    class GraphicFaceTrackerFactory(private val overlayGroupView: OverlayGroupView,
                                    private val faceTrackerListener: FaceTrackerListener) : MultiProcessor.Factory<FvFaceImage> {

        interface FaceTrackerListener {
            fun newItem(id: Int, faceImage: FvFaceImage)
            fun onUpdateItem(id: Int, faceImage: FvFaceImage)
            fun onMissingItem(id: Int)
            fun onDestroyItem(id: Int)
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
        var id: Int = 0


        override fun onNewItem(id: Int, faceImage: FvFaceImage?) {
            this.id = id
            "new item".debug("FACE_TRACKER")
            faceImage?.let {
                overlayGroupView.addOverlay(graphicFaceOverlay)
                graphicFaceOverlay.updateFace(faceImage.firebaseVisionFace)
                faceTrackerListener?.newItem(id, faceImage)
            }
        }

        override fun onUpdate(
                detectionResult: Detector.Detections<FvFaceImage>?,
                faceImage: FvFaceImage?
        ) {
            "onUdpate".debug("FACE_TRACKER")
            faceImage?.let {
                graphicFaceOverlay.updateFace(faceImage.firebaseVisionFace)
                faceTrackerListener?.onUpdateItem(id, faceImage)
            }
        }

        override fun onMissing(detectionResult: Detector.Detections<FvFaceImage>?) {
            "onMIssing".debug("FACE_TRACKER")
            overlayGroupView.removeOverlay(graphicFaceOverlay)
            faceTrackerListener?.onMissingItem(id)
        }

        override fun onDone() {
            "onDone".debug("FACE_TRACKER")
            overlayGroupView.removeOverlay(graphicFaceOverlay)
            faceTrackerListener?.onDestroyItem(id)
            faceTrackerListener = null
        }
    }

    class FirebaseVisionDetectorWrapper(private val firebaseVisionFaceDetector: FirebaseVisionFaceDetector) :
            Detector<FvFaceImage>() {

        companion object {
            private val COLOR_CHOICES = intArrayOf(Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED, Color.WHITE, Color.YELLOW)
            private var mCurrentColorIndex = 0
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

                mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.size
                val selectedColor: Int = COLOR_CHOICES[mCurrentColorIndex]
                val sparseArray = SparseArray<FvFaceImage>()
                result?.forEachIndexed { index, fvFace ->
                    Log.d("test", "fvImage!")
                    sparseArray.put(index, FvFaceImage(fvFace, fvImage, selectedColor))
                }
                return sparseArray
            } else {
                return SparseArray()
            }
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