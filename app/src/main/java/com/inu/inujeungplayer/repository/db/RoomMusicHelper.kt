package com.inu.inujeungplayer.repository.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.inu.inujeungplayer.repository.dao.RoomMusicDao
import com.inu.inujeungplayer.model.Music
import com.inu.inujeungplayer.model.Radio

@Database(entities = [Music::class, Radio::class], version = 1)
abstract class RoomMusicHelper: RoomDatabase() {
    abstract fun roomMusicDao(): RoomMusicDao
}