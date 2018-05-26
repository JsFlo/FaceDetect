package com.fhc.emotionrec.facedetect

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class FaceDetail : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, FaceDetail::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detail)
    }
}
