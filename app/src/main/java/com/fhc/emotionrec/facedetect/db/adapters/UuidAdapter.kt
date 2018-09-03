package com.fhc.emotionrec.facedetect.db.adapters

import android.arch.persistence.room.TypeConverter
import android.graphics.Rect
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class UuidAdapter {

    @TypeConverter
    fun fromUuid(uuid: UUID): String {

        return uuid.toString()
    }

    @TypeConverter
    fun toRect(value: String): UUID {
        return UUID.fromString(value)
    }
}