package com.fhc.emotionrec.facedetect

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.fhc.emotionrec.facedetect.adapter.FaceDetailItem
import com.fhc.emotionrec.facedetect.adapter.FaceDetailItemAdapter
import com.fhc.emotionrec.facedetect.detector.FirebaseVisionDetectorWrapper
import com.fhc.emotionrec.facedetect.detector.facetracker.FirebaseVisionFaceTracker
import com.fhc.emotionrec.facedetect.detector.facetracker.facetrackerlisteners.ColorFaceTrackerListener
import com.fhc.emotionrec.facedetect.detector.facetracker.facetrackerlisteners.FaceDetailItemFaceTrackerListener
import com.fhc.emotionrec.facedetect.detector.facetracker.facetrackerlisteners.GraphicFaceOverlayFaceTrackerListener
import com.fhc.emotionrec.facedetect.ui.faceoverlay.OverlayGroupView
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import com.fhc.emotionrec.facedetect.models.TrackedFaceImageParcel
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.activity_emotion_detection.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.util.*

class EmotionDetectionActivity : AppCompatActivity(),
    FaceDetailItemFaceTrackerListener.Listener {

    private var faceTrackerProcessor: MultiProcessor<FvFaceImage>? = null
    private var mlKitFaceDetector: FirebaseVisionFaceDetector? = null
    private var detector: FirebaseVisionDetectorWrapper? = null
    private var cameraSource: CameraSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_detection)

        face_id_recycler_view.layoutManager =
                LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val adapter = FaceDetailItemAdapter()
        face_id_recycler_view.adapter = adapter

        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
            .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()

        mlKitFaceDetector = FirebaseVision.getInstance()
            .getVisionFaceDetector(options)

        faceTrackerProcessor = MultiProcessor.Builder<FvFaceImage>(
            GraphicFaceTrackerFactory(
                overlay_group_view,
                FaceDetailItemFaceTrackerListener(adapter, this, ColorFaceTrackerListener)
            )
        )
            .build()

        preview_surface_view.addListeners(overlay_group_view)
    }

    override fun onFaceDetailClicked(uuid: UUID, faceDetailItems: List<FaceDetailItem>) {
        camera_progress.visibility = View.VISIBLE
        async {
            val faceIdParcel = TrackedFaceImageParcel.create(
                uuid,
                ColorFaceTrackerListener.getColor(uuid),
                faceDetailItems,
                contentResolver
            )
            launch(UI) {
                camera_progress.visibility = View.GONE
                startActivity(
                    FaceDetailActivity.newIntent(
                        this@EmotionDetectionActivity,
                        faceIdParcel
                    )
                )
            }

        }
    }

    override fun onResume() {
        super.onResume()

        if (detector?.isOperational == true && cameraSource != null) {
            preview_surface_view.start(cameraSource!!)
        } else {
            detector =
                    FirebaseVisionDetectorWrapper(
                        mlKitFaceDetector!!
                    )
            detector?.setProcessor(faceTrackerProcessor)

            cameraSource = CameraSource.Builder(this, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(10.0f)
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

class GraphicFaceTrackerFactory(
    private val overlayGroupView: OverlayGroupView,
    private val faceTrackerListener: FirebaseVisionFaceTracker.Listener
) : MultiProcessor.Factory<FvFaceImage> {

    override fun create(faceImage: FvFaceImage?): Tracker<FvFaceImage> {

        return FirebaseVisionFaceTracker(
            faceImage!!,
            ColorFaceTrackerListener,
            GraphicFaceOverlayFaceTrackerListener(
                overlayGroupView,
                ColorFaceTrackerListener
            ),
            faceTrackerListener
        )
    }
}