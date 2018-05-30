package com.fhc.emotionrec.facedetect.models

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import androidx.core.net.toUri
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.util.*

@Parcelize
data class FvFaceImageParcel(val smilingProb: Float,
                             val leftEyeProb: Float,
                             val rightEyeProb: Float,
                             val imageBitmapUri: Uri,
                             val boundingBox: Rect,
                             val color: Int) : Parcelable {
    companion object {
        fun imageToUri(contentResolver: ContentResolver, bitmap: Bitmap): Uri {
            val path = Environment.getExternalStorageDirectory().toString()

            val file = File(path, "bitmap_${Date()}.png")
            file.outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)
            }
            MediaStore.Images.Media.insertImage(contentResolver, file.absolutePath, file.name, file.name)
            return file.toUri()
        }

        fun create(contentResolver: ContentResolver, faceImage: FvFaceImage): FvFaceImageParcel {
            with(faceImage) {
                return FvFaceImageParcel(smilingProb,
                        leftEyeProb,
                        rightEyeProb,
                        imageToUri(contentResolver, imageBitmap),
                        boundingBox,
                        color)
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
        val color: Int) {
    companion object {
        fun create(firebaseVisionFace: FirebaseVisionFace, firebaseVisionImage: FirebaseVisionImage, color: Int): FvFaceImage {
            return FvFaceImage(firebaseVisionFace.smilingProbability,
                    firebaseVisionFace.leftEyeOpenProbability,
                    firebaseVisionFace.rightEyeOpenProbability,
                    firebaseVisionImage.bitmapForDebugging,
                    firebaseVisionFace.boundingBox,
                    color)
        }
    }
}