package com.inu.inujeungplayer.constant

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.net.Uri
import com.inu.inujeungplayer.model.Music
import com.inu.inujeungplayer.model.Radio

object MusicDevice {
//    val deviceMusics = mutableListOf<Music>()
    var isAllSearch = true // 전체 reload나 shuffle은 한번만 연속안됨
    /***************************************************/
    var songID  = -1 // songID, musiclist, musicIDList, oldMusic 이번 핵심
    var musicList = listOf<Music>()
    var radioList = listOf<Radio>()
    var musicIDList = mutableListOf<Int>()
//    var radioIDList = mutableListOf<Int>()
    var oldMusic : Music? = null
    var radioID = -1
//    var index = -1  // musicAdapter에 있음
//    var currentSong : Music? = null
    /***************************************************/
//    var oldMusicList = mutableListOf<Music>()
//    var olMusicIndex = -1
//    var songList = mutableListOf<Music>()
    var lyric: String? = ""
    var dataSource: String? = null // 라디오전용 방송 uri
    var titleMain: String? = null
    var titleDetail: String? = null
    var isRadioOn: Boolean = false
    var oldRadio: Radio? = null
    var isVideo = false // 비디오로드 관련
//    var mPlayer: MediaPlayer? = null
    var imageRadioPlaySource: Uri? = null
    var indexOfSong = -1
    var isScreen = 0 // play imageplay 한번만
    var isPlaying = false // mPlayer 멈춤 확인
    var once = true // MusicAdapter #84 , 한번만 실행하게
//    val br: BroadcastReceiver = FragmentPlay.
//    val filter = IntentFilter()
//    var song : Music? = null
}