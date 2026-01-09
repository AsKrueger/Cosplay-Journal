package com.cosplayjournal.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cosplans")
data class Cosplan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val status: String,
    val tags: String,
    val season: String,
    val difficulty: String,
    val estimatedBudget: Double,
    val realBudget: Double,
    val notes: String
)
