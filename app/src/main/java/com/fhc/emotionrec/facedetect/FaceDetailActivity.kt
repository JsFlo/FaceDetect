package com.fhc.emotionrec.facedetect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.fhc.emotionrec.facedetect.utils.setImage
import com.fhc.emotionrec.facedetect.db.FaceImageDb
import com.fhc.emotionrec.facedetect.models.FaceImageEntity
import com.fhc.emotionrec.facedetect.network.predictionServiceApi
import com.fhc.emotionrec.facedetect.network.uploadImage
import kotlinx.android.synthetic.main.activity_face_detail.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.File

/**
 * Takes in a uuid [EXTRA_UUID] and shows the most recent image and makes a call to
 * get the emotion predictions.
 */
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
            face_details_stats_view.setFaceDetailStats(faceImage.smilingProb, faceImage.leftEyeProb, faceImage.rightEyeProb)
            face_detail_hero_image.setImage(faceImage.imagePath)

            getEmotionPrediction(File(it.imagePath))
        }
    }

    private fun getEmotionPrediction(file: File) {
        launch {
            val result = predictionServiceApi.uploadImage(file).execute()
            if (result != null && result.isSuccessful) {
                launch(UI) {
                    face_details_emotion_view.setPredictionResponse(result.body()!!)
                }
            } else {
            }
        }
    }
}
