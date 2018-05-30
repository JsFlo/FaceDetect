package com.fhc.emotionrec.facedetect.facedetail.ui

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.fhc.emotionrec.facedetect.facecamera.EmotionDetectionActivity
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import kotlinx.android.synthetic.main.view_face_details_stats.view.*

class FaceDetailStatsView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    init {
        View.inflate(context, R.layout.view_face_details_stats, this)
    }

    fun setFaceImage(faceImage: FvFaceImage) {
        face_detail_smiling_prob.text = face_detail_smiling_prob.text.toString() + faceImage.smilingProb.toString()
        face_detail_left_eye_prob.text = face_detail_left_eye_prob.text.toString() + faceImage.leftEyeProb.toString()
        face_detail_right_eye_prob.text = face_detail_right_eye_prob.text.toString() + faceImage.rightEyeProb.toString()
    }
}