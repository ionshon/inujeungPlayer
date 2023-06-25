package com.inu.inujeungplayer.repository.db

import android.net.Uri
import androidx.room.TypeConverter
import java.util.*

class MusicTypeConverters {

    @TypeConverter
    fun getUrifromString(value: String?): Uri? {
        return if (value == null) {
            null
        } else Uri.parse(value)
    }


    @TypeConverter
    fun getStringfromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date? {
        return millisSinceEpoch?.let {
            Date(it)
        }
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }
    @TypeConverter
    fun toUUID(uuid: UUID?): UUID? {
        return UUID.fromString(uuid.toString())
    }

}