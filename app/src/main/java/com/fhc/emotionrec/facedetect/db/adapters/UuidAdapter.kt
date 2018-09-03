package com.fhc.emotionrec.facedetect.db.adapters

import android.arch.persistence.room.TypeConverter
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