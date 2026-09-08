package com.iptv.smartplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val streamId: Int,
    val name: String,
    val logoUrl: String?,
    val categoryId: String?,
    val epgChannelId: String?,
    val sortOrder: Int,
    val isHidden: Boolean = false, // دعم "إخفاء قنوات" في الإعدادات
    val supportsCatchup: Boolean = false,
)

/** تُخزَّن مضغوطة ولفترة قصيرة فقط (Cache) لتسريع دليل البرامج دون طلب متكرر */
@Entity(tableName = "epg_data", primaryKeys = ["channelId", "startEpochMillis"])
data class EpgEntity(
    val channelId: String,
    val programTitle: String,
    val description: String?,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
)
