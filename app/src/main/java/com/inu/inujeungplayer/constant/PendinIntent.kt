package com.inu.inujeungplayer.constant

import android.content.Intent
import com.inu.inujeungplayer.service.ForegroundService
import com.inu.inujeungplayer.utils.MyApplication

object PendinIntent {
    val lPauseIntent = Intent(MyApplication.applicationContext(), ForegroundService::class.java)
    val lPlayIntent = Intent(MyApplication.applicationContext(), ForegroundService::class.java)
    val lReplayIntent = Intent(MyApplication.applicationContext(), ForegroundService::class.java)

    var intent: Intent? = null

}