package com.fhc.emotionrec.facedetect.facedetail.ui

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.View
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.facedetail.adapter.FaceEmotionAdapter
import com.fhc.emotionrec.facedetect.facedetail.models.PredictionResponse
import kotlinx.android.synthetic.main.view_face_details_emotion.view.*

class FaceDetailEmotionRec(context: Context, attrs: AttributeSet) :
        ConstraintLayout(context, attrs) {
    init {
        View.inflate(context, R.layout.view_face_details_emotion, this)
    }

    fun setPredictionResponse(predictionResponse: PredictionResponse) {
        with(predictionResponse) {
            face_details_emotion_recycler_view.adapter = FaceEmotionAdapter(predictionResponse.sortedPredictions)
            face_details_emotion_recycler_view.layoutManager = LinearLayoutManager(context)
        }
    }

}