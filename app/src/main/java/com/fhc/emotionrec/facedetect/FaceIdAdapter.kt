package com.fhc.emotionrec.facedetect

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import kotlinx.android.synthetic.main.view_face_id.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class FaceIdAdapter(private val idList: MutableList<String>) : RecyclerView.Adapter<FaceIdViewHolder>(), EmotionDetectionActivity.GraphicFaceTrackerFactory.FaceTrackerListener {
    override fun newItem(id: Int, face: FirebaseVisionFace) {
        "new Item".debug("faceId")
        idList.add(id.toString())
        launch(UI) {
            notifyDataSetChanged()
        }
    }

    override fun onUpdateItem(id: Int, face: FirebaseVisionFace) {
//        idLi
    }

    override fun onMissingItem(id: Int) {

    }

    override fun onDestroyItem(id: Int) {
        "onDestroyItem".debug("faceId")
        idList.remove(id.toString())
        launch(UI) {
            notifyDataSetChanged()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaceIdViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_face_id, parent, false)
        return FaceIdViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaceIdViewHolder, position: Int) {
        holder.itemView.face_id_text.text = idList[position]
    }

    override fun getItemCount() = idList.size

}

class FaceIdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)