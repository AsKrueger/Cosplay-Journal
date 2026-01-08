package com.cosplayjournal.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photo_sessions",
    foreignKeys = [
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["locationId"])]
)
data class PhotoSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long, // Timestamp
    val photographer: String,
    val notes: String = "",
    val locationId: Long? = null
)

@Entity(
    tableName = "cosplay_photosession_cross_ref",
    primaryKeys = ["cosplayId", "photoSessionId"],
    indices = [Index(value = ["photoSessionId"])]
)
data class CosplayPhotoSessionCrossRef(
    val cosplayId: Long,
    val photoSessionId: Long
)
