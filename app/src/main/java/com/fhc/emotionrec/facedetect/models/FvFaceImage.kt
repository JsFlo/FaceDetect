package com.fhc.emotionrec.facedetect.models

import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace


data class FvFaceImage(val fvFace: FirebaseVisionFace, val fvImage: FirebaseVisionImage)