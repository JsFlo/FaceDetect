package com.emotionrec.emotionrecapp.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private fun Context.hasWriteExternalPermissions() = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
private fun Intent.hasAppToHandle(packageManager: PackageManager) = this.resolveActivity(packageManager) != null

sealed class ImageFileResult {
    object WritePermissionMissing : ImageFileResult()
    object NoAppHandler : ImageFileResult()
    data class FileCreated(val file: File?, val intent: Intent) : ImageFileResult()
}

@Throws(IOException::class)
private fun createImageFile(): File? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    return File.createTempFile(
            "emotionRec_${timeStamp}_",
            ".jpg",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) // storage dir
    )
}


fun getImageFile(context: Context, packageManager: PackageManager, packageName: String): ImageFileResult {
    return if (!context.hasWriteExternalPermissions()) {
        ImageFileResult.WritePermissionMissing
    } else {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.hasAppToHandle(packageManager)) {
            val file = createImageFile()
            val photoUri = FileProvider.getUriForFile(context, "$packageName.fileprovider", file!!)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            ImageFileResult.FileCreated(file, intent)
        } else {
            ImageFileResult.NoAppHandler
        }
    }
}