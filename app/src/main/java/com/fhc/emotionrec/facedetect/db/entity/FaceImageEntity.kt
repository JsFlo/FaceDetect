package com.fhc.emotionrec.facedetect.db.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Environment
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import java.io.File
import java.io.FileOutputStream
import java.util.*


@Entity
data class FaceImageEntity(@PrimaryKey val uuid: UUID,
                           val color: Int,
                           val smilingProb: Float,
                           val leftEyeProb: Float,
                           val rightEyeProb: Float,
                           val boundingBox: Rect,
                           val imagePath: String,
                           val active: Boolean = true)


fun FvFaceImage.toFaceImageEntity(uuid: UUID, color: Int): FaceImageEntity {
    return FaceImageEntity(uuid, color,
            fvFace.smilingProbability,
            fvFace.leftEyeOpenProbability,
            fvFace.rightEyeOpenProbability,
            fvFace.boundingBox,
            fvImage.bitmapForDebugging.toFilePath())
}

fun FvFaceImage.toFaceImageEntity(uuid: UUID): FaceImageEntity {
    return FaceImageEntity(uuid, 0,
            fvFace.smilingProbability,
            fvFace.leftEyeOpenProbability,
            fvFace.rightEyeOpenProbability,
            fvFace.boundingBox,
            fvImage.bitmapForDebugging.toFilePath())
}

private fun Bitmap.toFilePath(): String {
    val file_path = Environment.getExternalStorageDirectory().absolutePath + "/faceimages"
    val dir = File(file_path)
    if (!dir.exists())
        dir.mkdirs()

    val file = File(dir, "faceimages" + System.currentTimeMillis() + ".png")
    val fOut = FileOutputStream(file)

    this.compress(Bitmap.CompressFormat.PNG, 100, fOut)
    fOut.flush()
    fOut.close()
    return file.absoluteFile.toString()
}
