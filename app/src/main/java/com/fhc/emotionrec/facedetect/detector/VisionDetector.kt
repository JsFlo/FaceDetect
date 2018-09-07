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
 *
 * Class used to detect images and return predictions.
 *
 * Once [release] is called this object will no longer detect images.
 */
class FirebaseVisionDetectorWrapper(private val firebaseVisionFaceDetector: FirebaseVisionFaceDetector) :
        Detector<FvFaceImage>() {

    private var releaseCalled = false

    /**
     * Will return false after [releaseCalled] has been set to true
     */
    override fun isOperational(): Boolean {
        return !releaseCalled
    }

    /**
     * Sets release flag and calls super [release] which releases the detector this wraps.
     */
    override fun release() {
        releaseCalled = true
        super.release()
    }

    /**
     * Main method used to detect.
     *
     * Takes in a [Frame] which represents the image and has a couple ways to get the image data.
     * We convert the frame to a [FirebaseVisionImage] and then wait for the prediction.
     *
     * The [SparseArray] returned will hold all the [FvFaceImage] that were found.
     */
    override fun detect(frame: Frame?): SparseArray<FvFaceImage> {
        return if (frame != null) {
            // convert the frame to a Firebase Vision Image
            val fvImage = frame.toFirebaseVisionImage()

            // block the current thread and try to get a prediction
            val result: List<FirebaseVisionFace>? = runBlocking { firebaseVisionFaceDetector.detectImageSync(fvImage) }

            // The result is a list of [FirebaseVisionFaces] which we will convert to a sparse array of FvFaceImage
            val sparseArray = result?.map { fvFace -> FvFaceImage(fvFace, fvImage) }?.toSparseArray()

            sparseArray ?: SparseArray()
        } else {
            SparseArray()
        }
    }
}

/**
 * Converts a [Frame] to a [FirebaseVisionImage].
 *
 * Uses the [Frame.getGrayscaleImageData] for the image data and [Frame.getMetadata] for the metadata.
 *
 */
private fun Frame.toFirebaseVisionImage(): FirebaseVisionImage {
    return FirebaseVisionImage.fromByteBuffer(
            grayscaleImageData,
            metadata.toFirebaseVisionMetaData()
    )
}

/**
 * Converts a [Frame.Metadata] object to a [FirebaseVisionImageMetadata].
 */
private fun Frame.Metadata.toFirebaseVisionMetaData(): FirebaseVisionImageMetadata {
    return FirebaseVisionImageMetadata.Builder()
            .setWidth(width)
            .setHeight(height)
            .setFormat(format)
            .setRotation(rotation)
            .build()
}

/**
 * Wraps the [FirebaseVisionFaceDetector.detectInImage] async callbacks to behave like a synchronous function
 * to be able to more easily and naturally use it with Kotlin coroutines.
 */
private suspend fun FirebaseVisionFaceDetector.detectImageSync(firebaseVisionImage: FirebaseVisionImage): List<FirebaseVisionFace>? {
    return suspendCoroutine<List<FirebaseVisionFace>> { continuation ->
        detectInImage(firebaseVisionImage)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
    }
}

/**
 * Converts a [List] to a [SparseArray] using the index as the key and the items as the value.
 */
private fun <T> List<T>.toSparseArray(): SparseArray<T> {
    val sparseArray: SparseArray<T> = SparseArray()
    forEachIndexed { index, item ->  sparseArray.put(index, item)}
    return sparseArray
}
