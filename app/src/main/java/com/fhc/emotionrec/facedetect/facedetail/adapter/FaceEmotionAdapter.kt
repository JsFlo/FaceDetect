package com.fhc.emotionrec.facedetect.facedetail.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.facedetail.models.Prediction
import kotlinx.android.synthetic.main.item_face_emotion.view.*


class FaceEmotionAdapter(private val predictions: List<Prediction>) : RecyclerView.Adapter<FaceEmotionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaceEmotionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_face_emotion, parent, false)
        return FaceEmotionViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaceEmotionViewHolder, position: Int) {
        val prediction = predictions[position]
        with(holder.itemView) {
            face_emotion_header_text.text = prediction.emotion.toString()
            face_emotion_progress_bar.progress = prediction.probability
        }
    }

    override fun getItemCount(): Int = predictions.size

}

class FaceEmotionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)