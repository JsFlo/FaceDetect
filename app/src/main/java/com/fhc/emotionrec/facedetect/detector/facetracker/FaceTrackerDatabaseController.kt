package com.fhc.emotionrec.facedetect.detector.facetracker

import android.app.Application
import android.graphics.Color
import com.fhc.emotionrec.facedetect.db.dao.FaceImageDao
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import com.fhc.emotionrec.facedetect.models.toFaceImageEntity
import kotlinx.coroutines.experimental.launch
import java.util.*

class FaceTrackerDatabaseController(private val appContext: Application,
                                    private val faceImageDao: FaceImageDao) : FirebaseVisionFaceTracker.Listener {

    private var stopUpdates = false
    private val COLOR_CHOICES = intArrayOf(
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    )

    override fun newItem(uuid: UUID, faceImage: FvFaceImage) {
        if (!stopUpdates) {
            addFaceImageToDb(uuid, faceImage, COLOR_CHOICES.toList().random() ?: 0)
        }
    }

    override fun onUpdateItem(uuid: UUID, faceImage: FvFaceImage) {
        if (!stopUpdates) {
            addFaceImageToDb(uuid, faceImage, COLOR_CHOICES.toList().random() ?: 0)
        }
    }

    override fun onDestroyItem(uuid: UUID) {
        if (!stopUpdates) {
            deleteFaceImageFromDb(uuid)
        }
    }

    private fun addFaceImageToDb(uuid: UUID, fvFaceImage: FvFaceImage, color: Int) {
        launch {
            faceImageDao.update(fvFaceImage.toFaceImageEntity(uuid, color))
        }
    }

    private fun updateFaceImageToDb(uuid: UUID, faceImage: FvFaceImage) {
        launch { faceImageDao.update(faceImage.toFaceImageEntity(uuid)) }
    }

    private fun deleteFaceImageFromDb(uuid: UUID) {
        launch {
            faceImageDao.deActivateFaceImage(uuid)
        }
    }


    /**
     * Returns a random element.
     */
    fun <T> List<T>.random(): T? = if (size > 0) get(Random().nextInt(size)) else null


}