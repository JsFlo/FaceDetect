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
        fun onFaceDetailClicked(uuid: UUID, faceDetailItems: List<FaceDetailItem>)
    }

    private val faceDetailMap = HashMap<UUID, MutableList<FaceDetailItem>>()

    init {
        adapter.listener = this
    }

    override fun onFaceDetailItemClicked(faceDetailItem: FaceDetailItem) {
        faceDetailMap[faceDetailItem.uuid]?.let { faceDetailItems ->
            listener.onFaceDetailClicked(faceDetailItem.uuid, faceDetailItems)
        }
    }

    override fun initItem(uuid: UUID, faceImage: FvFaceImage) {}

    override fun newItem(uuid: UUID, faceImage: FvFaceImage) {
        val faceDetailItem = faceImage.toFaceDetailItem(uuid)
        faceDetailMap[uuid] = mutableListOf(faceDetailItem)
        launch(UI) { adapter.addFaceDetailItem(faceDetailItem) }
    }

    override fun onUpdateItem(uuid: UUID, faceImage: FvFaceImage) {
        val faceDetailItem = faceImage.toFaceDetailItem(uuid)
        addNewImageToFace(uuid, faceDetailItem)
        launch(UI) { adapter.updateFaceDetailItem(faceDetailItem) }
    }

    override fun onMissingItem(uuid: UUID) {}

    override fun onDestroyItem(uuid: UUID) {
        faceDetailMap.remove(uuid)
        async {
            delay(DESTROY_DELAY_SECONDS, TimeUnit.SECONDS)
            launch(UI) { adapter.removeFaceDetailItem(uuid) }
        }
    }

    private fun FvFaceImage.toFaceDetailItem(uuid: UUID): FaceDetailItem {
        return FaceDetailItem(
                uuid, colorFactory.getColor(uuid), imageBitmap, boundingBox,
                FaceDetailStats(
                        smilingProb,
                        leftEyeProb,
                        rightEyeProb
                )
        )
    }

    private fun addNewImageToFace(uuid: UUID, faceDetailItem: FaceDetailItem){
        val listOfFaces =faceDetailMap[uuid]
        if(listOfFaces != null) {
            listOfFaces.add(0, faceDetailItem)
            if(listOfFaces.size > 3) {
                listOfFaces.removeAt(3)
            }
        }
    }

    companion object {
        private const val DESTROY_DELAY_SECONDS = 3L
    }
}