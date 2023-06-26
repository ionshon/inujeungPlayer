package com.inu.inujeungplayer.ui.home

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.lifecycle.*
import com.inu.inujeungplayer.R
import com.inu.inujeungplayer.model.Music
import com.inu.inujeungplayer.repository.HomeRepository
import com.inu.inujeungplayer.utils.MyApplication
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

private const val TAG = "FragmentListVM"
class HomeViewModelFactory(private val repository: HomeRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
class HomeViewModel internal constructor(
    private val homeRepository: HomeRepository,) : ViewModel() {

//    val musicInNoti: LiveData<Music>
//        get() = homeRepository._musicInNoti // 서비스에서 주입위해
//    var _songid = MutableLiveData<Int>()
//    var songid: LiveData<Int> = _songid
    var musics: LiveData<List<Music>> = homeRepository.readAllData.asLiveData() // h ome //Repository.getMusicsWithGenreSort()

    suspend fun setMusicsData() : List<Music> {
//        Log.d("dddddddddddddd","setMusicsData1")
        return homeRepository.tryUpdateRecentMusicsCache()
//        homeRepository.tryUpdateRecentMusicsCache()
//        launchDataLoad { homeRepository.tryUpdateRecentMusicsCache() } // 단지 추가mp3 룸에 insert
    }
    suspend fun setVideosData() :List<Music> {
//        Log.d("dddddddddddddd","setMusicsData1")
        return homeRepository.fetchAllVideos()
    }

   /* fun getAllmusics() : List<Music> {
        val result = homeRepository.allMusics
        return result
    }*/

//    lateinit var musicList : LiveData<List<Music>>
    var prefs: SharedPreferences? = null

    var _music = MutableLiveData<Music>()
    val music: LiveData<Music> = _music
    fun updateMusicFalse(music: Music) { // old
//        Log.d("performUpdateMusic","updateMusic(), currentPos= ${music.currentPosition}, ${music.artist}")
        CoroutineScope(Dispatchers.IO).launch {
//            performUpdateMusic(music) // mediastore 에러남
            homeRepository.update(music) // 룸 변경된면 musics 관찰하니까 화면 전체 로드됨
            _music.postValue(music) // 라이브데이터 변경
        }
    }
    fun updateMusicTrue(music: Music) {
//        Log.d("performUpdateMusic","updateMusic(), currentPos= ${music.currentPosition}, ${music.artist}")
        viewModelScope.launch {
//            performUpdateMusic(music) // mediastore 에러남
            homeRepository.update(music) // 룸 변경
            _music.postValue(music) // 라이브데이터 변경
        }
    }

    fun deleteMusic(music: Music) {
        viewModelScope.launch {
            homeRepository.delete(music.id)
            _music.postValue(music)
        }
    }
//***********************************************************************************
    private val _snackbar = MutableLiveData<String?>()
    /**
     * Request a snackbar to display a string.
     */
    val snackbar: LiveData<String?>
        get() = _snackbar

    private val _spinner = MutableLiveData<Boolean>(false)
    /**
     * Show a loading spinner if true
     */
    val spinner: LiveData<Boolean>
        get() = _spinner
      // 여기서 전체소팅이나 불러와서 라이브
    /*= growZone.switchMap { growZone ->
        if (growZone == NoGrowZone) {
            plantRepository.plants
        } else {
            plantRepository.getPlantsWithGrowZone(growZone)
        }
    }*/
    /**
     * Called immediately after the UI shows the snackbar.
     */
    fun onSnackbarShown() {
        _snackbar.value = null
    }


    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            try {
                _spinner.value = true
                block()
            } catch (error: Throwable) {
                _snackbar.value = error.message
            } finally {
                _spinner.value = false
            }
        }
    }

    private suspend fun checkFirstRun() {
        prefs = MyApplication.applicationContext().getSharedPreferences("Pref", MODE_PRIVATE)
        val isFirstRun: Boolean = prefs!!.getBoolean("isFirstRun", true)
        if (isFirstRun) {
//            musicList = queryMusics()// 부팅시 Mediastore에서 전체불러와서 room에 넣고 room에 있는 거 전달, mp3 추가 가능
//            musics = musicList

//                Log.v(TAG, "checkFirstRun: musicList1=> ${musicList}")
            prefs!!.edit().putBoolean("isFirstRun", false).apply() // ${musicList.size}")
        } else {
//            musics  = repository.allMusics //repository.getAllMusics()
//            Log.v(TAG, "checkFirstRun: musicList2=> ${musics.value?.size}")
            // ${musicList.size}")
        }
    }
    fun searchQueryMusics(query: String?): LiveData<List<Music>> { // ROOM 에서 서치, 버튼
//        isAllSearch = true
        //        Log.i("reLoadMusics(searchQueryMusics)", "${query}, ${result.value?.size} ")
        return query.let { homeRepository.searchMusics("%${it}%") }.asLiveData()
//            isPlayReloadReset()
//        isSearch = true
    }

    fun reLoadMusics(): LiveData<List<Music>> {
        return homeRepository.readAllData.asLiveData()
    }
    fun shuffle() {
        CoroutineScope(Dispatchers.IO).launch {
//            _musics.postValue(repository.getAllMusicsRandom())
            Log.i(TAG, "reLoadMusics(shuffle) ") // ${musicList.size} musics")
        }
        /*val seed = System.currentTimeMillis()
        if (isSearch) { // 서치나 북마크에서는 그 목록에서 셔플
            Log.i(TAG, "reLoadMusics(shuffle2) ${musicList.size} musics")
            musicList = musicList.shuffled(Random(seed)) as MutableList<MediaStoreMusic>
            _musics.postValue(musicList)
            index = musicList.indexOf(song)
        } else if (isAllSearch) { // 아니면 전체 room에서 셔플
            Log.i(TAG, "reLoadMusics(shuffle3) ${musicList.size} musics")
            if (index != -1 && index < musicList.size) song = musicList[index]
            CoroutineScope(Dispatchers.IO).launch {
                musicList.clear()
                musicList = repository.getAllMusicsRandom()
                _musics.postValue(musicList) // 라이브데이터, 화면 설정
                index = musicList.indexOf(song)
            }
            isAllSearch = false
        }*/
//        Log.i(TAG, "reLoadMusics(shuffle) ${musicList.size} musics")
    }

    fun showInfo(view: View, music: Music) { // 업그레이드 필요
        val inflater: LayoutInflater = LayoutInflater.from(view.context)
        //view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.popup_info, null)
        val textViewInfo: TextView = popupView.findViewById(R.id.textView_info)

        textViewInfo.text = "\n\n- ID:  ${music.id}\n\n- Title: \n    ${music.title}\n\n" +
                "- bookmark: ${music.bookmark} \n\n" +
                "- Artist: \n   ${music.artist}\n\n- Genre or Path: \n    ${music.genre}\n\n- isSelected: \n  ${music.isSelected}\n" +
                "\n- Path: ${music.path}\n\n"

        // create the popup window
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        // dismiss the popup window when touched
        popupView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                popupWindow.dismiss()
                return true
            }
        })
    }
}