package com.fhc.emotionrec.facedetect

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.emotionrec.emotionrecapp.utils.setImage
import com.fhc.emotionrec.facedetect.db.FaceImageDb
import com.fhc.emotionrec.facedetect.models.FaceImageEntity
import com.fhc.emotionrec.facedetect.ui.FaceDetailStats
import kotlinx.android.synthetic.main.activity_face_detail.*

class FaceDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_UUID = "EXTRA_UUID"
        fun newIntent(context: Context, uuid: String): Intent {

            val intent = Intent(context, FaceDetailActivity::class.java)
            intent.putExtra(EXTRA_UUID, uuid)
            return intent
        }
    }

    private var uuidExtra: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detail)

        uuidExtra = intent.getStringExtra(EXTRA_UUID)
        val dao = FaceImageDb.getInstance(this)!!.faceImageDao()

        dao.getLiveDataFaceImageEntityWithUuid(uuidExtra).observe(this, android.arch.lifecycle.Observer {
            onHeroInfo(it)
        })
    }

    private fun onHeroInfo(faceImage: FaceImageEntity?) {
        faceImage?.let {
            face_details_stats_view.setFaceDetailStats(FaceDetailStats(faceImage.smilingProb, faceImage.leftEyeProb, faceImage.rightEyeProb))
            face_detail_hero_image.setImage(faceImage.imagePath)
        }
    }

    private fun Uri.getBitmap(contentResolver: ContentResolver): Bitmap {
        return MediaStore.Images.Media.getBitmap(contentResolver, this)
    }

//    private fun getPredictionResponse(file: File) {
//        face_details_emotion_view.visibility = View.GONE
//        async {
//            val result = predictionServiceApi.uploadImage(file).execute()
//            if (result != null && result.isSuccessful) {
//                Log.d("test", "on")
//                launch(UI) {
//                    face_details_emotion_view.visibility = View.VISIBLE
//                    face_details_emotion_view.setPredictionResponse(result.body()!!)
//                }
//            } else {
//                Log.d("test", "else")
//            }
//        }
//    }
}
