package com.fhc.emotionrec.facedetect.detector.facetracker.facetrackerlisteners

import android.graphics.Color
import com.fhc.emotionrec.facedetect.detector.facetracker.FirebaseVisionFaceTracker
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import java.util.*


interface ColorFactory {
    fun getColor(uuid: UUID): Int
}

object ColorFaceTrackerListener : ColorFactory,
    FirebaseVisionFaceTracker.Listener {
    private val COLOR_CHOICES = intArrayOf(
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.MAGENTA,
        Color.RED,
        Color.WHITE,
        Color.YELLOW
    )
    private var mCurrentColorIndex = 0

    private val colorMap = HashMap<UUID, Int>()
    override fun getColor(uuid: UUID): Int {
        return colorMap[uuid] ?: 0
    }

    override fun initItem(uuid: UUID, faceImage: FvFaceImage) {}

    override fun newItem(uuid: UUID, faceImage: FvFaceImage) {
        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.size
        val selectedColor: Int = COLOR_CHOICES[mCurrentColorIndex]
        colorMap[uuid] = selectedColor
    }

    override fun onUpdateItem(uuid: UUID, faceImage: FvFaceImage) {}

    override fun onMissingItem(uuid: UUID) {}

    override fun onDestroyItem(uuid: UUID) {
        colorMap.remove(uuid)
    }
}
