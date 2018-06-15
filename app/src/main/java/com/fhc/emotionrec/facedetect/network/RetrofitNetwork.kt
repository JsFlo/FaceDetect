package com.fhc.emotionrec.facedetect.network

import com.fhc.emotionrec.facedetect.models.PredictionResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://5efb7123.ngrok.io"

private const val READ_TIMEOUT_SECONDS = 20L

interface PredictionApi {
    @Multipart
    @POST("/predictionImage")
    fun uploadImage(@Part file: MultipartBody.Part, @Part("name") name: RequestBody): Call<PredictionResponse>
}

private val retrofit by lazy {
    val okHttpClient = OkHttpClient.Builder()
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

val predictionServiceApi by lazy {
    retrofit.create(PredictionApi::class.java)
}

fun PredictionApi.uploadImage(file: File): Call<PredictionResponse> {
    val fileRequestBody = RequestBody.create(MediaType.parse("image/*"), file)
    val fileToUpload = MultipartBody.Part.createFormData("file", file?.name, fileRequestBody)
    val filename = RequestBody.create(MediaType.parse("text/plain"), file?.name)
    return uploadImage(fileToUpload, filename)
}