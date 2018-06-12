package com.fhc.emotionrec.facedetect.adapter

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.ui.FaceDetailStats
import kotlinx.android.synthetic.main.view_face_id.view.*
import java.util.*

data class FaceDetailItem(
    val uuid: UUID,
    val color: Int,
    val image: Bitmap,
    val faceDetailStats: FaceDetailStats
) {
    override fun equals(other: Any?): Boolean {
        return other is FaceDetailItem && uuid == other.uuid
    }
}

class FaceDetailItemAdapter(
    var listener: Listener? = null,
    private val faceDetailItems: MutableList<FaceDetailItem> = mutableListOf()
) :
    RecyclerView.Adapter<FaceDetailItemViewHolder>(),
    FaceDetailItemViewHolder.Listener {

    interface Listener {
        fun onFaceDetailItemClicked(faceDetailItem: FaceDetailItem)
    }

    fun addFaceDetailItem(faceDetailItem: FaceDetailItem) {
        faceDetailItems.add(faceDetailItem)
        notifyDataSetChanged()
    }

    fun updateFaceDetailItem(newFaceDetailItem: FaceDetailItem) {
        faceDetailItems.find { it.uuid == newFaceDetailItem.uuid }?.let { oldItem ->
            val index = faceDetailItems.indexOf(oldItem)
            faceDetailItems[index] = newFaceDetailItem
            notifyItemChanged(index)
        }
    }

    fun removeFaceDetailItem(uuid: UUID) {
        faceDetailItems.find { it.uuid == uuid }?.let { faceDetailItem ->
            faceDetailItems.remove(faceDetailItem)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaceDetailItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_face_id, parent, false)
        return FaceDetailItemViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: FaceDetailItemViewHolder, position: Int) {
        val faceDetailItem = faceDetailItems[position]
        with(faceDetailItem) {
            holder.itemView.face_id_image.setImageBitmap(image)
            holder.itemView.face_id_image.borderColor = color
            holder.itemView.face_id_face_detail.setFaceDetailStats(faceDetailStats)
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