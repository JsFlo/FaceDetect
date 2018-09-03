package com.fhc.emotionrec.facedetect.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.graphics.Rect
import com.fhc.emotionrec.facedetect.utils.toFilePath
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import java.util.*


data class FvFaceImage(val fvFace: FirebaseVisionFace, val fvImage: FirebaseVisionImage)


@Entity
data class FaceImageEntity(@PrimaryKey val uuid: UUID,
                           val color: Int,
                           val smilingProb: Float,
                           val leftEyeProb: Float,
                           val rightEyeProb: Float,
                           val boundingBox: Rect,
                           val imagePath: String,
                           val active: Boolean = true)


fun FvFaceImage.toFaceImageEntity(uuid: UUID, color: Int=0): FaceImageEntity {
    return FaceImageEntity(uuid, color,
            fvFace.smilingProbability,
            fvFace.leftEyeOpenProbability,
            fvFace.rightEyeOpenProbability,
            fvFace.boundingBox,
            fvImage.bitmapForDebugging.toFilePath())
}