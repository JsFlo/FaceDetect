package com.emotionrec.emotionrecapp.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import androidx.core.net.toUri
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

fun imageToUri(contentResolver: ContentResolver, bitmap: Bitmap): Uri {
    val path = Environment.getExternalStorageDirectory().toString()

    val file = File(path, "bitmap_${Date()}.png")
    file.outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }
    MediaStore.Images.Media.insertImage(
            contentResolver,
            file.absolutePath,
            file.name,
            file.name
    )
    return file.toUri()
}

fun ImageView.setImage(imagePath: String){
    launch {


        val targetW = width
        val targetH = height
        if(targetW > 0 && targetH > 0) {


            // Get the dimensions of the bitmap
            val bmOptions = BitmapFactory.Options()
            with(bmOptions) {
                inJustDecodeBounds = true
                val photoW = outWidth
                val photoH = outHeight
                val scaleFactor = Math.min(photoW / targetW, photoH / targetH)
                inJustDecodeBounds = false
                inSampleSize = scaleFactor
                inPurgeable = true
            }
            val bitmap = BitmapFactory.decodeFile(imagePath, bmOptions)
            var rotatedBitmap = bitmap

            // rotate bitmap if needed
            try {
                val ei = ExifInterface(imagePath)
                val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = bitmap.rotateImage(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = bitmap.rotateImage(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = bitmap.rotateImage(270f)
                }
            } catch (e: IOException) {
                throw IllegalStateException("Error: Image processing")
            }

            launch(UI) {
                setImageBitmap(rotatedBitmap)
            }
        }

    }
}

private fun Bitmap.rotateImage(angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.toFilePath(): String {
    val file_path = Environment.getExternalStorageDirectory().absolutePath + "/faceimages"
    val dir = File(file_path)
    if (!dir.exists())
        dir.mkdirs()

    val file = File(dir, "faceimages" + System.currentTimeMillis() + ".png")
    val fOut = FileOutputStream(file)

    this.compress(Bitmap.CompressFormat.PNG, 100, fOut)
    fOut.flush()
    fOut.close()
    return file.absoluteFile.toString()
}
