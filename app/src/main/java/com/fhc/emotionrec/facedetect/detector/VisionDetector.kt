package com.fhc.emotionrec.facedetect.detector

import android.util.SparseArray
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import kotlinx.coroutines.experimental.runBlocking
import kotlin.coroutines.experimental.suspendCoroutine

class FirebaseVisionDetectorWrapper(private val firebaseVisionFaceDetector: FirebaseVisionFaceDetector) :
    Detector<FvFaceImage>() {

    private var releaseCalled = false

    override fun isOperational(): Boolean {
        return !releaseCalled
    }

    override fun release() {
        releaseCalled = true
        super.release()
    }

    override fun detect(frame: Frame?): SparseArray<FvFaceImage> {
        return if (frame != null) {
            val fvImage = frame.toFirebaseVisionImage()
            val result = runBlocking { firebaseVisionFaceDetector.detectImageSync(fvImage) }

            val sparseArray = SparseArray<FvFaceImage>()
            result?.map { fvFace -> FvFaceImage.create(fvFace, fvImage) }
                ?.forEachIndexed { index, fvFaceImage ->
                    sparseArray.put(index, fvFaceImage)
                }
            sparseArray
        } else {
            SparseArray()
        }
    }
}

private fun Frame.toFirebaseVisionImage(): FirebaseVisionImage {
    return FirebaseVisionImage.fromByteBuffer(
        grayscaleImageData,
        metadata.toFirebaseVisionMetaData()
    )
}

private fun Frame.Metadata.toFirebaseVisionMetaData(): FirebaseVisionImageMetadata {
    return FirebaseVisionImageMetadata.Builder()
        .setWidth(width)
        .setHeight(height)
        .setFormat(format)
        .setRotation(rotation)
        .build()
}

private suspend fun FirebaseVisionFaceDetector.detectImageSync(firebaseVisionImage: FirebaseVisionImage): List<FirebaseVisionFace>? {
    return suspendCoroutine<List<FirebaseVisionFace>> { continuation ->
        detectInImage(firebaseVisionImage)
            .addOnSuccessListener { continuation.resume(it) }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }
}
