package com.cosplayjournal.app.data.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "cosplay_reference_cross_ref",
    primaryKeys = ["cosplayId", "referenceId"],
    indices = [Index(value = ["referenceId"])]
)
data class CosplayReferenceCrossRef(
    val cosplayId: Long,
    val referenceId: Long
)
