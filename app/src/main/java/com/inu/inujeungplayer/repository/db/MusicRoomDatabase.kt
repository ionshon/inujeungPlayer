package com.inu.inujeungplayer.repository.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inu.inujeungplayer.MediaStoreService
import com.inu.inujeungplayer.repository.dao.RoomMusicDao
import com.inu.inujeungplayer.model.Music
import com.inu.inujeungplayer.repository.dao.RoomRadioDao
import com.inu.inujeungplayer.model.Radio

@Database(entities = [Music::class, Radio::class], version = 8, exportSchema = false)
@TypeConverters(MusicTypeConverters::class)
abstract class MusicRoomDatabase: RoomDatabase() {
    abstract fun musicDao(): RoomMusicDao
//    abstract fun storeService() : MediaStoreService
    abstract fun radioDao(): RoomRadioDao

    companion object {
        @Volatile // 휘발성 변수의 값은 캐시되지 않고 모든 쓰기와 읽기는 기본 메모리에서 실행됩니다. 이렇게 하면 INSTANCE 값이 항상 최신 상태로 유지되고 모든 실행 스레드에서 같은지 확인할 수 있습니다. 즉, 한 스레드에서 INSTANCE를 변경하면 다른 모든 스레드에 즉시 표시됩니다.
        private var  instance: MusicRoomDatabase? = null

        @Synchronized //휘발성 변수의 값은 캐시되지 않고 모든 쓰기와 읽기는 기본 메모리에서 실행됩니다. 이렇게 하면 INSTANCE 값이 항상 최신 상태로 유지되고 모든 실행 스레드에서 같은지 확인할 수 있습니다. 즉, 한 스레드에서 INSTANCE를 변경하면 다른 모든 스레드에 즉시 표시됩니다.
        fun getInstance(context: Context): MusicRoomDatabase {
            return instance ?: synchronized(this) {
                instance
                    ?: buildDatabase(
                        context
                    ). also { instance = it }
            }
        }
        private fun buildDatabase(context: Context): MusicRoomDatabase {
            return Room.databaseBuilder(context, MusicRoomDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration().build()
        }
    }



    // Music 변경으로 필요, 버전올리고 아래 .addMigrations(migration_1_2) 추가하면 됨
    val migration_1_2 = object: Migration(1,2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            //만약, 테이블이 추가 되었다면 어떤 테이블이 추가 되었는지 알려주는 query문장이 필요
            database.execSQL("CREATE TABLE 'REVIEW' ('id' INTEGER, 'review' TEXT, " + "PRIMARY KEY('id'))")
        }
    }
    fun destroyInstance() {
        instance = null
    }
}

private const val DATABASE_NAME = "musics_db"