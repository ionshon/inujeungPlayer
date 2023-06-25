package com.inu.inujeungplayer.ui.radio

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inu.inujeungplayer.constant.MusicConstants.allRadio
import com.inu.inujeungplayer.constant.MusicConstants.allRadio2
import com.inu.inujeungplayer.constant.MusicConstants.allRadioIcon
import com.inu.inujeungplayer.model.Music
import com.inu.inujeungplayer.model.Radio
import com.inu.inujeungplayer.repository.RadioRepo
import com.inu.inujeungplayer.utils.MyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RadioViewModel(private val radioRepo: RadioRepo) : ViewModel() {

/*    private val _text = MutableLiveData<String>().apply {
        value = "This is radio Fragment"
    }*/
//    val text: LiveData<String> = _text
    private var radioList = mutableListOf<Radio>()
    var _radio = MutableLiveData<Radio>()
    var radio: LiveData<Radio> = _radio
    private val _radios = MutableLiveData<List<Radio>>()
    val radios: LiveData<List<Radio>> get() = _radios
    //    private val _radio = MutableLiveData<Radio>()
//    val radio: LiveData<Radio> get() = _radio
    private var contentObserver: ContentObserver? = null

//    val radioInSetStream: LiveData<Radio> get() = radioRepo.radioInSetStream

    fun updateRadioFalse(radio: Radio) { // old
        CoroutineScope(Dispatchers.IO).launch {
            radioRepo.update(radio) // 룸 변경된면 musics 관찰하니까 화면 전체 로드됨
            _radio.postValue(radio) // 라이브데이터 변경
//            Log.d("itemClicksda(updateRadioFalse)","${radio}")
        }
    }
    fun updateRadioTrue(radio: Radio) {
//        Log.d("performUpdateMusic","updateMusic(), currentPos= ${music.currentPosition}, ${music.artist}")
        viewModelScope.launch {
//            performUpdateMusic(music) // mediastore 에러남
            radioRepo.update(radio) // 룸 변경
            _radio.postValue(radio) // 라이브데이터 변경
//            Log.d("itemClicksda(updateRadioTrue)","${radio}")
        }
    }
    fun loadRadios(query: String?) {
        viewModelScope.launch {
            for (r in allRadio2) {
                if (r.addr.contains("m3u8")) {
                    radioList.add(r)
//                    Log.d("itemClicksda(updateRadioTrue)","${r.addr}")
                }
            }
//            radioList = queryRadios() //allRadio2//
//            Log.v("FragmentRadioVM", "Found in loadRadios queryRadios: ${radioList.size} Radios")
            _radios.postValue(radioList) // 룸에서 꺼낸거

//            Log.v("FragmentRadioVM", "Found in loadRadios: ${radioList.size} Radios")
            if (contentObserver == null) {
                contentObserver =
                    MyApplication.applicationContext().contentResolver.registerObserver(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    ) {
                        CoroutineScope(Dispatchers.IO).launch {
                            loadRadios(query) // contentObserver null이면 다시 실행
                        }
                    }
            }
        }
    }

    private suspend fun queryRadios(): MutableList<Radio> {
//        var radios = mutableListOf<Radio>()
        withContext(Dispatchers.IO){
//            radioRepo.deleteAll()
            allRadio.mapIndexed {index, data ->
                val radio = Radio(index, data[0], data[1], allRadioIcon[index], false, false)
//                Log.d("allRadio","${id}, ${data[0]}, ${data[1]}, ${data[2]}")
//                radioRepo.deleteAll()
                if (!radio.addr.contains(".pls")) {
                    radioRepo.insert(radio)
//                    radioRepo.update((radio))
                }
                //                radioList.add(radio)
            }
//            radioRepo.insertAll(allRadio2)
            radioList = radioRepo.getAllradios()
        }
//        Log.d("FragmentRadioVM", "Found in radioRepo.getAllradios: ${radioList.size} Radios")
        return radioList
    }

    suspend fun deleteAllRadio() {
        radioRepo.deleteAll()
    }
    private fun ContentResolver.registerObserver(
        uri: Uri,
        observer: (selfChange: Boolean) -> Unit
    ): ContentObserver {
        val contentObserver = object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean) {
                observer(selfChange)
            }
        }
        registerContentObserver(uri, true, contentObserver)
        return contentObserver
    }
}