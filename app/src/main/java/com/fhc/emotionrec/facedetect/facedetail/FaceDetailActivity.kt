package com.fhc.emotionrec.facedetect.facedetail

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_face_detail.*
import android.provider.MediaStore
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import com.fhc.emotionrec.facedetect.models.FvFaceImageParcel


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
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, faceImageParcel.imageBitmapUri)

        face_details_stats_view.setFaceImage(FvFaceImage(faceImageParcel.smilingProb,
                faceImageParcel.leftEyeProb, faceImageParcel.rightEyeProb, bitmap,
                faceImageParcel.boundingBox, faceImageParcel.color))

        face_detail_hero_image.setImageBitmap(bitmap)
    }
}
