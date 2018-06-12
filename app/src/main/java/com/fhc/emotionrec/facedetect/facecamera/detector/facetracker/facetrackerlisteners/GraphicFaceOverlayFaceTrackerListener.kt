package com.fhc.emotionrec.facedetect.facecamera.detector.facetracker.facetrackerlisteners

import com.fhc.emotionrec.facedetect.facecamera.detector.facetracker.FirebaseVisionFaceTracker
import com.fhc.emotionrec.facedetect.facecamera.ui.faceoverlay.GraphicFaceOverlay
import com.fhc.emotionrec.facedetect.facecamera.ui.faceoverlay.OverlayGroupView
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import java.util.*

class GraphicFaceOverlayFaceTrackerListener(
    private val overlayGroupView: OverlayGroupView,
    private val colorFactory: ColorFactory
) : FirebaseVisionFaceTracker.Listener {

    private var graphicFaceOverlay: GraphicFaceOverlay? = null
    override fun initItem(uuid: UUID, faceImage: FvFaceImage) {

    }

    override fun newItem(uuid: UUID, faceImage: FvFaceImage) {
        graphicFaceOverlay = GraphicFaceOverlay(faceImage, colorFactory.getColor(uuid))
            .also { overlayGroupView.addOverlay(it) }
    }

    override fun onUpdateItem(uuid: UUID, faceImage: FvFaceImage) {
        graphicFaceOverlay?.updateFace(faceImage)
    }

    override fun onMissingItem(uuid: UUID) {
//        graphicFaceOverlay?.updateFace(faceImage)
    }

    override fun onDestroyItem(uuid: UUID) {
        if (graphicFaceOverlay != null) {
            overlayGroupView.removeOverlay(graphicFaceOverlay!!)
            graphicFaceOverlay?.destroy()
            graphicFaceOverlay = null
        }
    }

}
