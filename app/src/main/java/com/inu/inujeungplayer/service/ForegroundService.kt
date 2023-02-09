package com.inu.inujeungplayer.service

import android.app.*
import android.content.ComponentName
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.inu.inujeungplayer.MainActivity
import com.inu.inujeungplayer.R
import com.inu.inujeungplayer.constant.MusicConstants
import com.inu.inujeungplayer.constant.MusicDevice
import com.inu.inujeungplayer.constant.MusicDevice.dataSource
import com.inu.inujeungplayer.constant.MusicDevice.isPlaying
import com.inu.inujeungplayer.constant.MusicDevice.isRadioOn
import com.inu.inujeungplayer.constant.MusicDevice.isScreen
import com.inu.inujeungplayer.constant.MusicDevice.isVideo
import com.inu.inujeungplayer.constant.MusicDevice.musicIDList
import com.inu.inujeungplayer.constant.MusicDevice.musicList
import com.inu.inujeungplayer.constant.MusicDevice.songID
import com.inu.inujeungplayer.constant.MusicDevice.titleDetail
import com.inu.inujeungplayer.constant.MusicDevice.titleMain
import com.inu.inujeungplayer.constant.PendinIntent.intent
import com.inu.inujeungplayer.constant.PendinIntent.lPauseIntent
import com.inu.inujeungplayer.constant.PendinIntent.lReplayIntent
import com.inu.inujeungplayer.utils.MyApplication
import com.inu.inujeungplayer.service.Play.destroyPlayer
import com.inu.inujeungplayer.service.Play.mPlayer
import com.inu.inujeungplayer.ui.dashboard.DashboardFragment
import com.inu.inujeungplayer.utils.bounceIconSetting
import com.inu.inujeungplayer.utils.goldLingSetting

class ForegroundService : Service(),MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener {


//    private val mHandler = Handler(Looper.getMainLooper())//Handler()

//    private var mUriRadio: Uri? = null
    private var notification: Notification? = null
    private var mNotificationManager: NotificationManager? = null
    /* private val mTimerUpdateHandler = Handler(Looper.getMainLooper()) //Handler()
     private val mTimerUpdateRunnable: Runnable = object : Runnable {
         override fun run() {
             mNotificationManager?.notify(MusicConstants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNoti())
             mTimerUpdateHandler.postDelayed(this, MusicConstants.DELAY_UPDATE_NOTIFICATION_FOREGROUND_SERVICE)
         }
     }*/

//    private var isClickedStop = false
//    private var isClickedPause = false

    override fun onCreate() {
        super.onCreate()
//        Log.d("isServiceRunning", "onCreate()")
        state = MusicConstants.STATE_SERVICE.NOT_INIT
        mNotificationManager = getSystemService(NotificationManager::class.java)
        isServiceRunning = true
        mPlayer = MediaPlayer()

        /*if (thread.isAlive) {
            thread.interrupt()
        }
        thread.start()*/
    }
    override fun startService(service: Intent?): ComponentName? {
//        Log.d("MusicViewPager22", "startService() called")
        if (!isServiceRunning!!) {
            val serviceIntent = Intent(this, ForegroundService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        }

        return super.startService(service)
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(FOREGROUND_CHANNEL_ID, "My_FOREGROUND", NotificationManager.IMPORTANCE_HIGH)
        serviceChannel.setSound(null, null) //soundUri, audioAttributes)
        mNotificationManager?.createNotificationChannel(serviceChannel)
    }

    private fun prepareNoti(): Notification {
        createNotificationChannel()

        intent = Intent(this, MainActivity::class.java).also {
            it.action = Intent.ACTION_MAIN
            it.addCategory(Intent.CATEGORY_LAUNCHER)
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        val mainPendingIntent = PendingIntent.getActivity(this,0, intent,  PendingIntent.FLAG_IMMUTABLE)

        val notificationIntent = Intent(this, ForegroundService::class.java)
        notificationIntent.action = MusicConstants.ACTION.MAIN_ACTION
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

//        val lPlayIntent = Intent(this, ForegroundService::class.java)
//        lPlayIntent.action = MusicConstants.ACTION.PLAY_ACTION
//        val lPendingPlayIntent = PendingIntent.getService(this, 0, lPlayIntent, PendingIntent.FLAG_IMMUTABLE)
        lReplayIntent.action = MusicConstants.ACTION.REPLAY_ACTION
        val lPendingReplayIntent = PendingIntent.getService(this, 0, lReplayIntent, PendingIntent.FLAG_IMMUTABLE)

        lPauseIntent.action = MusicConstants.ACTION.PAUSE_ACTION
        val lPendingPauseIntent = PendingIntent.getService(this, 0, lPauseIntent, PendingIntent.FLAG_IMMUTABLE)

        val lStopIntent = Intent(this, ForegroundService::class.java).also {
            it.action = MusicConstants.ACTION.STOP_ACTION
        }
        val lPendingStopIntent = PendingIntent.getService(this, 0, lStopIntent, PendingIntent.FLAG_IMMUTABLE)

        val lRemoteViews = RemoteViews(packageName, R.layout.radio_notification)
        lRemoteViews.setOnClickPendingIntent(R.id.ui_notification_close_button, lPendingStopIntent)  // stop 인텐트 실행하라
        lRemoteViews.setOnClickPendingIntent(R.id.linearLayout, mainPendingIntent) // 앱 띄우기

        when(state) {
            MusicConstants.STATE_SERVICE.PAUSE -> {
                Log.d("PAUSE in Noti", "$state")
                lRemoteViews.setTextViewText(R.id.text_title1, titleMain)
                lRemoteViews.setTextViewText(R.id.text_title2,titleDetail) // musicList[musicIDList.indexOf(songID)].title) // titleDe
                lRemoteViews.setViewVisibility(R.id.ui_notification_progress_bar, View.INVISIBLE)
                lRemoteViews.setOnClickPendingIntent(R.id.ui_notification_player_button, lPendingReplayIntent)
                lRemoteViews.setImageViewResource(R.id.ui_notification_player_button, R.drawable.ic_play_arrow_white)
            }
            MusicConstants.STATE_SERVICE.PLAY -> {
                Log.d("PLAY in Noti", "$state")
                lRemoteViews.setViewVisibility(R.id.ui_notification_progress_bar, View.INVISIBLE)
                lRemoteViews.setTextViewText(R.id.text_title1, titleMain)
                lRemoteViews.setTextViewText(R.id.text_title2, titleDetail) // musicList[musicIDList.indexOf(songID)].title)
                lRemoteViews.setOnClickPendingIntent(R.id.ui_notification_player_button, lPendingPauseIntent)
                lRemoteViews.setImageViewResource(R.id.ui_notification_player_button, R.drawable.ic_pause_white)
//                lRemoteViews.setTextViewText(R.id.textTitle, dataSource)
            }
                MusicConstants.STATE_SERVICE.PREPARE -> {
//                Log.d("PREPARE in Noti", "$state")
                lRemoteViews.setTextViewText(R.id.text_title1, titleMain)
                lRemoteViews.setTextViewText(R.id.text_title2, titleDetail) //musicList[musicIDList.indexOf(songID)].title)
                lRemoteViews.setViewVisibility(R.id.ui_notification_progress_bar, View.VISIBLE)
                lRemoteViews.setOnClickPendingIntent(R.id.ui_notification_player_button, lPendingPauseIntent)
                lRemoteViews.setImageViewResource(R.id.ui_notification_player_button, R.drawable.ic_pause_white)
            }
        }

        notification = NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setContent(lRemoteViews)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setSound(null)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        return notification as Notification
    }

    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        isServiceRunning = true
//        Log.i(TAG, "Received start Intent2,  ")
        when (intent?.action) {
            MusicConstants.ACTION.START_ACTION -> {
                if (!isRadioOn) bounceIconSetting()
                else goldLingSetting()
                isPlaying = true
                isScreen = 0
//                Log.i(TAG, "Clicked start Intent22,  ")
                state = MusicConstants.STATE_SERVICE.PREPARE
//                Log.d("kkkk in lremoteView:", "${bindingNoti?.textTitle2?.text}")
                startForeground(MusicConstants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNoti())

                destroyPlayer()
                initPlayer()
                play()
//                if (isRadioOn) isLoadingBubble = true
            }

            MusicConstants.ACTION.PAUSE_ACTION -> {
                if (!isRadioOn)bounceIconSetting()
                else goldLingSetting()
                isPlaying = false
                isScreen = 0
                state = MusicConstants.STATE_SERVICE.PAUSE
                mNotificationManager?.notify(MusicConstants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNoti())
                Log.i(TAG,"Clicked pause0,")
                mPlayer?.pause()
            }

            MusicConstants.ACTION.STOP_ACTION -> {
//                Log.i(TAG, "Clicked stop Intent,  ${mPlayer?.isPlaying}")
                stopForeground(STOP_FOREGROUND_REMOVE)
                destroyPlayer()
                stopSelf()
                finish()
            }

            MusicConstants.ACTION.PLAY_ACTION -> {
                if (!isRadioOn)bounceIconSetting()
                else goldLingSetting()
                isPlaying = true
                isScreen = 0
                state = MusicConstants.STATE_SERVICE.PREPARE
                mNotificationManager?.notify(MusicConstants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNoti())
//                Log.i(TAG, "Clicked play")
                destroyPlayer()
                initPlayer() // MediaPlayer 할당, 리스너 설정
                play() //
            }

            MusicConstants.ACTION.REPLAY_ACTION -> {
                if (!isRadioOn)bounceIconSetting()
                else goldLingSetting()
                isPlaying = true
                isScreen = 0
                state = MusicConstants.STATE_SERVICE.PLAY
                mNotificationManager?.notify(MusicConstants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNoti())
//                Log.i(TAG, "Clicked replay")
                mPlayer?.start()
            }
        }
        return  START_REDELIVER_INTENT // super.onStartCommand(intent, flags, startId) //
    }
    fun initPlayer() {
//        Log.d(TAG,"initPlayer() called")
        val aa = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mPlayer = MediaPlayer()
        mPlayer!!.setAudioAttributes(aa)
        mPlayer!!.setOnErrorListener(this)
        mPlayer!!.setOnPreparedListener(this)
        mPlayer!!.setOnBufferingUpdateListener(this)
//        mPlayer!!.setScreenOnWhilePlaying(true)
        mPlayer!!.setOnInfoListener { mp, what, extra ->
//            Log.d(TAG, "Player onInfo(), what:$what, extra:$extra, 네트워크 단절시")
            if (what == 701) { // 네트워크 단절시 MediaPlayer is temporarily pausing playback internally in order to buffer more data.
                play()
            }
            false
        }
        Play.lockWiFi()
        Play.lockCPUi()
    }

    fun play() {
        /* if (!isRadioOn && index != -1) {
             MusicDevice.dataSource = musicList[index].path
         }*/
        synchronized(Play.mLock) {
            try {
                if (mPlayer == null) {
//                    Log.d("workerPlay()", "mPlayer == null")
                    initPlayer() // 플레이어 할당, 리스너 설정
                }
                mPlayer?.reset()
                mPlayer?.setDisplay(null)
                mPlayer?.setVolume(1.0f, 1.0f)
                val source = if (!isRadioOn) { // mp3
                    val playSong = musicList[musicIDList.indexOf(songID)]
                    isVideo = playSong.currentPosition == 1 // 비디오 여부 플래그
                    playSong.path
                } else {
                    isVideo = false
                    dataSource // radio
                }

                Log.d("workerPlay()", "${source}")
                mPlayer?.setDataSource(
                    MyApplication.applicationContext(),
                    Uri.parse(source)
                )// dataSource)), MusicAdapter.songAtAdaptr.path))
                mPlayer?.prepareAsync()

            } catch (e: Exception) {
                destroyPlayer()
                e.printStackTrace()
            }
        }
        state = MusicConstants.STATE_SERVICE.PLAY
        mNotificationManager?.notify(MusicConstants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNoti())
    }

    private fun finish() {
//        Log.d(TAG, "finishhhhh() called${musicList[index].title}, ${musicList[index].isSelected}")
        MainActivity().moveTaskToBack(true)						// 태스크를 백그라운드로 이동
        MainActivity().finishAndRemoveTask()					// 액티비티 종료 + 태스크 리스트에서 지우기
        Process.killProcess(Process.myPid())	// 앱 프로세스 종료
    }

    override fun onDestroy() {
//        Log.d(TAG, "onDestroy() called")
        isServiceRunning = false
        state = MusicConstants.STATE_SERVICE.NOT_INIT

        // call MyReceiver which will restart this service via a worker
        /*if (!isClickedStop && !isClickedPause){
            val broadcastIntent = Intent(this, MyReceiver::class.java)
            sendBroadcast(broadcastIntent) // 여기가 시작점
        }*/

        super.onDestroy()
    }

    companion object {
        var isServiceRunning: Boolean? = null
//        var isAlbum: Boolean = true // isScreen 으로 대체
        private const val FOREGROUND_CHANNEL_ID = "foreground_channel_id"
        val TAG = ForegroundService::class.java.simpleName
        var state = MusicConstants.STATE_SERVICE.NOT_INIT
    }

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
//        Log.d(TAG, "Player onError() what:$what")
        destroyPlayer()
//        mHandler.postDelayed(mDelayedShutdown, MusicConstants.DELAY_SHUTDOWN_FOREGROUND_SERVICE)
//        mNotificationManager?.notify(MusicConstants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNoti()) //prepareNotification())
        state = MusicConstants.STATE_SERVICE.NOT_INIT
        return false
    }

    override fun onPrepared(mp: MediaPlayer?) {
//        FragmentPlay.realTotalLen = mPlayer?.duration ?: 0
        DashboardFragment.totalLen = if (!isRadioOn) {
            mPlayer?.duration!!
        } else 9900000
//        isAlbum = true // 정적화면 변경용 isScreen으로 대체
//        if (isRadioOn) MusicDevice.isLoadingBubble = false
//        Log.d("isLoadingBubble(onPrepared)","${MusicDevice.isLoadingBubble}")

        state = MusicConstants.STATE_SERVICE.PLAY
//        mNotificationManager?.notify(MusicConstants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNoti()) //prepareNotification())
        try {
            mPlayer?.setWakeMode(MyApplication.applicationContext(), PowerManager.PARTIAL_WAKE_LOCK)
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        tvTotalLen = SongTimer().milliSecondsToTimer(totalLen.toLong())//totalLen..toLong()?.let { songTimer.milliSecondsToTimer(it) }
        mPlayer?.start()
        mPlayer?.setOnCompletionListener(this)

    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        Log.d(TAG, "Player onBufferingUpdate():$percent")
    }

    override fun onCompletion(mp: MediaPlayer?) { // 라디오는 클릭 외에 끝이 없음
//        isClickedPause = true
        MusicDevice.isAllSearch = true
        MusicDevice.once = true // 중요, 맨나중에 필요
        isScreen = 0 // play 화면 변경 필요시



//        Play.isPlaySet()
        state = MusicConstants.STATE_SERVICE.PAUSE
//        mNotificationManager?.notify(MusicConstants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNoti())
        Play.nextMusicPlay() // 여기서 id 변경 아래서 주입(이전에는 index로)

//        Log.i("musicInNoti", "onCompletion ${musicIDList.indexOf(songID)}")
//        if (index != -1) MyApplication.repository?.serviceNotiChanged(musicList[index])
    }


/*
    private val mDelayedShutdown = Runnable {
//        unlockWiFi()
//        unlockCPU()
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }*/

}

