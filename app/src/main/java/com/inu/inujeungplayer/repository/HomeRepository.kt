package com.inu.inujeungplayer.repository

import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.annotation.AnyThread
import androidx.lifecycle.*
import com.inu.inujeungplayer.MediaStoreService
import com.inu.inujeungplayer.constant.MusicDevice
import com.inu.inujeungplayer.constant.MusicDevice.musicList
import com.inu.inujeungplayer.constant.MusicDevice.oldMusic
import com.inu.inujeungplayer.repository.dao.RoomMusicDao
import com.inu.inujeungplayer.model.Music
import com.inu.inujeungplayer.utils.CacheOnSuccess
import com.inu.inujeungplayer.utils.ComparablePair
import com.inu.inujeungplayer.utils.MyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeRepository (
    private val musicDao: RoomMusicDao
){

    private val mediaService = MediaStoreService()

    var _songid = MutableLiveData<Int>()
    var songid: LiveData<Int> = _songid
    /*var _musicInNoti = MutableLiveData<Music>()
    fun serviceNotiChanged(music: Music) {
        _musicInNoti.value = music
        Log.i("musicInNoti", "serviceNotiChanged ${MusicDevice.musicIDList.indexOf(MusicDevice.songID)}")
    }*/
    val readAllData : Flow<List<Music>> = musicDao.getAllMusics()
   /* val musics: LiveData<List<Music>> = liveData<List<Music>> {
        val musicsLiveData = musicDao.getAllMusics().asLiveData() // Room에서 가져오는 것
        val customSortOrder = musicsListSortOrderCache.getOrAwait() // 미디어에서 가져오는 것
        emitSource(musicsLiveData.map {
                musicList -> musicList.applySort(customSortOrder)
        })
    }*/

   /* fun getAllMusicList() : Flow<List<Music>> {
        CoroutineScope(Dispatchers.Default).launch {
            musicList = musicDao.getAllMusicsList()
        }
        return musicList
    }*/
    fun searchMusics(query: String): Flow<List<Music>> {
        //        Log.d("eeeeeeeee","${result.value?.size}")
        return musicDao.searchMusic(query)
    }

    private val customSortFlow = flow { emit(musicsListSortOrderCache.getOrAwait()) }
    suspend fun tryUpdateRecentMusicsCache(): List<Music>  {
        if (shouldUpdatePlantsCache()) {
//            Log.d("dddddddddddddd","tryUpdateRecentMusicsCache(1) ${shouldUpdatePlantsCache()}")
            return fetchRecentMusics() // 미디어스토어에서 처음 설치시 모두 가져오기
        } /// 초기설치여부에 따라
        else  {
//            Log.d("dddddddddddddd","tryUpdateRecentMusicsCache(2) ${shouldUpdatePlantsCache()}")
            dataReset() // Room, isSelected false로 초기화
            return musicList
        } //room에서 가져오기
    }
    suspend fun fetchRecentMusics(): List<Music> {
//        musicDao.deleteAlMusics()
        val musics = mediaService.allMusics() // MediaStoreㅇㅔㅅㅓ ㄱㅏㅈㅕㅇㅗㄱㅣㅇㅇㅇㅁㄴㅇㅗㅗㅗㄹㅓ
        musicDao.insertAll(musics)
        return  musics
    }

    suspend fun fetchAllVideos(): List<Music> {
//        Log.d("dddddddddddddd","fetchAllVideos")
        val videos = mediaService.allVideos()
        musicDao.insertAll(videos)
        Log.d("vvvvvvvvv","${videos.size}")
        return videos
    }


    suspend fun dataReset(){
        mediaService.allMusicFromRoom(musicDao) // 단지 다운로드된 음악만 룸에 추가



//        Log.d("dddddddddddddd","dataReset: ${roomMusicsList?.size}")

    }
    /**
     * Returns true if we should make a network request.
     */

    val prefs = MyApplication.applicationContext().getSharedPreferences("Pref", MODE_PRIVATE)
    private fun shouldUpdatePlantsCache(): Boolean {
        val isFirstRun: Boolean = prefs.getBoolean("isFirstRun", false)
        Log.d("dddddddddddd","shouldUpdatePlantsCache0 ${isFirstRun}")
        if (!isFirstRun) {
//            musicList = queryMusics()// 부팅시 Mediastore에서 전체불러와서 room에 넣고 room에 있는 거 전달, mp3 추가 가능
//            musics = musicList

            Log.d("dddddddddddd","cursor=? ${isFirstRun}")
//                Log.v(TAG, "checkFirstRun: musicList1=> ${musicList}")
            prefs.edit().putBoolean("isFirstRun", true).apply() // ${musicList.size}")
        } else {
            /*val roomMusics = getAllMusics().value!!
            for ((i,  m) in roomMusics.withIndex()) {
                if (m.isSelected == true) {
                    roomMusics[i].isSelected = false
                    update(roomMusics[i])
                    Log.d("dddddddddddd","isFirstRun=? ${roomMusics[i]}")
                }
            }*/
            Log.d("dddddddddddd","isFirstRun=? ${isFirstRun}")
        }
        return !isFirstRun
    }

    private var musicsListSortOrderCache =
        CacheOnSuccess(onErrorFallback = { listOf<String>() }) {
            mediaService.customMusicSortOrder()
        }
    private fun List<Music>.applySort(customSortOrder: List<String>): List<Music> {
        return sortedBy { music ->
            val positionForItem = customSortOrder.indexOf(music.title).let { order ->
                if (order > -1) order else Int.MAX_VALUE
            }
            ComparablePair(positionForItem, music.id)
        }
    }
    @AnyThread
    suspend fun List<Music>.applyMainSafeSort(customSortOrder: List<String>) =
        withContext(Dispatchers.Default) {//디스패처 간에 전환하기 위해 코루틴은 withContext를 사용
            this@applyMainSafeSort.applySort(customSortOrder)
        }
    fun getMusicsWithGenreSort() =
        musicDao.getMusicsWithGenreSorted()
            .switchMap { plantList ->
                liveData {
                    val customSortOrder = musicsListSortOrderCache.getOrAwait()
                    emit(plantList.applyMainSafeSort(customSortOrder))
                }
            }

    suspend fun insert(music: Music) {
        musicDao.insert(music)
    }

    suspend fun insertAll(musics: List<Music>) {
        musicDao.insertAll(musics)
    }
    fun getAllIds(): MutableList<Int> {
        return  musicDao.getAllIds()
    }

    suspend fun getAllMusicsRandom():MutableList<Music> {
        return musicDao.getAllMusicsRandom()
    }
    suspend fun update(music: Music) {
        musicDao.update(music)
        Log.d("performUpdateMusic","updateMusic(),${music.bookmark} currentPos= ${music.currentPosition}, ${music.artist}")
    }

//    val allMusics : Flow<List<Music>> = musicDao.getAllMusics()

    /*fun getAllMusics(): Flow<List<Music>> {
        return musicDao.getAllMusics()
    }*/
    fun search(query: String): List<Music> {
        return musicDao.search(query)
    }

    suspend fun getBookmark(b: Boolean) : MutableList<Music> {
        return musicDao.getBook(b)
    }

    suspend fun delete(id: Int) {
        musicDao.delete(id)
    }
    suspend fun deleteAll() {
        musicDao.deleteAlMusics()
    }

    fun getMusic(id: Int) :Music {
        return musicDao.getMusic(id)
    }

    companion object {
    // For Singleton instantiation
    @Volatile private var instance: HomeRepository? = null

    fun getInstance(musicDao: RoomMusicDao) = //, musicservice: NetworkService) =
        instance ?: synchronized(this) {
            instance ?: HomeRepository(musicDao/*, musicService*/).also { instance = it }
        }
    }
}