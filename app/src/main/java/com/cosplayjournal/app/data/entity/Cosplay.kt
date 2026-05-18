package com.cosplayjournal.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cosplays",
    foreignKeys = [
        ForeignKey(
            entity = Cosplan::class,
            parentColumns = ["id"],
            childColumns = ["cosplanId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["cosplanId"])]
)
data class Cosplay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cosplanId: Long? = null,
    val characterName: String,
    val series: String,
    val preferredWeather: String = "",
    val wigs: String = "",
    val makeup: String = "",
    val accessories: String = "",
    val notes: String = "",
    val recognition: String = "", // Premios o menciones
    val isCompleted: Boolean = true, // Por defecto true para Cosplay
    val isFavorite: Boolean = false,
    val mainImageUri: String? = null
)
