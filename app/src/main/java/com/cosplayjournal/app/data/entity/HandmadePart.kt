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
    val processSteps: String,
    val materials: String,
    val estimatedCost: Double,
    val isFinished: Boolean = false
)
