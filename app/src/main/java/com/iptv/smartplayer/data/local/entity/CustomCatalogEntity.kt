package com.iptv.smartplayer.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "custom_catalogs")
data class CustomCatalogEntity(
    @PrimaryKey(autoGenerate = true) val catalogId: Long = 0,
    val name: String,
    val iconKey: String? = null, // معرّف أيقونة مختارة من مكتبة داخلية
    val createdAtEpochMillis: Long,
)

@Entity(
    tableName = "catalog_items",
    foreignKeys = [
        ForeignKey(
            entity = CustomCatalogEntity::class,
            parentColumns = ["catalogId"],
            childColumns = ["catalogId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CatalogItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val catalogId: Long,
    val contentId: String,
    val contentType: ContentType,
    val title: String,
    val posterUrl: String?,
)
