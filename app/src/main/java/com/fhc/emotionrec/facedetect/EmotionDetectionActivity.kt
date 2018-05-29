package com.fhc.emotionrec.facedetect

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.util.SparseArray
import androidx.core.net.toUri
import com.fhc.emotionrec.facedetect.camera.CameraOverlaySurfaceListener
import com.fhc.emotionrec.facedetect.camera.OverlayGroupView
import com.google.android.gms.vision.*
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.parcel.Parcelize

import kotlinx.android.synthetic.main.activity_emotion_detection.*
import kotlinx.coroutines.experimental.runBlocking
import java.io.File
import java.util.*
import kotlin.coroutines.experimental.suspendCoroutine


fun String?.debug(tag: String = "test") {
    Log.d(tag, this)
}

class EmotionDetectionActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2
    }

    private var graphicListener: CameraOverlaySurfaceListener? = null

    private var faceTrackerProcessor: MultiProcessor<FvFaceImage>? = null

    private var mlKitFaceDetector: FirebaseVisionFaceDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_detection)

        face_id_recycler_view.layoutManager = LinearLayoutManager(this)
        val adapter = FaceIdAdapter(object : FaceIdAdapter.Listener {
            override fun onFaceImageClicked(faceImage: FvFaceImage) {
                startActivity(FaceDetailActivity.newIntent(this@EmotionDetectionActivity, FvFaceImageParcel.create(contentResolver, faceImage)))
            }

        })
        face_id_recycler_view.adapter = adapter

        val options = FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build()

        mlKitFaceDetector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options)
        faceTrackerProcessor = MultiProcessor.Builder<FvFaceImage>(GraphicFaceTrackerFactory(overlay_group_view, adapter))
                .build()

    }

    private var detector: FirebaseVisionDetectorWrapper? = null

    override fun onResume() {
        super.onResume()

        if (detector?.isOperational == true && graphicListener != null) {
            preview_surface_view.start(graphicListener!!)
        } else {
            detector = FirebaseVisionDetectorWrapper(mlKitFaceDetector!!)
            detector?.setProcessor(faceTrackerProcessor)

            val cameraSource = CameraSource.Builder(this, detector)
                    .setRequestedPreviewSize(640, 480)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedFps(30.0f)
                    .build()

            graphicListener = CameraOverlaySurfaceListener(cameraSource, overlay_group_view)
            preview_surface_view.start(graphicListener!!)
        }
    }

    override fun onPause() {
        super.onPause()
        preview_surface_view.stop()
    }


    @Parcelize
    data class FvFaceImageParcel(val smilingProb: Float,
                                 val leftEyeProb: Float,
                                 val rightEyeProb: Float,
                                 val imageBitmapUri: Uri,
                                 val boundingBox: Rect,
                                 val color: Int) : Parcelable {
        companion object {
            fun imageToUri(contentResolver: ContentResolver, bitmap: Bitmap): Uri {
                val path = Environment.getExternalStorageDirectory().toString()

                val file = File(path, "bitmap_${Date()}.png")
                file.outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)
                }
                MediaStore.Images.Media.insertImage(contentResolver, file.absolutePath, file.name, file.name)
                return file.toUri()
            }

            fun create(contentResolver: ContentResolver, faceImage: FvFaceImage): FvFaceImageParcel {
                with(faceImage) {

                    //                    val tmpFile = createTempFile()
//                    val out = tmpFile.outputStream()
//                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
//                    out.close()

                    return FvFaceImageParcel(smilingProb,
                            leftEyeProb,
                            rightEyeProb,
                            imageToUri(contentResolver, imageBitmap),
                            boundingBox,
                            color)
                }

            }
        }
    }

    data class FvFaceImage(
            val smilingProb: Float,
            val leftEyeProb: Float,
            val rightEyeProb: Float,
            val imageBitmap: Bitmap,
            val boundingBox: Rect,
            val color: Int) {
        companion object {
            fun create(firebaseVisionFace: FirebaseVisionFace, firebaseVisionImage: FirebaseVisionImage, color: Int): FvFaceImage {
                return FvFaceImage(firebaseVisionFace.smilingProbability,
                        firebaseVisionFace.leftEyeOpenProbability,
                        firebaseVisionFace.rightEyeOpenProbability,
                        firebaseVisionImage.bitmapForDebugging,
                        firebaseVisionFace.boundingBox,
                        color)
            }
        }
    }
//            val firebaseVisionFace: FirebaseVisionFace, val firebaseVisionImage: FirebaseVisionImage, val color: Int) : Parcelable

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
                graphicFaceOverlay.updateFace(faceImage)
                faceTrackerListener?.newItem(id, faceImage)
            }
        }

        override fun onUpdate(
                detectionResult: Detector.Detections<FvFaceImage>?,
                faceImage: FvFaceImage?
        ) {
            "onUdpate".debug("FACE_TRACKER")
            faceImage?.let {
                graphicFaceOverlay.updateFace(faceImage)
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