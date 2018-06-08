package com.fhc.emotionrec.facedetect.facedetail

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_face_detail.*
import android.provider.MediaStore
import android.util.Log
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.facedetail.network.predictionServiceApi
import com.fhc.emotionrec.facedetect.facedetail.network.uploadImage
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import com.fhc.emotionrec.facedetect.models.FvFaceImageParcel
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.io.File


class FaceDetailActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context, faceImageParcel: FvFaceImageParcel): Intent {

            val intent = Intent(context, FaceDetailActivity::class.java)
            intent.putExtra("faceImageParcel", faceImageParcel)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detail)
        val faceImageParcel = intent.getParcelableExtra("faceImageParcel") as FvFaceImageParcel
        val bitmap =
                MediaStore.Images.Media.getBitmap(this.contentResolver, faceImageParcel.imageBitmapUri)

        face_details_stats_view.setFaceImage(
                FvFaceImage(
                        faceImageParcel.smilingProb,
                        faceImageParcel.leftEyeProb, faceImageParcel.rightEyeProb, bitmap,
                        faceImageParcel.boundingBox, faceImageParcel.color
                )
        )

        face_detail_hero_image.setImageBitmap(bitmap)

//        face_details_emotion_view.setPredictionResponse("ajrajkr")
        getPredictionResponse(File(faceImageParcel.imageBitmapUri.path))

    }

    private fun getPredictionResponse(file: File) {
        async {
            launch(UI) {
                face_details_emotion_view.setPredictionResponse("about to make call")
            }
            val result = predictionServiceApi.uploadImage(file).execute()
            launch(UI) {
                face_details_emotion_view.setPredictionResponse("after execute")
            }
            if (result != null && result.isSuccessful) {
                Log.d("test", "on")
                launch(UI) {
                    face_details_emotion_view.setPredictionResponse(result.body()!!.toString())
                }
            } else {
                Log.d("test", "else")
                launch(UI) {
                    face_details_emotion_view.setPredictionResponse("Err")
                }
            }
        }
    }
}
