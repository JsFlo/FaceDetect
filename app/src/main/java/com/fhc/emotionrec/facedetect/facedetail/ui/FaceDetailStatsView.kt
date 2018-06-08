package com.fhc.emotionrec.facedetect.facedetail.ui

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import kotlinx.android.synthetic.main.view_face_details_stats.view.*
import java.text.DecimalFormat

class FaceDetailStatsView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    init {
        View.inflate(context, R.layout.view_face_details_stats, this)
    }

    fun setFaceImage(faceImage: FvFaceImage) {
        with(faceImage) {
            face_detail_smiling_prob.text = smilingProb.toPrettyFormat()
            face_detail_left_eye_prob.text = leftEyeProb.toPrettyFormat()
            face_detail_right_eye_prob.text = rightEyeProb.toPrettyFormat()
        }
    }

    private fun Float.toPrettyFormat(): String {
        return "${decimalFormat.format(this * 100)}%"
    }

    companion object {
        private val decimalFormat = DecimalFormat("##")
    }
}