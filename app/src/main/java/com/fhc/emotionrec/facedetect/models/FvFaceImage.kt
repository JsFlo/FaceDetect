package com.fhc.emotionrec.facedetect.models

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import androidx.core.net.toUri
import com.fhc.emotionrec.facedetect.adapter.FaceDetailItem
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.util.*

@Parcelize
data class TrackedFaceImageParcel(
    val uuid: UUID,
    val color: Int,
    val faceImages: List<FvFaceImageParcel>
) :
    Parcelable {

    companion object {
        fun create(
            uuid: UUID,
            color: Int,
            faceDetailItems: List<FaceDetailItem>,
            contentResolver: ContentResolver
        ): TrackedFaceImageParcel {

            return TrackedFaceImageParcel(
                uuid,
                color,
                faceDetailItems.map { FvFaceImageParcel.create(contentResolver, it) })
        }
    }
}

@Parcelize
data class FvFaceImageParcel(
    val smilingProb: Float,
    val leftEyeProb: Float,
    val rightEyeProb: Float,
    val imageBitmapUri: Uri,
    val boundingBox: Rect
) : Parcelable {
    companion object {
        fun imageToUri(contentResolver: ContentResolver, bitmap: Bitmap): Uri {
            val path = Environment.getExternalStorageDirectory().toString()

            val file = File(path, "bitmap_${Date()}.png")
            file.outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            MediaStore.Images.Media.insertImage(
                contentResolver,
                file.absolutePath,
                file.name,
                file.name
            )
            return file.toUri()
        }

        fun create(
            contentResolver: ContentResolver,
            faceDetailItem: FaceDetailItem
        ): FvFaceImageParcel {
            with(faceDetailItem) {
                return FvFaceImageParcel(
                    faceDetailStats.smilingProb,
                    faceDetailStats.leftEyeProb,
                    faceDetailStats.rightEyeProb,
                    imageToUri(contentResolver, image),
                    boundingBox
                )
            }

        }
    }
}

data class FvFaceImage(
    val smilingProb: Float,
    val leftEyeProb: Float,
    val rightEyeProb: Float,
    val imageBitmap: Bitmap,
    val boundingBox: Rect,
    val imageBitmapUri: Uri? = null
) {
    companion object {
        fun create(
            firebaseVisionFace: FirebaseVisionFace,
            firebaseVisionImage: FirebaseVisionImage
        ): FvFaceImage {
            return FvFaceImage(
                firebaseVisionFace.smilingProbability,
                firebaseVisionFace.leftEyeOpenProbability,
                firebaseVisionFace.rightEyeOpenProbability,
                firebaseVisionImage.bitmapForDebugging,
                firebaseVisionFace.boundingBox
            )
        }
    }
}