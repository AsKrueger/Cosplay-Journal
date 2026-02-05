package com.cosplayjournal.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "handmade_parts",
    foreignKeys = [
        ForeignKey(
            entity = Cosplay::class,
            parentColumns = ["id"],
            childColumns = ["cosplayId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cosplayId"])]
)
data class HandmadePart(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cosplayId: Long,
    val name: String,
    val imageUris: String = "", // Comma-separated URIs
    val price: Double = 0.0,
    val timeSpent: String = "", // e.g., "4h 15m"
    val processDescription: String = "",
    val projectPercentage: Int = 0,
    val materials: String = "", // Legacy field or general list
    val isFinished: Boolean = false
)

@Entity(
    tableName = "part_resources",
    foreignKeys = [
        ForeignKey(
            entity = HandmadePart::class,
            parentColumns = ["id"],
            childColumns = ["partId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["partId"])]
)
data class PartResource(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partId: Long,
    val name: String,
    val webLink: String = "",
    val price: Double = 0.0
)
