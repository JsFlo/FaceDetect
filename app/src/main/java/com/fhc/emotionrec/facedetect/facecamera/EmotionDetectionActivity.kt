package com.fhc.emotionrec.facedetect.facecamera

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.fhc.emotionrec.facedetect.facedetail.FaceDetailActivity
import com.fhc.emotionrec.facedetect.facecamera.adapter.FaceIdAdapter
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.facecamera.adapter.FaceId
import com.fhc.emotionrec.facedetect.models.FaceIdParcel
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import com.google.android.gms.vision.*
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions

import kotlinx.android.synthetic.main.activity_emotion_detection.*


fun String?.debug(tag: String = "test") {
    Log.d(tag, this)
}

class EmotionDetectionActivity : AppCompatActivity() {

    private var faceTrackerProcessor: MultiProcessor<FvFaceImage>? = null

    private var mlKitFaceDetector: FirebaseVisionFaceDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_detection)

        face_id_recycler_view.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val adapter = FaceIdAdapter(object : FaceIdAdapter.Listener {
            override fun onFaceImageClicked(faceId: FaceId) {
                startActivity(FaceDetailActivity.newIntent(this@EmotionDetectionActivity, FaceIdParcel.create(faceId, contentResolver)))
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