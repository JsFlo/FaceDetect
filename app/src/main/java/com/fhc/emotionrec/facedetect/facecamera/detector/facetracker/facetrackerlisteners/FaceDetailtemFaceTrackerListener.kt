package com.fhc.emotionrec.facedetect.facecamera.detector.facetracker.facetrackerlisteners

import com.fhc.emotionrec.facedetect.adapter.FaceDetailItem
import com.fhc.emotionrec.facedetect.adapter.FaceDetailItemAdapter
import com.fhc.emotionrec.facedetect.facecamera.detector.facetracker.FirebaseVisionFaceTracker
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import com.fhc.emotionrec.facedetect.ui.FaceDetailStats
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.*
import java.util.concurrent.TimeUnit

class FaceDetailItemFaceTrackerListener(
    val adapter: FaceDetailItemAdapter,
    val listener: Listener,
    val colorFactory: ColorFactory
) :
    FirebaseVisionFaceTracker.Listener, FaceDetailItemAdapter.Listener {
    interface Listener {
        fun onFaceDetailClicked(name: String)
    }

    init {
        adapter.listener = this
    }

    override fun onFaceDetailItemClicked(faceDetailItem: FaceDetailItem) {
        listener.onFaceDetailClicked("bleh")
    }

    private fun FvFaceImage.toFaceDetailItem(uuid: UUID): FaceDetailItem {
        return FaceDetailItem(
            uuid, colorFactory.getColor(uuid), imageBitmap,
            FaceDetailStats(
                smilingProb,
                leftEyeProb,
                rightEyeProb
            )
        )
    }

    override fun initItem(uuid: UUID, faceImage: FvFaceImage) {}

    override fun newItem(uuid: UUID, faceImage: FvFaceImage) {
        launch(UI) { adapter.addFaceDetailItem(faceImage.toFaceDetailItem(uuid)) }
    }

    override fun onUpdateItem(uuid: UUID, faceImage: FvFaceImage) {
        launch(UI) { adapter.updateFaceDetailItem(faceImage.toFaceDetailItem(uuid)) }
    }

    override fun onMissingItem(uuid: UUID) {}

    override fun onDestroyItem(uuid: UUID) {
        async {
            delay(DESTROY_DELAY_SECONDS, TimeUnit.SECONDS)
            launch(UI) { adapter.removeFaceDetailItem(uuid) }
        }
    }

    companion object {
        private const val DESTROY_DELAY_SECONDS = 3L
    }
}