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

/**
 * Wraps the [FiresbaseVisionFaceDetector] from the new ml.vision package in a [Detector] object from the
 * old gms.vision package so we can use the [CameraSource] and capture a controlled stream of frames and predictions.
 */
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
            // convert the frame to a Firebase Vision Image
            val fvImage = frame.toFirebaseVisionImage()
            // block the current thread and try to get a prediction
            val result = runBlocking { firebaseVisionFaceDetector.detectImageSync(fvImage) }

            // The result is a list of [FirebaseVisionFaces] which we will convert to FvFaceImage
            val sparseArray = SparseArray<FvFaceImage>()
            result?.map { fvFace -> FvFaceImage(fvFace, fvImage) }?.forEachIndexed { index, fvFaceImage ->
                sparseArray.put(index, fvFaceImage)
            }
            sparseArray
        } else {
            SparseArray()
        }
    }
}

// using fromByteBuffer with grayscaleImageData but could use bitmap
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
