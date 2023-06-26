package com.inu.inujeungplayer.ui.dashboard

import android.app.Activity
import android.app.PendingIntent
import android.content.Context.POWER_SERVICE
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.*
import android.view.View.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.inu.inujeungplayer.R
import com.inu.inujeungplayer.adapter.MusicAdapter.Companion.index
import com.inu.inujeungplayer.constant.MusicConstants
import com.inu.inujeungplayer.constant.MusicDevice
import com.inu.inujeungplayer.constant.MusicDevice.isPlaying
import com.inu.inujeungplayer.constant.MusicDevice.isRadioOn
import com.inu.inujeungplayer.constant.MusicDevice.isScreen
import com.inu.inujeungplayer.constant.MusicDevice.isVideo
import com.inu.inujeungplayer.constant.MusicDevice.lyric
import com.inu.inujeungplayer.constant.MusicDevice.musicIDList
import com.inu.inujeungplayer.constant.MusicDevice.musicList
import com.inu.inujeungplayer.constant.MusicDevice.songID
import com.inu.inujeungplayer.constant.MusicDevice.titleDetail
import com.inu.inujeungplayer.constant.PendinIntent
import com.inu.inujeungplayer.databinding.FragmentDashboardBinding
import com.inu.inujeungplayer.model.Music
import com.inu.inujeungplayer.service.ForegroundService
import com.inu.inujeungplayer.service.MyHandler
import com.inu.inujeungplayer.service.Play.mPlayer
import com.inu.inujeungplayer.service.Play.nextMusicPlay
import com.inu.inujeungplayer.service.Play.prevMusicPlay
import com.inu.inujeungplayer.ui.home.HomeViewModel
import com.inu.inujeungplayer.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class DashboardFragment : Fragment(), SurfaceHolder.Callback {
    var clickTime = 0L // exocontrolview 관련
    val seekForwardTime = 15000
    var orientationConfig = false
    val pm = MyApplication.applicationContext().getSystemService(POWER_SERVICE) as PowerManager

    companion object {
        var totalLen = 30000

        var holder: SurfaceHolder? = null
        /*fun videoPlay(music: Music) { // 실행 필요 없음
            Log.d("itemClickkkkkk(companion)","${music.genre}, ${music.path}")
            try {
                mPlayer?.setDataSource(MyApplication.applicationContext(), Uri.parse(music.contentUri)) //"http://techslides.com/demos/sample-videos/small.mp4")
            } catch (e: java.lang.Exception) {
                Log.e("TAG", "ERROR", e)
            }
            mPlayer?.setDisplay(holder)
            mPlayer?.prepareAsync()
        }*/
//        var realTotalLen = 0
//        var isPlaying = false
    }
    private fun threadRun() {
        if (totalLen >= 0) { // 음악이면
            mHandler.removeCallbacksAndMessages(null)
            mHandler.post(r)
        }
    }
    var isThreadRun = true
    private val mHandler = MyHandler() //Handler()
    private val r: Runnable = object : kotlinx.coroutines.Runnable {
        override fun run() {
//            Log.d("playFlag", "핸들러 스레드 온")

            ////////exoControlView/////////////////
            if (System.currentTimeMillis() - clickTime < 1500) {
//                Log.d("clickPosition", "$currentPosition - $clickTime")
                binding.exoControlView.visibility = VISIBLE
            } else binding.exoControlView.visibility = GONE
            /////////////////////////////////////////
            CoroutineScope(Dispatchers.Main).launch {
                currentPosition = mPlayer?.currentPosition ?: 0
                seekBar.max = totalLen
                seekBar.progress = currentPosition
            }
            if (isScreen < 1) { // 한번만 실행하게
                screenSet()
                try {
                    if (!isRadioOn) mPlayer?.setDisplay(holder)
                } catch (e: Exception) {}

                isScreen+=1
            }
            if (isThreadRun) {
                if (pm.isInteractive) {
                    mHandler.postDelayed(this, 500)
                } else {
                    mHandler.removeCallbacksAndMessages(null)
                }
            }
        }
    }

//    val playViewModel: HomeViewModel by  viewModels {Injector.provideHomeListViewModelFactory(requireContext()) }

    private var _binding: FragmentDashboardBinding? = null
    lateinit var songTimer: SongTimer
    lateinit var imagePlay: ImageView
    lateinit var centerImageView: ImageView
    lateinit var playingImage: ImageView
    lateinit var seekBar: SeekBar
    lateinit var textTitle: TextView
    lateinit var textLyric: TextView
    lateinit var surfaceFrame : FrameLayout
    lateinit var wm : WindowManager
//    lateinit var window: Window
//    private val layoutStateChangeCallback = LayoutStateChangeCallback()
    var currentPosition: Int = 0

//    private lateinit var musicAdapter: MusicAdapter
    var surfaceView: SurfaceView? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val packageName = MyApplication.applicationContext().packageName

        // 절전모드로 라디오 정지 방지
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
            } else {
                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, 0)
            }
        }
        // 비디오관련 //////////////////////////////////////////////
        wm = requireContext().getSystemService(WINDOW_SERVICE) as WindowManager
        surfaceFrame = binding.surfaceFrame
//        musicAdapter =  MusicAdapter( { music, view -> menuActions(music, view) },
//            { music -> itemClick(music) }, { music -> artistLongClick(music)})
        surfaceView = binding.surfaceView
        holder = surfaceView?.holder
        holder?.addCallback(this)

        //////////////////////////////////////////////////////////////////////
        //전체화면 관련
//        window = MyApplication.applicationContext().getSystemService(WINDOW_SERVICE) as Window

        /*val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }*/
//****************************************************************************
        songTimer = SongTimer()
        centerImageView = binding.imageView
        playingImage = binding.imagePlaying
        imagePlay = binding.imagePlay
        textTitle = binding.textViewTitle
        textTitle.setHorizontallyScrolling(true)
        textTitle.isSelected = true
        textLyric = binding.textViewLyric
        seekBar = binding.seekBar
        val seekBarHint: TextView = binding.textViewTimeMoving

        Glide.with(requireContext())
            .load(R.drawable.ic_play)
            .into(imagePlay)


        surfaceView?.setOnClickListener {
            if (orientationConfig) {
                binding.exoControlView.visibility = VISIBLE
                clickTime = System.currentTimeMillis()
            }
        }

        imagePlay.setOnClickListener {
//            Log.d("imagePlayClick","$isRadioOn")
           /* if (!isRadioOn) //bounceIconSetting()
            else
                goldLingSetting()*/

            when(ForegroundService.state) {
                MusicConstants.STATE_SERVICE.PAUSE -> {
                    PendinIntent.lPlayIntent.action = MusicConstants.ACTION.REPLAY_ACTION
                    val lPendingPlayIntent = PendingIntent.getService(context, 0,
                        PendinIntent.lPlayIntent, PendingIntent.FLAG_IMMUTABLE)
                    try {
                        lPendingPlayIntent.send()
                    } catch (e: PendingIntent.CanceledException) {
                        e.printStackTrace()
                    }
                    Glide.with(requireContext())
                        .load(R.drawable.ic_pause)
                        .into(imagePlay)
                    if (isRadioOn) {
                        Glide.with(requireContext()).load(R.raw.colorloading96).into(playingImage)
                    } else  Glide.with(requireContext()).load(R.raw.musicloader96).into(playingImage)
                }
                MusicConstants.STATE_SERVICE.PREPARE, MusicConstants.STATE_SERVICE.PLAY -> {
                    PendinIntent.lPauseIntent.action = MusicConstants.ACTION.PAUSE_ACTION
                    val lPendingPauseIntent = PendingIntent.getService(context,0,
                        PendinIntent.lPauseIntent,PendingIntent.FLAG_IMMUTABLE)
                    try {
                        lPendingPauseIntent.send()
                    } catch (e: PendingIntent.CanceledException) {
                        e.printStackTrace()
                    }
                    Glide.with(requireContext())
                        .load(R.drawable.ic_play_arrow_white)
                        .into(imagePlay)
                    Glide.with(requireContext()).load(R.drawable.ic_favorite).into(playingImage)
                }
            }
        }

        binding.imageNext.setOnClickListener {
            if (!isRadioOn) {
                nextMusicPlay()
                screenSet()
            }
        }
        binding.imagePrev.setOnClickListener {
            if (!isRadioOn) {
                prevMusicPlay()
                screenSet()
            }
        }

        binding.exoPause.setOnClickListener {
            clickTime = System.currentTimeMillis()
            binding.exoPlay.visibility = VISIBLE
            binding.exoPause.visibility = GONE
            mPlayer?.pause()
        }
        binding.exoPlay.setOnClickListener {
            clickTime = System.currentTimeMillis()
            binding.exoPlay.visibility = GONE
            binding.exoPause.visibility = VISIBLE
            mPlayer?.start()
        }
        binding.exoNext.setOnClickListener {
            clickTime = System.currentTimeMillis()
            nextMusicPlay()
            screenSet()
        }
        binding.exoPrev.setOnClickListener {
            clickTime = System.currentTimeMillis()
            prevMusicPlay()
            screenSet()
        }
        binding.exoFfwd.setOnClickListener {
            clickTime = System.currentTimeMillis()
            if (currentPosition + seekForwardTime <= totalLen) { // realTotalLen) {
                mPlayer?.seekTo(currentPosition + seekForwardTime)
            } else {
                mPlayer?.seekTo(totalLen)
            }
        }
        binding.exoRew.setOnClickListener {
            clickTime = System.currentTimeMillis()
            if (currentPosition - seekForwardTime >= 0) {
                mPlayer?.seekTo(currentPosition - seekForwardTime)
            } else {
                mPlayer?.seekTo(0)
            }
        }

        binding.imageForward.setOnClickListener {
            if (!isRadioOn) {
                if (currentPosition + seekForwardTime <= totalLen) { // realTotalLen) {
                    mPlayer?.seekTo(currentPosition + seekForwardTime)
                } else {
                    mPlayer?.seekTo(totalLen)
                }
            }
        }
        binding.imageRewind.setOnClickListener {
            if (!isRadioOn) {
                if (currentPosition - seekForwardTime >= 0) {
                    mPlayer?.seekTo(currentPosition - seekForwardTime)
                } else {
                    mPlayer?.seekTo(0)
                }
            }
        }

        binding.imageViewBookmark.setOnClickListener {
            isScreen = 0
        }

        if (!isRadioOn) {
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    seekBarHint.visibility = View.VISIBLE
                }
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromTouch: Boolean
                ) {
                    seekBarHint.visibility = VISIBLE
                    seekBarHint.text = songTimer.milliSecondsToTimer(currentPosition.toLong())
                    val percent = progress / seekBar.max.toDouble()
                    val offset = seekBar.thumbOffset
                    val seekWidth = seekBar.width
                    val `val` = (percent * (seekWidth - 2 * offset)).roundToInt()
                    val labelWidth = seekBarHint.width
                    binding.frameTextviewPlaytime.x =
                        (offset + seekBar.x + `val` - (percent * offset).roundToInt()
                                - (percent * labelWidth / 2).roundToInt())
                }
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    if (mPlayer != null && mPlayer?.isPlaying == true) {
                        mPlayer!!.seekTo(seekBar.progress)
                    }
                }
            })
        }

        return root
    }

    private fun itemClick(music: Music) {
//        Log.d("itemClickkkkkk","${music.genre}, ${music.path}")
        if (music.path.contains(".mp4")) {
            centerImageView.visibility = INVISIBLE
        } else centerImageView.visibility = VISIBLE

//        videoPlay(music)
    }

    private fun screenSet() {
//        Log.d("screenSet", "$isPlaying, ${songID}, ${musicList[musicIDList.indexOf(songID)].currentPosition}")
        val i = musicIDList.indexOf(songID)
        if (i != -1) { //
            if (musicList[i].currentPosition == 1) { // 비디오
                centerImageView.visibility = INVISIBLE
                surfaceView?.visibility = VISIBLE
                (surfaceFrame.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,16:9"
                 // radio
            } else { // mp3
                centerImageView.visibility = VISIBLE
                surfaceView?.visibility = INVISIBLE
                (surfaceFrame.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,1:1"
            }
        }
//        movieAspectSetting()

        if (!isRadioOn && songID != -1 && index != -1) { // mp3
            binding.appTextView.text = "Music Playing..."
            index = musicIDList.indexOf(songID)
//            if (index != -1 && index < musicIDList.size) { // 목록에 있을 때 //song: ${musicList[indexOfSong]}")
            textTitle.text = musicList[index].title
            binding.textSinger.text = musicList[index].artist
            binding.tvCurrentDuration.text = "${index+1}/${musicIDList.size}"
            textLyric.text = lyric
            binding.textViewGenreInplay.text = musicList[index].album
            binding.tvTotalDuration.text = songTimer.milliSecondsToTimer(totalLen.toLong())

            if (musicList[index].bookmark == true)
                binding.imageViewBookmark.setBackgroundResource(R.drawable.ic_disk)
            else binding.imageViewBookmark.setBackgroundResource(R.drawable.bookmark)
//                Log.d("imageViewBookmark()","index: $index, ${musicList[index].bookmark},  $isRadioOn, ${isPlaying}")

            Glide.with(requireContext())
                .load(musicList[index].albumUri)
                .placeholder(R.drawable.ic_not)
                .error(R.drawable.ic_not)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(centerImageView)

            if (isPlaying) {
                Glide.with(requireContext())
                    .load(R.drawable.ic_pause)
                    .into(imagePlay)
//                    playingImage.visibility = VISIBLE
                Glide.with(this@DashboardFragment).load(R.raw.musicloader96).into(playingImage)
                ///////////exoControlView 관련 추가 //////////////////////
                binding.exoPlay.visibility = GONE
                binding.exoPause.visibility = VISIBLE
                ///////////exoControlView 관련 추가 끝//////////////////////
            } else {
                Glide.with(requireContext())
                    .load(R.drawable.ic_play)
                    .into(imagePlay)
//                    playingImage.visibility = VISIBLE
                Glide.with(this@DashboardFragment).load(R.drawable.ic_favorite).into(playingImage)
                binding.exoPlay.visibility = VISIBLE
                binding.exoPause.visibility = GONE
            }
        } else { // 라디오
//            isAlbum = false
//            Log.d("screenSet(else)","index: $isRadioOn, ")
            centerImageView.visibility = VISIBLE
            surfaceView?.visibility = INVISIBLE

            textTitle.text = titleDetail
            binding.textSinger.text = "방송"
            textLyric.text = "                     "
            binding.tvTotalDuration.text = songTimer.milliSecondsToTimer(9900000) //totalLen.toLong())
            binding.tvCurrentDuration.text = "0:00"
            binding.textViewGenreInplay.text = ""
            binding.imagePlaying.visibility = VISIBLE
            Glide.with(this@DashboardFragment).load(R.raw.colorloading96).into(playingImage)
//            textTitle.visibility = INVISIBLE
            binding.appTextView.text = "Radio Playing..."
            Glide.with(requireContext())
                .load(MusicDevice.imageRadioPlaySource)
                .error(R.drawable.ic_not)
                .into(centerImageView)

            if (isPlaying) {
                Glide.with(requireContext())
                    .load(R.drawable.ic_pause)
                    .into(imagePlay)
            } else {
                Glide.with(requireContext())
                    .load(R.drawable.ic_play)
                    .into(imagePlay)
                Glide.with(requireContext())
                    .load(R.drawable.ic_favorite)
                    .into(playingImage)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == ORIENTATION_LANDSCAPE) {
            orientationConfig = true
            fullScreenMode(requireActivity())
            hideAppBar()
//            movieAspectSetting()
        } else {
            orientationConfig = false
            normalScreen()
            showAppBar()
        }
    }

    private var appbarLayoutHeight = 0
    private var constraintLayoutPlayHeight = 0
    private fun hideAppBar() {
        with(binding) {
            appbarLayoutHeight = appBarLayout.measuredHeight
            appBarLayout.updateLayoutParams { height = 0 }
            constraintLayoutPlay.visibility = GONE
            var layoutParams : ConstraintLayout.LayoutParams = surfaceFrame.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.bottomToBottom = R.id.constlayoutHome
            surfaceFrame.layoutParams = layoutParams
        }
    }

    private fun showAppBar() {
        binding.appBarLayout.updateLayoutParams { height = appbarLayoutHeight }
        binding.constraintLayoutPlay.visibility = VISIBLE

//        binding.constraintLayoutPlay.updateLayoutParams { height = constraintLayoutPlayHeight }
    }
    private fun fullScreenMode(activity: Activity){
        //상단에 Action Bar 사라지게 하기
        activity.actionBar?.hide()
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //Android 11(R) 대응
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(false)
            val controller = activity.window.insetsController
            if (controller != null) {
                //상태바와 네비게이션 숨김
//                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.hide(WindowInsetsCompat.Type.systemBars())
                //화면 끝을 스와이프 하면 나타나게 설정
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            //R 버전 이하 대응
//            Log.d("fullScreenMode","ddddddd")
            activity.window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            )
        }
    }
    private fun normalScreen() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        activity?.actionBar?.show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity?.window?.setDecorFitsSystemWindows(true)
            val controller = activity?.window?.insetsController
            if (controller != null) {
                //상태바와 네비게이션 숨김
//                controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.show(WindowInsetsCompat.Type.systemBars())
                //화면 끝을 스와이프 하면 나타나게 설정
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }else {
                //R 버전 이하 대응
                activity?.window?.decorView?.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_VISIBLE)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        if (!isVideo)  activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        isThreadRun = true // 스레드 실행 조건
        threadRun()
        if (mPlayer?.isPlaying == true) {
        } else {
            binding.exoControlView.visibility = GONE
        }
    }
    override fun onPause() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        super.onPause()
//        Log.d("screenSet", "onPause()  called")
//        isScreen = true
        isThreadRun = false
    }
    override fun onDestroyView() {
        super.onDestroyView()
        mPlayer?.setDisplay(null)
        _binding = null
//        Log.d("screenSet", "onDestroyView()  called")
    }

//    var tempSongId = -1
    override fun surfaceCreated(holder: SurfaceHolder) {
//        Toast.makeText(context,"surfaceCreated",Toast.LENGTH_SHORT).show()
//        mPlayer?.setVideoScalingMode(VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
        if (!isRadioOn) mPlayer?.setDisplay(holder)
            /*try {
                mPlayer?.prepareAsync()
            } catch (e: java.lang.Exception) {
                Log.e("TAG", "ERROR", e)
            }*/
            /*mPlayer?.setOnPreparedListener {
                it.start()
            }*/
//        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//        Toast.makeText(context,"surfaceChanged",Toast.LENGTH_SHORT).show()
//        public void onVideoSizeChanged(int width, int height) {
//            Log.i("surfaceChanged", "onVideoSizeChanged: width = " + width + ", height = " + height)
        // // Get the dimensions of the video
        // // Get the dimensions of the video


    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
//        Toast.makeText(context,"surfaceDestroyed",Toast.LENGTH_SHORT).show()
        mPlayer?.setDisplay(null)
    }

   /* fun movieAspectSetting() {
        if (songID != -1 && musicList[musicIDList.indexOf(songID)].currentPosition == 1) {
            val videoWidth: Int? = mPlayer?.videoWidth
            val videoHeight: Int? = mPlayer?.videoHeight
            val videoProportion = videoWidth?.toFloat()!! / videoHeight?.toFloat()!!
//            Log.i("surfaceChanged", "videoWidth: width = " + videoWidth + ", height = " + videoHeight)

            // Get the width of the screen
            val screenWidth: Int = wm.defaultDisplay.width
            val screenHeight: Int = wm.defaultDisplay.height
            val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()
//            Log.i("surfaceChanged", "screenWidth: width = " + screenWidth + ", height = " + screenHeight)

            // Get the SurfaceView layout parameters
            val lp = surfaceView?.layoutParams
            if (videoProportion > screenProportion) {
                lp?.width = screenWidth
                lp?.height = (screenWidth.toFloat() / videoProportion).toInt()
            } else {
                lp?.width = (videoProportion * screenHeight.toFloat()).toInt()
                lp?.height = screenHeight
            }
            // Commit the layout parameters
            surfaceView!!.layoutParams = lp
        }
    }*/
}