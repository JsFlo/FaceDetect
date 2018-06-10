package com.fhc.emotionrec.facedetect.facecamera.adapter

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
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.*
import java.util.concurrent.TimeUnit

data class FaceId(val uuid: UUID, val faceImage: FvFaceImage)

class FaceIdAdapter(private val listener: Listener, private val idFace: MutableList<FaceId> = mutableListOf()) : RecyclerView.Adapter<FaceIdViewHolder>(),
        FaceTrackerListener, FaceIdViewHolder.Listener {

    interface Listener {
        fun onFaceImageClicked(faceImage: FvFaceImage)
    }

    companion object {
        private const val DESTROY_DELAY_SECONDS = 3L
    }

    override fun newItem(uuid: UUID, face: FvFaceImage) {
        "new Item".debug("faceId")
        launch(UI) { addFaceId(FaceId(uuid, face)) }
    }

    override fun onUpdateItem(uuid: UUID, face: FvFaceImage) {
//        idLi
    }

    override fun onMissingItem(uuid: UUID) {

    }

    override fun onDestroyItem(uuid: UUID) {
        "onDestroyItem".debug("faceId")
        async {
            delay(DESTROY_DELAY_SECONDS, TimeUnit.SECONDS)
            idFace.find { it.uuid == uuid }?.let {
                launch(UI) { removeFaceId(it) }
            }
        }
    }

    private fun addFaceId(faceId: FaceId) {
        idFace.add(faceId)
        notifyDataSetChanged()
    }

    private fun removeFaceId(faceId: FaceId) {
        idFace.remove(faceId)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaceIdViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_face_id, parent, false)
        return FaceIdViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: FaceIdViewHolder, position: Int) {
        val idFace = idFace[position]
        holder.itemView.face_id_image.setImageBitmap(idFace.faceImage.imageBitmap)
        holder.itemView.face_id_image.borderColor = idFace.faceImage.color
        holder.itemView.face_id_face_detail.setFaceImage(idFace.faceImage)
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