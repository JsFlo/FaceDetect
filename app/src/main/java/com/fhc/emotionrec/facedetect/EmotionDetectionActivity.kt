package com.fhc.emotionrec.facedetect

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

    private var imagePath: String? = null

    private lateinit var options: FirebaseVisionFaceDetectorOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_detection)
        options = FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build()

        createCameraSource()

//        take_pic_fab.setOnClickListener {
//            createImageFileAndStartImageRequest()
//        }
    }

    private var mCameraSource: CameraSource? = null

    private fun createCameraSource() {

        val context = applicationContext
        val detector = FirebaseVisionDetectorWrapper(
                FirebaseVision.getInstance()
                        .getVisionFaceDetector(options)
        )

        face_id_recycler_view.layoutManager = LinearLayoutManager(this)
        val adapter = FaceIdAdapter()
        face_id_recycler_view.adapter = adapter

        detector.setProcessor(
                MultiProcessor.Builder<VisionFaceImage>(GraphicFaceTrackerFactory(overlay_group_view, adapter))
                        .build()
        )

        mCameraSource = CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .build()

        preview_surface_view.start(
                CameraOverlaySurfaceListener(
                        mCameraSource!!,
                        overlay_group_view
                )
        )
    }

    data class VisionFaceImage(val firebaseVisionImage: FirebaseVisionImage, val firebaseVisionFace: FirebaseVisionFace)


    class GraphicFaceTrackerFactory(private val overlayGroupView: OverlayGroupView,
                                    private val faceTrackerListener: FaceTrackerListener) :
            MultiProcessor.Factory<VisionFaceImage> {

        interface FaceTrackerListener {
            fun newItem(id: Int, faceImage: VisionFaceImage)
            fun onUpdateItem(id: Int, faceImage: VisionFaceImage)
            fun onMissingItem(id: Int)
            fun onDestroyItem(id: Int)
        }

        override fun create(faceImage: VisionFaceImage?): Tracker<VisionFaceImage> {
            return FirebaseVisionFaceTracker(GraphicFaceOverlay(faceImage!!.firebaseVisionFace), overlayGroupView, faceTrackerListener)
        }
    }

    class FirebaseVisionFaceTracker(
            private val graphicFaceOverlay: GraphicFaceOverlay,
            private val overlayGroupView: OverlayGroupView,
            private var faceTrackerListener: GraphicFaceTrackerFactory.FaceTrackerListener?
    ) :
            Tracker<VisionFaceImage>() {
        var id: Int = 0


        override fun onNewItem(id: Int, faceImage: VisionFaceImage?) {
            this.id = id
            "new item".debug("FACE_TRACKER")
            faceImage?.let {
                overlayGroupView.addOverlay(graphicFaceOverlay)
                graphicFaceOverlay.updateFace(faceImage.firebaseVisionFace)
                faceTrackerListener?.newItem(id, faceImage)
            }
        }

        override fun onUpdate(
                detectionResult: Detector.Detections<VisionFaceImage>?,
                faceImage: VisionFaceImage?
        ) {
            "onUdpate".debug("FACE_TRACKER")
            faceImage?.let {
                graphicFaceOverlay.updateFace(faceImage.firebaseVisionFace)
                faceTrackerListener?.onUpdateItem(id, faceImage)
            }
        }

        override fun onMissing(detectionResult: Detector.Detections<VisionFaceImage>?) {
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
            Detector<VisionFaceImage>() {
        // TODO: Vision and bitmap
        override fun detect(frame: Frame?): SparseArray<VisionFaceImage> {
            Log.d("test", "detect")
            if (frame != null) {
                Log.d("test", "frame not null")
                val image =
                        FirebaseVisionImage.fromByteBuffer(frame.grayscaleImageData, frame.metadata.toFirebaseVisionMetaData())

                val result = runBlocking {
                    firebaseVisionFaceDetector.detectImageSync(image)
                }

                val sparseArray = SparseArray<VisionFaceImage>()
                result?.forEachIndexed { index, firebaseVisionFace ->
                    Log.d("test", "image!")
                    sparseArray.put(index, VisionFaceImage(image, firebaseVisionFace))
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