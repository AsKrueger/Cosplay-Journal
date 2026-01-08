package com.cosplayjournal.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "character_references")
data class CharacterReference(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterName: String,
    val series: String,
    val imageUri: String, // Local URI
    val notes: String = ""
)
