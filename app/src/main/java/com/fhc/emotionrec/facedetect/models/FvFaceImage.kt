package com.fhc.emotionrec.facedetect.models

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import androidx.core.net.toUri
import com.fhc.emotionrec.facedetect.facecamera.adapter.FaceId
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.util.*

@Parcelize
data class FaceIdParcel(val uuid: UUID, val color: Int, val faceImages: List<FvFaceImageParcel>) :
    Parcelable {
    companion object {
        fun create(faceId: FaceId, contentResolver: ContentResolver): FaceIdParcel {
            return FaceIdParcel(faceId.uuid,
                faceId.color,
                faceId.faceImages.map { FvFaceImageParcel.create(contentResolver, it) })
        }
    }
}

@Parcelize
data class FvFaceImageParcel(
    val smilingProb: Float,
    val leftEyeProb: Float,
    val rightEyeProb: Float,
    val imageBitmapUri: String,
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
            faceImage: FvFaceImage
        ): FvFaceImageParcel {
            with(faceImage) {
                return FvFaceImageParcel(
                    smilingProb,
                    leftEyeProb,
                    rightEyeProb,"",
//                    imageToUri(contentResolver, imageBitmap),
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
    val boundingBox: Rect
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