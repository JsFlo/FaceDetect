package com.fhc.emotionrec.facedetect.ui

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.fhc.emotionrec.facedetect.R

class FaceDetailStatsView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    init {
        View.inflate(context, R.layout.view_face_details_stats, this)
    }
}