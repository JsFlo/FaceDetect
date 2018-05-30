package com.fhc.emotionrec.facedetect.facedetail.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fhc.emotionrec.facedetect.R
import com.fhc.emotionrec.facedetect.facecamera.GraphicFaceTrackerFactory.FaceTrackerListener
import com.fhc.emotionrec.facedetect.facecamera.debug
import com.fhc.emotionrec.facedetect.models.FvFaceImage
import kotlinx.android.synthetic.main.view_face_id.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

data class FaceId(val id: Int, val faceImage: FvFaceImage) {

}

class FaceIdAdapter(private val listener: Listener, private val idFace: MutableList<FaceId> = mutableListOf()) : RecyclerView.Adapter<FaceIdViewHolder>(),
        FaceTrackerListener, FaceIdViewHolder.Listener {

    interface Listener {
        fun onFaceImageClicked(faceImage: FvFaceImage)
    }

    override fun newItem(id: Int, face: FvFaceImage) {
        "new Item".debug("faceId")
        idFace.add(FaceId(id, face))
        launch(UI) {
            notifyDataSetChanged()
        }
    }

    override fun onUpdateItem(id: Int, face: FvFaceImage) {
//        idLi
    }

    override fun onMissingItem(id: Int) {

    }

    override fun onDestroyItem(id: Int) {
        "onDestroyItem".debug("faceId")
        idFace.remove(idFace.find { it.id == id })

        launch(UI) {
            notifyDataSetChanged()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaceIdViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_face_id, parent, false)
        return FaceIdViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: FaceIdViewHolder, position: Int) {
        val idFace = idFace[position]
        holder.itemView.face_id_text.text = idFace.id.toString()
        holder.itemView.face_id_image.setImageBitmap(idFace.faceImage.imageBitmap)
        holder.itemView.face_id_image.borderColor = idFace.faceImage.color
    }

    override fun onFaceClicked(adapterPosition: Int) {
        listener.onFaceImageClicked(idFace[adapterPosition].faceImage)
    }

    override fun getItemCount() = idFace.size
}

class FaceIdViewHolder(itemView: View, val listener: Listener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    interface Listener {
        fun onFaceClicked(adapterPosition: Int)
    }

    init {
        itemView.face_id_image.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        listener.onFaceClicked(adapterPosition)
    }
}