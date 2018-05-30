package com.fhc.emotionrec.facedetect.facecamera

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
import com.fhc.emotionrec.facedetect.facedetail.FaceDetailActivity
import com.fhc.emotionrec.facedetect.facedetail.adapter.FaceIdAdapter
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.facecamera.ui.faceoverlay.GraphicFaceOverlay
import com.fhc.emotionrec.facedetect.facecamera.ui.faceoverlay.OverlayGroupView
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import com.fhc.emotionrec.facedetect.models.FvFaceImageParcel
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

        preview_surface_view.addListeners(overlay_group_view)
    }

    private var detector: FirebaseVisionDetectorWrapper? = null

    private var cameraSource: CameraSource? = null

    override fun onResume() {
        super.onResume()

        if (detector?.isOperational == true && cameraSource != null) {
            preview_surface_view.start(cameraSource!!)
        } else {
            detector = FirebaseVisionDetectorWrapper(mlKitFaceDetector!!)
            detector?.setProcessor(faceTrackerProcessor)

            cameraSource = CameraSource.Builder(this, detector)
                    .setRequestedPreviewSize(640, 480)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedFps(30.0f)
                    .build()
            preview_surface_view.start(cameraSource!!)
        }
    }

    override fun onPause() {
        super.onPause()
        preview_surface_view.pause()
    }

    override fun onStop() {
        super.onStop()
        preview_surface_view.stop()
    }

}