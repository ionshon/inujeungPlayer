package com.inu.inujeungplayer.ui.dashboard

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import com.inu.inujeungplayer.R

/*
fun getControls() {
    // Get buttons
    var btnPlayTV = findViewById<View>(R.id.btnPlayTV) as Button
    val btnPlayVV = findViewById<View>(R.id.btnPlayVV) as Button
    val btnStopTV = findViewById<View>(R.id.btnStopTV) as Button
    val btnStopVV = findViewById<View>(R.id.btnStopVV) as Button

    btnPlayTV.setOnClickListener { playTextureView() }
    btnPlayVV.setOnClickListener { playVideoView() }
    btnStopTV.setOnClickListener { stopTextureView() }
    btnStopVV.setOnClickListener { stopVideoView() }

    // Get views
    mTextureView = findViewById<View>(R.id.screenTextureView) as TextureView
    mVideoView = findViewById<View>(R.id.screenVideoView) as VideoView
}
*/

class MyTexureViewListener(context: Context, textureView: TextureView, uri: Uri) : TextureView.SurfaceTextureListener,
        MediaPlayer.OnPreparedListener{
    var mTextureView: TextureView
    var mContext: Context
    var vPlayer = MediaPlayer()
    var mVideoSource :Uri
//    var mVideoSource: String

    init {
        // 생성자는 편의상 컨텍스트와 비디오 파일의 이름을 받아서 저장해 둔다.
        mTextureView = textureView
        mContext = context
        mVideoSource = uri
    }


    // 텍스쳐가 준비되면 불리는 메서드이다. 여기에서 초기화를 처리한다.
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        try {
            // Create MediaPlayer
//                val vPlayer = MediaPlayer()

            // Set the surface
            val surface = Surface(surface)
            vPlayer.setSurface(surface)

            // Set the video source
//            val resID = "${R.raw.love_feeling}"
//            val uri: Uri = Uri.parse("android.resource://" + R::class.java.getPackage().name + "/" + resID)
//            vPlayer.reset()
            vPlayer.setDataSource(mContext, mVideoSource)

            // Prepare: In case of local file prepare() can be used, but for streaming, prepareAsync() is a must
            vPlayer.prepareAsync()

            // Wait for the preparation
            vPlayer.setOnPreparedListener(OnPreparedListener { // Play the video
//                    playTextureView()
                vPlayer.start()
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    fun playTextureView() {
        // Play it
        vPlayer.start()
    }
    fun prepareTextureViewVideo() {
        // Set the listener to play "sample.mp4" in raw directory
        mTextureView.surfaceTextureListener = MyTexureViewListener(mContext, mTextureView, mVideoSource)
    }

    fun stopTextureView() {
        // Pause it. If stopped, vPlayer should be prepared again.
        vPlayer.pause()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        vPlayer.start()
    }
}