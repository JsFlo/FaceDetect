package com.fhc.emotionrec.facedetect.facedetail.ui

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.models.FvFaceImage

class FaceDetailEmotionRec(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    init {
        View.inflate(context, R.layout.view_face_details_emotion, this)
    }

}