package com.fhc.emotionrec.facedetect.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class PredictionResponse(val sortedPredictions: List<Prediction>,
                              val guessedPrediction: Prediction
) : Parcelable

@Parcelize
data class Prediction(val probability: Float, val emotion: Emotion) : Parcelable

enum class Emotion {
    ANGRY,
    DISGUST,
    FEAR,
    HAPPY,
    SAD,
    SURPRISE,
    NEUTRAL,
    VULCAN;
}