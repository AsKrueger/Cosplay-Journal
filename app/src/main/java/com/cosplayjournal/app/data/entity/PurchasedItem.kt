package com.cosplayjournal.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchased_items",
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
data class PurchasedItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cosplayId: Long,
    val name: String,
    val storeName: String,
    val purchaseLink: String,
    val price: Double,
    val isReceived: Boolean = false
)
