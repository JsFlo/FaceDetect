package com.fhc.emotionrec.facedetect.db.adapters

import android.arch.persistence.room.TypeConverter
import android.graphics.Rect
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// TODO: Make Rect an @embedded (with left, top, right, bottom as ints) instead of doing this
class RectAdapter {

    @TypeConverter
    fun fromRect(rect: Rect): String {

        val value = listOf<Int>(rect.left, rect.top, rect.right, rect.bottom)
        val gson = Gson()
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toRect(value: String): Rect {
        val gson = Gson()
        val type = object : TypeToken<List<Int>>() {}.type
        val intList = gson.fromJson<List<Int>>(value, type)
        return Rect(intList[0],intList[1],intList[2],intList[3])
    }
}