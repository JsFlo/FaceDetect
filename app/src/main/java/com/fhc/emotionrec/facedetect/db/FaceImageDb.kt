package com.fhc.emotionrec.facedetect.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.fhc.emotionrec.facedetect.db.adapters.RectAdapter
import com.fhc.emotionrec.facedetect.db.adapters.UuidAdapter
import com.fhc.emotionrec.facedetect.db.dao.FaceImageDao
import com.fhc.emotionrec.facedetect.models.FaceImageEntity


@Database(entities = arrayOf(FaceImageEntity::class), version = 1)
@TypeConverters(RectAdapter::class, UuidAdapter::class)
abstract class FaceImageDb : RoomDatabase() {
    abstract fun faceImageDao(): FaceImageDao

    companion object {
        private var INSTANCE: FaceImageDb? = null

        fun getInstance(context: Context): FaceImageDb? {
            if (INSTANCE == null) {
                synchronized(FaceImageDb::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            FaceImageDb::class.java, "FaceImageDb.db")
                            .build()
                }
            }
            return INSTANCE
        }
    }

}