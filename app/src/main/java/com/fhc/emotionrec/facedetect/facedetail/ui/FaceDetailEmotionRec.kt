package com.fhc.emotionrec.facedetect.facedetail.ui

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.facedetail.models.PredictionResponse
import kotlinx.android.synthetic.main.view_face_details_emotion.view.*

class FaceDetailEmotionRec(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs) {
    init {
        View.inflate(context, R.layout.view_face_details_emotion, this)
    }

    fun setPredictionResponse(message: String) {
        face_detail_emotion_text.text = message
    }

}