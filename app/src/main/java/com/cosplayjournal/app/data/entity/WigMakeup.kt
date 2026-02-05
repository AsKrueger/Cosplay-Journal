package com.cosplayjournal.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wig_makeup_items",
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
data class WigMakeup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cosplayId: Long,
    val name: String,
    val imageUris: String = "", // Comma-separated URIs
    val price: Double = 0.0,
    val timeSpent: String = "", // e.g., "4h 15m"
    val description: String = "",
    val productsUsed: String = "", // Specific for makeup/wig
    val isFinished: Boolean = false
)
