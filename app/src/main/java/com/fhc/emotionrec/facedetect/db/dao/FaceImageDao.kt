package com.fhc.emotionrec.facedetect.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.fhc.emotionrec.facedetect.db.entity.FaceImageEntity
import java.util.*


@Dao
interface FaceImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(faceImageEntity: FaceImageEntity)

    @Transaction
    fun update(updatedFaceImageEntity: FaceImageEntity){
        val oldFaceImageEntity = getFaceImageEntityWithUuid(updatedFaceImageEntity.uuid)
        if(oldFaceImageEntity != null) {
            insert(updatedFaceImageEntity.copy(color = oldFaceImageEntity.color))
        } else {
            insert(updatedFaceImageEntity)
        }
    }

    @Query("DELETE FROM FaceImageEntity")
    fun deleteAll()

    @Query("SELECT * FROM FaceImageEntity WHERE FaceImageEntity.uuid = :uuid")
    fun getFaceImageEntityWithUuid(uuid: UUID): FaceImageEntity?

    @Query("DELETE FROM FaceImageEntity WHERE FaceImageEntity.uuid = :uuid")
    fun deleteFaceImage(uuid: UUID)

    @Query("SELECT * FROM faceimageentity")
    fun getAllFaceImages(): LiveData<List<FaceImageEntity>>
}