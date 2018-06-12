package com.fhc.emotionrec.facedetect.ui

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.fhc.emotionrec.facedetect.R
import kotlinx.android.synthetic.main.view_face_details_stats.view.*
import java.text.DecimalFormat

data class FaceDetailStats(val smilingProb: Float, val leftEyeProb: Float, val rightEyeProb: Float)

class FaceDetailStatsView(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs) {
    init {
        View.inflate(context, R.layout.view_face_details_stats, this)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.FaceDetailStatsView)
        val showFaceIcon =
            ta.getBoolean(R.styleable.FaceDetailStatsView_showFaceIcon, SHOW_FACE_ICON_DEFAULT)
        ta.recycle()

        face_details_face_image.visibility = if (showFaceIcon) View.VISIBLE else View.GONE
    }

    fun setFaceDetailStats(faceDetailStats: FaceDetailStats) {
        with(faceDetailStats) {
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
        private const val SHOW_FACE_ICON_DEFAULT = true
    }
}