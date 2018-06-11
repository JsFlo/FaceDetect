package com.fhc.emotionrec.facedetect.facedetail.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.emotionrec.emotionrecapp.utils.setImage
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.models.FvFaceImageParcel
import kotlinx.android.synthetic.main.item_face_image.view.*

class FaceImageAdapter(val items: List<FvFaceImageParcel>) :
    RecyclerView.Adapter<FaceImageViewHolder>(), FaceImageViewHolder.Listener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaceImageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_face_image, parent, false)
        return FaceImageViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: FaceImageViewHolder, position: Int) {
//        holder.itemView.item_face_image_image.setImage(items[position].imageBitmapUri.path)
        holder.itemView.item_face_image_image.setImageResource(R.drawable.common_full_open_on_phone)
    }

    override fun onFaceImageClicked(adapterPosition: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int = items.size

}

class FaceImageViewHolder(itemView: View, val listener: Listener) :
    RecyclerView.ViewHolder(itemView),
    View.OnClickListener {

    interface Listener {
        fun onFaceImageClicked(adapterPosition: Int)
    }

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        listener.onFaceImageClicked(adapterPosition)
    }
}