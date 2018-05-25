package com.fhc.emotionrec.facedetect

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Camera
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.Toast
import com.emotionrec.emotionrecapp.utils.ImageFileResult
import com.emotionrec.emotionrecapp.utils.getImageFile
import com.emotionrec.emotionrecapp.utils.setImage
import com.fhc.emotionrec.facedetect.camera.CameraOverlaySurfaceListener
import com.fhc.emotionrec.facedetect.camera.OverlayGroupView
import com.google.android.gms.vision.*
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.activity_emotion_detection.*
import java.io.File
import android.R.attr.rotation
import com.google.android.gms.tasks.Continuation
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlin.coroutines.experimental.suspendCoroutine


fun String?.debug() {
    Log.d("test", this)
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

        detector.setProcessor(
            MultiProcessor.Builder<FirebaseVisionFace>(GraphicFaceTrackerFactory(overlay_group_view))
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

    //
//    private var lastResult: ImageFileResult.FileCreated? = null
//
//    private fun createImageFileAndStartImageRequest() {
//        val result = getImageFile(this, packageManager, applicationContext.packageName)
//        when (result) {
//            is ImageFileResult.WritePermissionMissing -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE)
//            is ImageFileResult.NoAppHandler -> Toast.makeText(this, "No app installed to take pictures", Toast.LENGTH_LONG).show()
//            is ImageFileResult.FileCreated -> {
//                imagePath = result.file?.absolutePath
//                lastResult = result
//                startActivityForResult(result.intent, REQUEST_IMAGE_CAPTURE)
//            }
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int,
//                                            permissions: Array<String>, grantResults: IntArray) {
//        when (requestCode) {
//            REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE -> {
//                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    createImageFileAndStartImageRequest()
//                }
//            }
//            else -> {
//            }
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK && imagePath != null) {
//            lastResult?.let {
//                onImageFileTaken(it.file!!, imagePath!!)
//            }
//        }
//    }
//
//    private fun onImageFileTaken(file: File, imagePath: String) {
//        take_pic_fab.enable(false)
//
//        val bitmap = face_view.setImage(imagePath)
//        val image = FirebaseVisionImage.fromBitmap(bitmap)
//
//        val detector = FirebaseVision.getInstance()
//                .getVisionFaceDetector(options)
//
//        detector.detectInImage(image)
//                .addOnSuccessListener {
//                    "eyy".debug()
//                    it.map { "smiling prob: ${it.smilingProbability},".debug() }
//                    take_pic_fab.enable(true)
//                }
//                .addOnFailureListener {
//                    take_pic_fab.enable(true)
//                    "Err ${it.message}".debug()
//                }
//    }
//
//    private fun FloatingActionButton.enable(enable: Boolean) {
//        isEnabled = enable
//        visibility = if (enable) View.VISIBLE else View.GONE
//    }
    class GraphicFaceTrackerFactory(val overlayGroupView: OverlayGroupView) :
        MultiProcessor.Factory<FirebaseVisionFace> {
        override fun create(face: FirebaseVisionFace?): Tracker<FirebaseVisionFace> {
            return FirebaseVisionFaceTracker(GraphicFaceOverlay(face!!), overlayGroupView)
        }
    }

    class FirebaseVisionFaceTracker(
        val graphicFaceOverlay: GraphicFaceOverlay,
        val overlayGroupView: OverlayGroupView
    ) :
        Tracker<FirebaseVisionFace>() {

        override fun onNewItem(id: Int, face: FirebaseVisionFace?) {
            face?.let {
                graphicFaceOverlay.updateFace(face)
            }
        }


        override fun onUpdate(
            detectionResult: Detector.Detections<FirebaseVisionFace>?,
            face: FirebaseVisionFace?
        ) {
            face?.let {
                overlayGroupView.addOverlay(graphicFaceOverlay)
                graphicFaceOverlay.updateFace(face)
            }
        }

        override fun onMissing(detectionResult: Detector.Detections<FirebaseVisionFace>?) {
            overlayGroupView.removeOverlay(graphicFaceOverlay)
        }

        override fun onDone() {
            overlayGroupView.removeOverlay(graphicFaceOverlay)
        }
    }

    class FirebaseVisionDetectorWrapper(private val firebaseVisionFaceDetector: FirebaseVisionFaceDetector) :
        Detector<FirebaseVisionFace>() {

        override fun detect(frame: Frame?): SparseArray<FirebaseVisionFace> {
            Log.d("test", "detect")
            if (frame != null) {
                Log.d("test", "frame not null")
                val metadata = FirebaseVisionImageMetadata.Builder()
                    .setWidth(frame.metadata.width)
                    .setHeight(frame.metadata.height)
//                    .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                    .setFormat(frame.metadata.format)
                    .setRotation(frame.metadata.rotation)
                    .build()
                val image =
                    FirebaseVisionImage.fromByteBuffer(frame.grayscaleImageData, metadata)

                val result = runBlocking {
                    firebaseVisionFaceDetector.detectImageSync(image)
                }

                val sparseArray = SparseArray<FirebaseVisionFace>()
                result?.forEachIndexed { index, firebaseVisionFace ->
                    Log.d("test", "image!")
                    sparseArray.put(index, firebaseVisionFace)
                }
                return sparseArray
            } else {
                return SparseArray()
            }
        }
    }
}

suspend fun FirebaseVisionFaceDetector.detectImageSync(firebaseVisionImage: FirebaseVisionImage): List<FirebaseVisionFace>? {
    return suspendCoroutine<List<FirebaseVisionFace>> { continuation ->
        detectInImage(firebaseVisionImage)
            .addOnSuccessListener { continuation.resume(it) }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }
}