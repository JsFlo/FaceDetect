package com.fhc.emotionrec.facedetect.facedetail

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import kotlinx.android.synthetic.main.activity_face_detail.*
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.facedetail.adapter.FaceImageAdapter
import com.fhc.emotionrec.facedetect.facedetail.network.predictionServiceApi
import com.fhc.emotionrec.facedetect.facedetail.network.uploadImage
import com.fhc.emotionrec.facedetect.models.FaceIdParcel
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.io.File


class FaceDetailActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context, faceIdParcel: FaceIdParcel): Intent {

            val intent = Intent(context, FaceDetailActivity::class.java)
            intent.putExtra("faceIdParcel", faceIdParcel)
            return intent
        }
    }

    private fun Uri.getBitmap(contentResolver: ContentResolver): Bitmap {
        return MediaStore.Images.Media.getBitmap(contentResolver, this)
    }

    private var faceIdParcel: FaceIdParcel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detail)
        faceIdParcel = intent.getParcelableExtra("faceIdParcel") as FaceIdParcel

        async {
            val faceImages = faceIdParcel?.faceImages?.map { FvFaceImage(it.smilingProb, it.leftEyeProb, it.rightEyeProb, it.imageBitmapUri.getBitmap(contentResolver), it.boundingBox, it.imageBitmapUri) }
            faceImages?.let { faceImages ->
                launch(UI) {
                    val firstFaceImage = faceImages[0]
                    onHeroInfo(firstFaceImage)

                    face_detail_face_id_recycler_view.adapter = FaceImageAdapter(faceImages)
                    face_detail_face_id_recycler_view.layoutManager = LinearLayoutManager(this@FaceDetailActivity)
                }
            }
        }
    }

    private fun onHeroInfo(faceImage: FvFaceImage) {
//        face_details_stats_view.setFaceDetailStats(
//                FvFaceImage(
//                        faceImage.smilingProb,
//                        faceImage.leftEyeProb, faceImage.rightEyeProb, faceImage.imageBitmap,
//                        faceImage.boundingBox
//                ))

        // File(faceImage.imageBitmapUri.path

    }

    private fun getPredictionResponse(file: File) {
        face_details_emotion_view.visibility = View.GONE
        async {
            val result = predictionServiceApi.uploadImage(file).execute()
            if (result != null && result.isSuccessful) {
                Log.d("test", "on")
                launch(UI) {
                    face_details_emotion_view.visibility = View.VISIBLE
                    face_details_emotion_view.setPredictionResponse(result.body()!!)
                }
            } else {
                Log.d("test", "else")
            }
        }
    }
}
