package com.fhc.emotionrec.facedetect.facedetail

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_face_detail.*
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.facedetail.adapter.FaceImageAdapter
import com.fhc.emotionrec.facedetect.facedetail.network.predictionServiceApi
import com.fhc.emotionrec.facedetect.facedetail.network.uploadImage
import com.fhc.emotionrec.facedetect.models.FaceIdParcel
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detail)
        val faceIdParcel = intent.getParcelableExtra("faceIdParcel") as FaceIdParcel

//        val bitmap =
//                MediaStore.Images.Media.getBitmap(this.contentResolver, faceIdParcel.faceImages[0].imageBitmapUri)

//        face_details_stats_view.setFaceImage(
//                FvFaceImage(
//                    faceIdParcel.smilingProb,
//                        faceImageParcel.leftEyeProb, faceImageParcel.rightEyeProb, bitmap,
//                        faceImageParcel.boundingBox, faceImageParcel.color
//                )
//        )

//        face_detail_hero_image.setImageBitmap(bitmap)
        face_detail_face_id_recycler_view.adapter = FaceImageAdapter(faceIdParcel.faceImages)
        face_detail_face_id_recycler_view.layoutManager = LinearLayoutManager(this)


//        face_details_emotion_view.setPredictionResponse("ajrajkr")
//        getPredictionResponse(File(faceImageParcel.imageBitmapUri.path))

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
