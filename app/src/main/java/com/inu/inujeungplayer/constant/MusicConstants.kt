package com.inu.inujeungplayer.constant

import android.os.Build
import android.provider.MediaStore
import com.inu.inujeungplayer.R
import com.inu.inujeungplayer.model.Radio
import com.inu.inujeungplayer.utils.MyApplication


object MusicConstants {
    const val NOTIFICATION_ID_FOREGROUND_SERVICE = 8466503
//    const val DELAY_SHUTDOWN_FOREGROUND_SERVICE: Long = 20000
//    const val DELAY_UPDATE_NOTIFICATION_FOREGROUND_SERVICE: Long = 30000

    object ACTION {
        const val MAIN_ACTION = "music.action.main"
        const val PAUSE_ACTION = "music.action.pause"
        const val PLAY_ACTION = "music.action.play"
        const val REPLAY_ACTION = "music.action.replay"
        const val START_ACTION = "music.action.start"
        const val STOP_ACTION = "music.action.stop"
    }

    object STATE_SERVICE {
        const val PREPARE = 888
        const val PLAY = 777
        const val PAUSE = 503
        const val NOT_INIT = 0
    }

    var allRadio =  mutableListOf(
        mutableListOf("kbs1Radio" , "http://116.39.207.4:8088/kbs1radio.pls"),
        mutableListOf("kbsCoolFM", "http://116.39.207.4:8088/kbs2fm.pls"),
        mutableListOf("kbs2Radio","http://116.39.207.4:8088/kbs2radio.pls"),
        mutableListOf("kbsClassic","http://116.39.207.4:8088/kbsfm.pls"),
        mutableListOf("cbs939","http://aac.cbs.co.kr/cbs939/cbs939.stream/playlist.m3u8"),
        mutableListOf("cbs981","http://aac.cbs.co.kr/cbs981/cbs981.stream/playlist.m3u8"),
        mutableListOf("sbsPower","http://116.39.207.4:8088/sbsfm.pls"),
        mutableListOf("sbsLove","http://116.39.207.4:8088/sbs2fm.pls"),
        mutableListOf("mbcFm","http://116.39.207.4:8088/mbcsfm.pls"),
        mutableListOf("mbcFm4u","http://116.39.207.4:8088/mbcfm.pls"),
        mutableListOf("ytn945","https://radiolive.ytn.co.kr/radio/_definst_/20211118_fmlive/playlist.m3u8"),
        mutableListOf("tbsfm", "https://cdnfm.tbs.seoul.kr/tbs/_definst_/tbs_fm_web_360.smil/playlist.m3u8"),
        mutableListOf("tbsBusan","http://210.96.79.115:1935/busan/myStream/playlist.m3u8?DVR"),
        mutableListOf("arirang","http://amdlive.ctnd.com.edgesuite.net:80/arirang_3ch/arirang_3ch_audio/playlist.m3u8"),
        mutableListOf("WonEumFM","http://157.245.196.186:80/live/livestream.m3u8"),
        mutableListOf("ebsFM","http://ebsonairiosaod.ebs.co.kr/fmradiobandiaod/bandiappaac/playlist.m3u8")
        )
    var allRadioIcon =  listOf( R.drawable.kbs1, R.drawable.kbs_cool_fm,R.drawable.kbs973, R.drawable.kbs_classic,
        R.drawable.cbs939,R.drawable.cbs981, R.drawable.sbs_power, R.drawable.sbslove,R.drawable.mbc_fm,R.drawable.mbc_fm4u,
        R.drawable.ytn945, R.drawable.tbs_fm, R.drawable.tbs_efm,R.drawable.arirangfm, R.drawable.woneum, R.drawable.ebs_fm)
    var allRadio2 =  mutableListOf(
        Radio(0,"kbs1Radio" , "http://116.39.207.4:8088/kbs1radio.pls",R.drawable.kbs1, false, false),
        Radio(1,"kbsCoolFM", "http://116.39.207.4:8088/kbs2fm.pls",R.drawable.kbs_cool_fm,false, false ),
        Radio(2,"kbs2Radio","http://116.39.207.4:8088/kbs2radio.pls",R.drawable.kbs973,false, false),
        Radio(3,"kbsClassic","http://116.39.207.4:8088/kbsfm.pls", R.drawable.kbs_classic,false, false),
        Radio(4,"cbs939","http://aac.cbs.co.kr/cbs939/cbs939.stream/playlist.m3u8",R.drawable.cbs939,false, false),
        Radio(5,"cbs981","http://aac.cbs.co.kr/cbs981/cbs981.stream/playlist.m3u8",R.drawable.cbs981,false, false),
        Radio(6,"sbsPower","http://116.39.207.4:8088/sbsfm.pls", R.drawable.sbs_power,false, false),
        Radio(7,"sbsLove","http://116.39.207.4:8088/sbs2fm.pls", R.drawable.sbslove,false, false),
        Radio(8,"mbcFm","https://sminiplay.imbc.com/aacplay.ashx?channel=sfm&agent=android",R.drawable.mbc_fm,false, false),
        Radio(9,"mbcFm4u","https://sminiplay.imbc.com/aacplay.ashx?channel=mfm&agent=android",R.drawable.mbc_fm4u,false, false),
        Radio(10,"ytn945","https://radiolive.ytn.co.kr/radio/_definst_/20211118_fmlive/playlist.m3u8",R.drawable.ytn945,false, false),
        Radio(11,"tbsfm", "https://cdnfm.tbs.seoul.kr/tbs/_definst_/tbs_fm_web_360.smil/playlist.m3u8", R.drawable.tbs_fm,false, false),
        Radio(12,"tbsBusan","http://210.96.79.115:1935/busan/myStream/playlist.m3u8?DVR", R.drawable.tbs_efm,false, false),
        Radio(13,"arirang","http://amdlive.ctnd.com.edgesuite.net:80/arirang_3ch/arirang_3ch_audio/playlist.m3u8",R.drawable.arirangfm,false, false),
        Radio(14,"WonEumFM","http://157.245.196.186:80/live/livestream.m3u8", R.drawable.woneum,false, false),
        Radio(15,"ebsFM","http://ebsonairiosaod.ebs.co.kr/fmradiobandiaod/bandiappaac/playlist.m3u8", R.drawable.ebs_fm,false, false)
    )

    val projection =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { arrayOf(
        MediaStore.Audio.AudioColumns.ALBUM, // 0
        MediaStore.Audio.AudioColumns.TITLE, // 1
        MediaStore.Audio.AudioColumns.ARTIST,// 2
        MediaStore.Audio.AudioColumns.ALBUM_ID, // 3
        MediaStore.Audio.AudioColumns.DURATION, // 4
        MediaStore.Audio.AudioColumns.DATA, // 5
        MediaStore.Audio.AudioColumns.GENRE,//6
        MediaStore.Audio.AudioColumns.BOOKMARK, //7
        MediaStore.Audio.AudioColumns._ID //8
    ) } else {
        arrayOf(
            MediaStore.Audio.AudioColumns.ALBUM, // 0
            MediaStore.Audio.AudioColumns.TITLE, // 1
            MediaStore.Audio.AudioColumns.ARTIST,// 2
            MediaStore.Audio.AudioColumns.ALBUM_ID, // 3
            MediaStore.Audio.AudioColumns.DURATION, // 4
            MediaStore.Audio.AudioColumns.DATA, // 5
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,//6
            MediaStore.Audio.AudioColumns.BOOKMARK, //7
            MediaStore.Audio.AudioColumns._ID //8
        ) }

    val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} ASC"// DESC"


}