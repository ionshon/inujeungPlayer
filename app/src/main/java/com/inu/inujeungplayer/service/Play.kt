package com.inu.inujeungplayer.service

import android.app.PendingIntent
import android.content.Context
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.util.Log
import com.inu.inujeungplayer.adapter.MusicAdapter.Companion.index
import com.inu.inujeungplayer.constant.MusicConstants
import com.inu.inujeungplayer.constant.MusicDevice.lyric
import com.inu.inujeungplayer.constant.MusicDevice.musicIDList
import com.inu.inujeungplayer.constant.MusicDevice.musicList
import com.inu.inujeungplayer.constant.MusicDevice.songID
import com.inu.inujeungplayer.constant.MusicDevice.titleDetail
import com.inu.inujeungplayer.constant.MusicDevice.titleMain
import com.inu.inujeungplayer.constant.PendinIntent
import com.inu.inujeungplayer.utils.MetaExtract.getLyric
import com.inu.inujeungplayer.utils.MyApplication
import com.inu.inujeungplayer.utils.bounceIconSetting

object Play {
    var mPlayer: MediaPlayer? = null
//    val playViewModel: HomeViewModel by  MyApplication.applicationContext().vie {
//        Injector.provideHomeListViewModelFactory(requireContext()) }
    val mLock = Any()
    private var mWakeLock: PowerManager.WakeLock? = null
    private var mWiFiLock: WifiManager.WifiLock? = null

    fun nextMusicPlay() { // 플레이 completion시, nextPlay 클릭시
        index = musicIDList.indexOf(songID)
        if (musicIDList.size > 0) { // 곡돌이 화면에 있으면 => index는 목록에 있으면 그 index 없으면 0
            if (index == musicIDList.size - 1) { // 리스트중 마지막곡
                songID = musicIDList[0]

//                listeners.playViewModel._songid.value = songID
//                index = 0
//                songAtAdaptr = musicList[index]
            }
            else {
                songID = musicIDList[index+1]
//                listeners.playViewModel._songid.value = songID
//                index += 1
//                songAtAdaptr = musicList[index]
            }

//            bounceIconSetting()
//            isPlaySet() // oncompletion
            titleMain = "Music" // noti 화면
            index = musicIDList.indexOf(songID)
            titleDetail = musicList[index].title
            if (musicList[musicIDList.indexOf(songID)].currentPosition != 1) {
                lyric = getLyric(musicList[index].path)
            } else lyric = "                 "

            PendinIntent.lPlayIntent.action = MusicConstants.ACTION.PLAY_ACTION
            val lPendingPlayIntent =
                PendingIntent.getService(
                    MyApplication.applicationContext(),
                    0,
                    PendinIntent.lPlayIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            try {
                lPendingPlayIntent.send()
            } catch (e: PendingIntent.CanceledException) {
                e.printStackTrace()
            }
//                Log.d("servicee:1","isClickedPause=> ")
        } else { // 곡이 화면에 없을때(검색이후) => index는 -1
            PendinIntent.lPauseIntent.action = MusicConstants.ACTION.PAUSE_ACTION
            val lPendingPlayIntent =
                PendingIntent.getService(
                    MyApplication.applicationContext(),
                    0,
                    PendinIntent.lPauseIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            try {
                lPendingPlayIntent.send()
            } catch (e: PendingIntent.CanceledException) {
                e.printStackTrace()
            }
//            Log.d("servicee:2","isClickedPause=> ")
        }
    }

    fun prevMusicPlay() {
//        if (musicList.isNotEmpty()) {
//             if (index == -1) index = 0
        index = musicIDList.indexOf(songID)
        if (index == 0) {
            songID = musicIDList[musicIDList.size-1] //musicList[musicIDList.size - 1].id
        } else {
            songID = musicIDList[index -1] // += -1
        }
        titleMain = "Music"
        index = musicIDList.indexOf(songID)
        titleDetail = musicList[index].title
        if (musicList[musicIDList.indexOf(songID)].currentPosition != 1) {
            lyric = getLyric(musicList[index].path)
        } else lyric = "                 "

//         isPlaySet()
//        bounceIconSetting()

        PendinIntent.lPlayIntent.action = MusicConstants.ACTION.PLAY_ACTION
        val lPendingPlayIntent =
            PendingIntent.getService(
                MyApplication.applicationContext(),
                0,
                PendinIntent.lPlayIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        try {
            lPendingPlayIntent.send()
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
//        }
    }

    fun destroyPlayer() {
        if (mPlayer != null) {
            try {
                mPlayer!!.reset()
                mPlayer!!.release()
//                Log.d(TAG, "Player destroyed")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
//                Log.d(TAG, "Player nulled")
                mPlayer = null
            }
        }
        unlockWiFi()
        unlockCPUi()
    }

    private fun unlockWiFi() {
        if (mWiFiLock != null && mWiFiLock!!.isHeld) {
            mWiFiLock!!.release()
            mWiFiLock = null
            Log.d(ForegroundService.TAG, "Player unlockWiFi()")
        }
    }

    fun lockCPUi() {
        val mgr = MyApplication.applicationContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.javaClass.simpleName)
        mWakeLock?.acquire(10*60*1000L)
        Log.d(ForegroundService.TAG, "Player lockCPU()")
    }

    private fun unlockCPUi() {
        mWakeLock?.let {
            if (it.isHeld) {
                it.release()

                Log.d(ForegroundService.TAG, "Player unlockCPU()")
            }
        }
        mWakeLock = null
    }

    fun lockWiFi() {
        val connectivityManager = MyApplication.applicationContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val lWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if(connectivityManager.isDefaultNetworkActive){
            //인터넷 됨
            val manager = MyApplication.applicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
            manager.let {
                mWiFiLock = manager.createWifiLock(
                    WifiManager.WIFI_MODE_FULL_HIGH_PERF, ForegroundService::class.java.simpleName)
                mWiFiLock?.acquire()
            }
//            Log.d(TAG, "Player lockWiFi() 인터넷 됨")
        }else{
            Log.d(ForegroundService.TAG, "Player lockWiFi() 인터넷 안됨")
            //인터넷 안됨
        }
    }
}