package com.fhc.emotionrec.facedetect.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.fhc.emotionrec.facedetect.models.FaceImageEntity
import java.util.*


@Dao
interface FaceImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(faceImageEntity: FaceImageEntity)

    @Transaction
    fun update(updatedFaceImageEntity: FaceImageEntity){
        val oldFaceImageEntity = getFaceImageEntityWithUuid(updatedFaceImageEntity.uuid.toString())
        if(oldFaceImageEntity != null) {
            insert(updatedFaceImageEntity.copy(color = oldFaceImageEntity.color, active = oldFaceImageEntity.active))
        } else {
            insert(updatedFaceImageEntity)
        }
    }

    @Query("DELETE FROM FaceImageEntity")
    fun deleteAll()

    @Query("SELECT * FROM FaceImageEntity WHERE FaceImageEntity.uuid = :uuid")
    fun getLiveDataFaceImageEntityWithUuid(uuid: String): LiveData<FaceImageEntity?>

    @Query("SELECT * FROM FaceImageEntity WHERE FaceImageEntity.uuid = :uuid")
    fun getFaceImageEntityWithUuid(uuid: String): FaceImageEntity?

    @Query("UPDATE FaceImageEntity SET active = 0 WHERE FaceImageEntity.uuid = :uuid")
    fun deActivateFaceImage(uuid: UUID)

    @Query("SELECT * FROM faceimageentity WHERE active = 1")
    fun getAllFaceImages(): LiveData<List<FaceImageEntity>>
}