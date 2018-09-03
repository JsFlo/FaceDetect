package com.fhc.emotionrec.facedetect

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.fhc.emotionrec.facedetect.adapter.FaceDetailItemAdapter
import com.fhc.emotionrec.facedetect.db.FaceImageDb
import com.fhc.emotionrec.facedetect.db.entity.FaceImageEntity
import com.fhc.emotionrec.facedetect.detector.FirebaseVisionDetectorWrapper
import com.fhc.emotionrec.facedetect.detector.facetracker.FaceTrackerDatabaseController
import com.fhc.emotionrec.facedetect.detector.facetracker.FirebaseVisionFaceTracker
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import com.fhc.emotionrec.facedetect.ui.faceoverlay.GraphicFaceOverlay
import com.fhc.emotionrec.facedetect.ui.faceoverlay.OverlayGroupView
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.activity_emotion_detection.*

class EmotionDetectionActivity : AppCompatActivity() {

    private var faceTrackerProcessor: MultiProcessor<FvFaceImage>? = null
    private var mlKitFaceDetector: FirebaseVisionFaceDetector? = null
    private var detector: FirebaseVisionDetectorWrapper? = null
    private var cameraSource: CameraSource? = null
    private var launchingDetail = false
    private var launchedDetail = false

//    private var faceTrackerAdapterController: FaceDetailItemFaceTrackerListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_detection)

        val faceImageDao = FaceImageDb.getInstance(this)!!.faceImageDao()

        // setup recycler view
        with(face_id_recycler_view) {
            layoutManager = LinearLayoutManager(this@EmotionDetectionActivity, RecyclerView.HORIZONTAL, false)
            val faceDetailItemAdapter = FaceDetailItemAdapter()
            adapter = faceDetailItemAdapter
        }

        // get an instance of the new face detector
        val options = FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build()
        mlKitFaceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options)

//        faceTrackerAdapterController =FaceDetailItemFaceTrackerListener(this, adapter, this)
        // face tracker that adds to db
        faceTrackerProcessor = MultiProcessor.Builder<FvFaceImage>(DetectorFaceTracker(FaceTrackerDatabaseController(application,
                faceImageDao)))
                .build()

        preview_surface_view.addListeners(overlay_group_view)

        faceImageDao.getAllFaceImages().observe(this, Observer {
            onNewFaceImages(it, overlay_group_view)
        })
    }

    private fun onNewFaceImages(faceImages: List<FaceImageEntity>?, overlayGroupView: OverlayGroupView) {

        // image adapter


        // graphic overlays
        overlayGroupView.clear()
        val graphiFaceOverlays = faceImages?.map { GraphicFaceOverlay(it) } ?: mutableListOf()
        overlayGroupView.addOverlays(*graphiFaceOverlays.toTypedArray())
    }
//
//    override fun onFaceDetailClicked(uuid: UUID, faceDetailItems: List<FaceDetailItem>) {
//        Log.d("test", "stop it")
//        preview_surface_view.stop()
//        launchingDetail = true
//        camera_progress.visibility = View.VISIBLE
//        async {
//            Log.d("test", "about to parcel")
//            val faceIdParcel = TrackedFaceImageParcel.create(
//                    uuid,
//                    ColorFaceTrackerListener.getColor(uuid),
//                    faceDetailItems,
//                    contentResolver
//            )
//            Log.d("test", "after parcel")
//            launch(UI) {
//                Log.d("test", "launch ui")
//                camera_progress.visibility = View.GONE
//                Log.d("test", "start activity")
//                startActivity(
//                        FaceDetailActivity.newIntent(
//                                this@EmotionDetectionActivity,
//                                faceIdParcel
//                        )
//                )
//                launchingDetail = false
//                launchedDetail = true
//            }
//
//        }
//    }

    override fun onResume() {
        super.onResume()
        if (!launchingDetail) {

            if (launchedDetail) {
//                faceTrackerAdapterController?.clearAndResumeUpdates()
                launchedDetail = false
            }

            if (detector?.isOperational == true && cameraSource != null) {
                preview_surface_view.start(cameraSource!!)
            } else {
                detector = FirebaseVisionDetectorWrapper(mlKitFaceDetector!!)
                detector?.setProcessor(faceTrackerProcessor)

                cameraSource = CameraSource.Builder(this, detector)
                        .setRequestedPreviewSize(640, 480)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedFps(10.0f)
                        .build()
                preview_surface_view.start(cameraSource!!)
            }
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

class DetectorFaceTracker(private val faceTrackerListener: FirebaseVisionFaceTracker.Listener) : MultiProcessor.Factory<FvFaceImage> {

    override fun create(faceImage: FvFaceImage?): Tracker<FvFaceImage> {

        return FirebaseVisionFaceTracker(faceTrackerListener)
    }
}