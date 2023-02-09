package com.inu.inujeungplayer

import android.content.ContentResolver
import android.content.ContentUris
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.inu.inujeungplayer.adapter.MusicAdapter.Companion.index
import com.inu.inujeungplayer.constant.MusicConstants
import com.inu.inujeungplayer.model.Music
import com.inu.inujeungplayer.repository.HomeRepository
import com.inu.inujeungplayer.repository.dao.RoomMusicDao
import com.inu.inujeungplayer.utils.MyApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MediaStoreService {
//    val viewModelFactory = MyApplication.repository?.let { ViewModelFactory(repository = it) }!!
    //        viewModel = ViewModelProvider(this, viewModelFactory).get(MusicViewModel::class.java)

    suspend fun allMusics(): List<Music> = withContext(Dispatchers.Default) {
//        val result = sunflowerService.getAllMusics()
        val result = getAllMusics()
//        Log.d("dddddddddddddd","${result.size}")
        result //.shuffled()
    }
    suspend fun allVideos(): List<Music> = withContext(Dispatchers.Default) {
//        val result = sunflowerService.getAllMusics()
        val result = getAllVideo()
//        Log.d("dddddddddddddd","allVideos: ${result.size}")
        result //.shuffled()
    }

    suspend fun allMusicFromRoom(musicDao: RoomMusicDao)/* : List<Music> = withContext(Dispatchers.Default) */{
//        val result = updateFromRoom()
        updateRoom(musicDao) // 새파일 Room 없데이트
//        result
    }
    suspend fun customMusicSortOrder(): List<String> = withContext(Dispatchers.Default) {
        val result = getAllMusics() // sunflowerService.getCustomPlantSortOrder()
        result.map { plant -> plant.title?: "" }
    }

//interface sunflowerService {
//    @GET("googlecodelabs/kotlin-coroutines/master/advanced-coroutines-codelab/sunflower/src/main/assets/plants.json")

    suspend fun updateRoom(musicDao: RoomMusicDao) {
//        var musics = mutableListOf<Music>() //LiveData<List<Music>>
        MyApplication.applicationContext().contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MusicConstants.projection, // mCursorCols
            null, //selectionClause, //"artist = "+ "'장나라'"
            null, //selectionArgs, // null
            MusicConstants.sortOrder
        )?.use { cursor ->
            withContext(Dispatchers.Default) {
                while (cursor.moveToNext()) {
                    val title = cursor.getString(1)
                    val duration = cursor.getLong(4)
                    if (duration > 10000 && !title.contains("통화 녹음")) {
                        val album = cursor.getString(0)
                        val id = cursor.getInt(8)
                        val artist = cursor.getString(2)
                        val albumID = cursor.getLong(3)
                        val path =
                            cursor.getString(5)   // getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))//
                        val albumUri = Uri.parse("content://media/external/audio/albumart/$albumID")
                        val genre = cursor.getString(6)
                        //                        val bookmark = cursor.getString(7)
                        val contentUri =
                            ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toLong())
                        val currentPosition = -1
                        //                        val isSelected = false

                        val tempMusic = Music(
                            id, title, artist, album, albumID, duration, albumUri.toString(), path, genre,
                            false, false, contentUri.toString(), currentPosition
                        )
//                        MyApplication.(tempMusic)
                        //                    repository?.insert(tempMusic) // room에 전체
                        musicDao.insert(tempMusic)

                    } // duration 조건00
                } // cursor while
                cursor.close()
            } // 코루틴
        }
        //    Log.d("dddddddddddd","cursor=? ${musics.size}")
//        return musics
    }
    suspend fun getAllMusics(): List<Music> {
        var musics = mutableListOf<Music>() //LiveData<List<Music>>
        MyApplication.applicationContext().contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MusicConstants.projection, // mCursorCols
            null, //selectionClause, //"artist = "+ "'장나라'"
            null, //selectionArgs, // null
            MusicConstants.sortOrder
        )?.use { cursor ->
            withContext(Dispatchers.Default) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val title = cursor.getString(1)
                        val duration = cursor.getLong(4)
                        if (duration > 10000 && !title.contains("통화 녹음")) {
                            val album = cursor.getString(0)
                            val id = cursor.getInt(8)
                            val artist = cursor.getString(2)
                            val albumID = cursor.getLong(3)
                            val path =
                                cursor.getString(5)   // getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))//
                            val albumUri = Uri.parse("content://media/external/audio/albumart/$albumID")
                            val genre = cursor.getString(6)
                            //                        val bookmark = cursor.getString(7)
                            val contentUri =
                                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toLong())
                            val currentPosition = -1
                            //                        val isSelected = false

                            val tempMusic = Music(
                                id, title, artist, album, albumID, duration, albumUri.toString(), path, genre,
                                false, false, contentUri.toString(), currentPosition
                            )
                            musics += tempMusic
                            //                    repository?.insert(tempMusic) // room에 전체

                        } // duration 조건00
                    } // cursor while
                    cursor.close()
                } // if
            } // 코루틴
        }
        return musics
    } // fun

    fun getAllVideo(): List<Music> {
        val videoList = mutableListOf<Music>()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
        val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
             arrayOf(
                MediaStore.Video.Media._ID, //0
                MediaStore.Video.Media.TITLE, //1
                MediaStore.Video.Media.ARTIST, //2
                MediaStore.Video.Media.DISPLAY_NAME, //3
                MediaStore.Video.Media.DURATION, //4
                MediaStore.Video.Media.SIZE, //5
                MediaStore.Video.Media.ALBUM, //6
                MediaStore.Video.Media.BOOKMARK, //7
                MediaStore.Video.Media.DATA, //8
                MediaStore.Video.Media.MIME_TYPE //9
            )
        } else {
             arrayOf(
                MediaStore.Video.Media._ID, //0
                MediaStore.Video.Media.TITLE, //1
                MediaStore.Video.Media.ARTIST, //2
                MediaStore.Video.Media.DISPLAY_NAME, //3
                MediaStore.Video.Media.DURATION, //4
                MediaStore.Video.Media.SIZE, //5
                MediaStore.Video.Media.ALBUM, //6
                MediaStore.Video.Media.BOOKMARK, //7
                MediaStore.Video.Media.DATA, //8
                MediaStore.Video.Media.MIME_TYPE //9
            )
        }


// Show only videos that are at least 5 minutes in duration.
        val selection = "${MediaStore.Video.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES).toString()
        )

// Display videos in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"

        MyApplication.applicationContext().contentResolver.query(
            collection,
            projection,
            null, //selection,
            null, //selectionArgs,
            null //sortOrder
        )?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                val id = cursor.getInt(idColumn)
                val title = cursor.getString(1)
                val artist = cursor.getString(2)
//                val albumID = cursor.getLong(3)
                val album = cursor.getString(6)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getInt(sizeColumn)
                val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toLong())
                val path = cursor.getString(8)   // getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))//
//                val albumUri = Uri.parse("content://media/external/audio/albumart/$albumID")
                val genre = cursor.getString(9)
                val bookmark = cursor.getString(7)
                //
                val currentPosition = -1
//                val mmr = MediaMetadataRetriever()
//                mmr.setDataSource(contentUri.path)
//                val thummbnailBitmap = mmr.frameAtTime
//                imageView.setImageBitmap(thummbnailBitmap)
                // Stores column values and the contentUri in a local object
                // that represents the media file.
                videoList += Music(id,title,artist,album,null,duration,null,path,genre,false,
                    false,contentUri.toString(),1 )
            }
        }
        return  videoList
    }
}