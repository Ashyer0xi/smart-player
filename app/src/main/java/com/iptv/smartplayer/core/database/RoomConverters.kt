package com.iptv.smartplayer.core.database

import androidx.room.TypeConverter
import com.iptv.smartplayer.data.local.entity.ContentType

class RoomConverters {
    @TypeConverter
    fun fromContentType(value: ContentType): String = value.name

    @TypeConverter
    fun toContentType(value: String): ContentType = ContentType.valueOf(value)
}
