package com.fhc.emotionrec.facedetect

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.fhc.emotionrec.facedetect.EmotionDetectionActivity.FvFaceImage
import kotlinx.android.synthetic.main.activity_face_detail.*


class FaceDetailActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context, faceImage: FvFaceImage): Intent {

            val intent = Intent(context, FaceDetailActivity::class.java)
            intent.putExtra("faceImage", faceImage)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detail)

        face_details_stats_view.setFaceImage(intent.getParcelableExtra("faceImage"))
    }
}
