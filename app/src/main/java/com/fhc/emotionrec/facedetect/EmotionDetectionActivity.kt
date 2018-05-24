package com.fhc.emotionrec.facedetect

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.emotionrec.emotionrecapp.utils.ImageFileResult
import com.emotionrec.emotionrecapp.utils.getImageFile
import com.emotionrec.emotionrecapp.utils.setImage
import com.fhc.emotionrec.facedetect.facetracker.camera.CameraSourcePreview
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.activity_emotion_detection.*
import java.io.File


fun String?.debug() {
    Log.d("test", this)
}

class EmotionDetectionActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2
    }

    private var imagePath: String? = null

    private lateinit var options: FirebaseVisionFaceDetectorOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_detection)
        take_pic_fab.setOnClickListener {
            createImageFileAndStartImageRequest()
        }
        options = FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build()
    }

    private var lastResult: ImageFileResult.FileCreated? = null

    private fun createImageFileAndStartImageRequest() {
        val result = getImageFile(this, packageManager, applicationContext.packageName)
        when (result) {
            is ImageFileResult.WritePermissionMissing -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE)
            is ImageFileResult.NoAppHandler -> Toast.makeText(this, "No app installed to take pictures", Toast.LENGTH_LONG).show()
            is ImageFileResult.FileCreated -> {
                imagePath = result.file?.absolutePath
                lastResult = result
                startActivityForResult(result.intent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    createImageFileAndStartImageRequest()
                }
            }
            else -> {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK && imagePath != null) {
            lastResult?.let {
                onImageFileTaken(it.file!!, imagePath!!)
            }
        }
    }

    private fun onImageFileTaken(file: File, imagePath: String) {
        take_pic_fab.enable(false)

        val bitmap = face_view.setImage(imagePath)
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options)

        detector.detectInImage(image)
                .addOnSuccessListener {
                    "eyy".debug()
                    it.map { "smiling prob: ${it.smilingProbability},".debug() }
                    take_pic_fab.enable(true)
                }
                .addOnFailureListener {
                    take_pic_fab.enable(true)
                    "Err ${it.message}".debug()
                }
    }

    private fun FloatingActionButton.enable(enable: Boolean) {
        isEnabled = enable
        visibility = if (enable) View.VISIBLE else View.GONE
    }

}
