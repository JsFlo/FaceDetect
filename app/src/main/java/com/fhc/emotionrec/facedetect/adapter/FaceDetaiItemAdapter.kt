package com.fhc.emotionrec.facedetect.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fhc.emotionrec.facedetect.utils.setImage
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.models.FaceImageEntity
import kotlinx.android.synthetic.main.view_face_id.view.*

class FaceDetailItemAdapter(
        var listener: Listener? = null,
        private val faceDetailItems: MutableList<FaceImageEntity> = mutableListOf()
) :
        RecyclerView.Adapter<FaceDetailItemViewHolder>(),
        FaceDetailItemViewHolder.Listener {

    interface Listener {
        fun onFaceDetailItemClicked(faceDetailItem: FaceImageEntity)
    }

    fun swapFaceDetailItems(newFaceDetailItems: List<FaceImageEntity>?) {
        faceDetailItems.clear()
        faceDetailItems.addAll(newFaceDetailItems ?: listOf())
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaceDetailItemViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_face_id, parent, false)
        return FaceDetailItemViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: FaceDetailItemViewHolder, position: Int) {
        val faceDetailItem = faceDetailItems[position]
        with(faceDetailItem) {
            holder.itemView.face_id_image.setImage(imagePath)
            holder.itemView.face_id_image.borderColor = color
            holder.itemView.face_id_face_detail.setFaceDetailStats(smilingProb, leftEyeProb, rightEyeProb)
        }
    }

    override fun onFaceDetailItemClicked(adapterPosition: Int) {
        listener?.onFaceDetailItemClicked(faceDetailItems[adapterPosition])
    }

    override fun getItemCount(): Int = faceDetailItems.size

}

class FaceDetailItemViewHolder(itemView: View, private val listener: Listener) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
    interface Listener {
        fun onFaceDetailItemClicked(adapterPosition: Int)
    }

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        listener.onFaceDetailItemClicked(adapterPosition)
    }
}