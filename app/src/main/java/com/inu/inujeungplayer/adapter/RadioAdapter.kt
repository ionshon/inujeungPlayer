package com.inu.inujeungplayer.adapter

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.inu.inujeungplayer.R
import com.inu.inujeungplayer.adapter.MusicAdapter.Companion.index
import com.inu.inujeungplayer.constant.MusicConstants
import com.inu.inujeungplayer.constant.MusicDevice
import com.inu.inujeungplayer.constant.MusicDevice.dataSource
import com.inu.inujeungplayer.constant.MusicDevice.imageRadioPlaySource
import com.inu.inujeungplayer.constant.MusicDevice.indexOfSong
import com.inu.inujeungplayer.constant.MusicDevice.isAllSearch
import com.inu.inujeungplayer.constant.MusicDevice.isPlaying
import com.inu.inujeungplayer.constant.MusicDevice.isRadioOn
import com.inu.inujeungplayer.constant.MusicDevice.isScreen
import com.inu.inujeungplayer.constant.MusicDevice.oldRadio
import com.inu.inujeungplayer.constant.MusicDevice.radioList
import com.inu.inujeungplayer.constant.MusicDevice.titleDetail
import com.inu.inujeungplayer.constant.MusicDevice.titleMain
import com.inu.inujeungplayer.constant.PendinIntent
import com.inu.inujeungplayer.constant.PendinIntent.intent
import com.inu.inujeungplayer.model.Radio
import com.inu.inujeungplayer.service.ForegroundService
import com.inu.inujeungplayer.utils.MyApplication
import com.inu.inujeungplayer.utils.NetworkHelper
import com.inu.inujeungplayer.utils.NetworkHelper.isInternetAvailable
import com.inu.inujeungplayer.utils.bounceIconSetting
import gen._base._base_java__assetres.srcjar.R.id.time

class RadioAdapter(val itemClick: (Int) -> Unit) : ListAdapter<Radio, RadioAdapter.RadioViewHolder>(Radio.DiffCallback) {
    var list = arrayListOf<Radio>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioAdapter.RadioViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.radio_item, parent, false)
        return RadioViewHolder(view)
    }

    override fun onBindViewHolder(holder: RadioAdapter.RadioViewHolder, position: Int) {
        val radio = getItem(position)
        holder.setRadio(radio)

        if (radio.isSelected) {
            holder.radioLayout.setBackgroundResource(R.drawable.edge_gold)
        } else holder.radioLayout.setBackgroundResource(R.drawable.edge)


        //**************************************************
        holder.itemView.setOnClickListener { v -> // ????????? ??????

//            Log.v("queryRadios", "FoundloadRadios2: ${radio.title}, ${radio.icon} Radios")
            MusicDevice.radioID = radio.id

            if (!isRadioOn && index != -1 && ForegroundService.state != MusicConstants.STATE_SERVICE.PAUSE) bounceIconSetting()
            isRadioOn = true
            isAllSearch = true
            isScreen = 0 // play ?????? ?????? ?????????
            dataSource = radio.addr
            // ?????? ???????????? ?????????

            radio.isOn = true

//            songID = music.id
//        Log.d("itemClick","filterdIsSelected= ${music.isSelected}, ${musicList.size}" )
            // ?????? oldRadio, value ???????????? ??????
            val filterdIsSelected = radioList.groupBy { it.isSelected }
            oldRadio = filterdIsSelected[true]?.get(0)
            itemClick(radio.id)

            if (radio.isSelected) {
                holder.radioLayout.setBackgroundResource(R.drawable.edge_gold)
            } else holder.radioLayout.setBackgroundResource(R.drawable.edge)


            if (!isInternetAvailable(v.context)) {
                showError(v)
                return@setOnClickListener
            }
            Log.d("workerPlay"," ??????0 currentTimeMillis=> ${radio.addr}")

//            Log.d("setOnClickListener","${MusicConstants.RADIO_ADDR.radioAddrList[position]}")
            if (radio.addr.contains(".pls")) {
                Toast.makeText(MyApplication.applicationContext(), "radio addr error!", Toast.LENGTH_SHORT).show()
//                SetStreamUrl().setStreamUrl(MusicConstants.RADIO_ADDR.radioAddrList[position])
            } else {
                when (ForegroundService.state) {
                    MusicConstants.STATE_SERVICE.NOT_INIT -> {
                        isPlaying = true
                        if (!NetworkHelper.isInternetAvailable(v.context)) {
                            Snackbar.make(v, "No internet", Snackbar.LENGTH_LONG).show()
                            return@setOnClickListener
                        }
                        intent = Intent(v.context, ForegroundService::class.java)
                        intent?.action = MusicConstants.ACTION.START_ACTION
//                        dataSource = data.addr
                        titleMain = "Radio"
                        titleDetail = radio.title
                        imageRadioPlaySource = Uri.parse("android.resource://" + R::class.java.getPackage().name + "/" + radio.icon)
                        ContextCompat.startForegroundService(v.context, intent!!)

                    }

                    MusicConstants.STATE_SERVICE.PREPARE, MusicConstants.STATE_SERVICE.PLAY -> {
                        time = System.currentTimeMillis().toInt()
                        if (titleDetail!= radio.title) {
                            Log.d("workerPlay"," ??????1 currentTimeMillis=> ${System.currentTimeMillis()}")
                            isPlaying = true
                            PendinIntent.lPauseIntent.action = MusicConstants.ACTION.PLAY_ACTION
//                            dataSource = data.addr
                            titleMain = "Radio"
                            titleDetail = radio.title
                            imageRadioPlaySource = Uri.parse("android.resource://" + R::class.java.getPackage().name + "/" + radio.icon)
                            val lPendingPauseIntent = PendingIntent.getService(
                                v.context,
                                0,
                                PendinIntent.lPauseIntent,
                                PendingIntent.FLAG_IMMUTABLE
                            )
                            try {
                                lPendingPauseIntent.send()
                            } catch (e: PendingIntent.CanceledException) {
                                e.printStackTrace()
                            }
                        } else {
                            isPlaying = false
                            Log.d("workerPlay"," ?????? currentTimeMillis=> ${System.currentTimeMillis()}")
                            PendinIntent.lPauseIntent.action = MusicConstants.ACTION.PAUSE_ACTION
                            val lPendingPauseIntent = PendingIntent.getService(
                                v.context, 0,
                                PendinIntent.lPauseIntent, PendingIntent.FLAG_IMMUTABLE
                            )
                            try {
                                lPendingPauseIntent.send()
                            } catch (e: PendingIntent.CanceledException) {
                                e.printStackTrace()
                            }
                        }
                    }

                    MusicConstants.STATE_SERVICE.PAUSE -> {
                        isPlaying = true
                        if (!NetworkHelper.isInternetAvailable(v.context)) {
                            showError(v)
                            return@setOnClickListener
                        }

                        if (titleMain != radio.title) { //  ???????????????
//                    Log.d("radoiTest","????????????, ?????? ?????? ?????????, ${titleDetail}, ${data.detail}")
                            PendinIntent.lPlayIntent.action = MusicConstants.ACTION.PLAY_ACTION
//                            dataSource = data.addr
                            titleMain = "Radio"
                            titleDetail = radio.title
                            imageRadioPlaySource = Uri.parse("android.resource://" + R::class.java.getPackage().name + "/" + radio.icon)
                            val lPendingPlayIntent = PendingIntent.getService(
                                v.context,
                                0,
                                PendinIntent.lPlayIntent,
                                PendingIntent.FLAG_IMMUTABLE
                            )
                            try {
                                lPendingPlayIntent.send()
                            } catch (e: PendingIntent.CanceledException) {
                                e.printStackTrace()
                            }
                        } else { // ?????? ??????
//                    Log.d("radoiTest","????????????, ????????? ??????")
                            val timeDiff = System.currentTimeMillis() - time
                            if (timeDiff > 10000) {
//                                Log.d("?????????","??????????????????, timeDiff = > $timeDiff" )
                                PendinIntent.lPlayIntent.action = MusicConstants.ACTION.PLAY_ACTION
                                val lPendingPlayIntent = PendingIntent.getService(
                                    v.context,
                                    0,
                                    PendinIntent.lPlayIntent,
                                    PendingIntent.FLAG_IMMUTABLE
                                )
                                try {
                                    lPendingPlayIntent.send()
                                } catch (e: PendingIntent.CanceledException) {
                                    e.printStackTrace()
                                }
                            } else {
//                                Log.d("?????????","?????????????????????, timeDiff = > $timeDiff\" ")
                                PendinIntent.lReplayIntent.action =
                                    MusicConstants.ACTION.REPLAY_ACTION
                                val lPendingPlayIntent = PendingIntent.getService(
                                    v.context,
                                    0,
                                    PendinIntent.lReplayIntent,
                                    PendingIntent.FLAG_IMMUTABLE
                                )
                                try {
                                    lPendingPlayIntent.send()
                                } catch (e: PendingIntent.CanceledException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                } // when
            }
        } //holder.itemView.setOnClickListener
    }
    inner class RadioViewHolder( view: View):  RecyclerView.ViewHolder(view) {
        var radioImageView = view.rootView.findViewById<ImageView>(R.id.image_radio_item)
//        val loadingBubble = view.rootView.findViewById<ImageView>(R.id.imageView_loading)
        val radioLayout = view.findViewById<FrameLayout>(R.id.radioLayout)

        fun setRadio(radio: Radio) {
//            GlideApp.with(loadingBubble).load(R.raw.loading_bubble).into(loadingBubble)
//            loadingBubble.visibility = INVISIBLE


            Glide.with(radioImageView)
                .load(radio.icon )//Uri.parse("resource://" + R::class.java.getPackage().name + "/" + radio.icon)  )
//                .load(radio.icon)
                .error(R.drawable.ic_not)
                .into(radioImageView)


//            if (radio.isOn) {
//                radioLayout.setBackgroundResource(R.drawable.edge_gold)
//            } else { radioLayout.setBackgroundResource(R.drawable.edge) }
        }
    }

    fun notifyChanged(i: Int) {
        notifyItemChanged(i)
//        Log.d("itemClicksda(notifyChanged)","${i}")
    }
    private fun showError(v: View) {
//        Snackbar.make(v, "No internet", Snackbar.LENGTH_LONG).show()
        val snackBar = Snackbar.make(v, "?????????????????? ????????? ????????????.", Snackbar.LENGTH_SHORT)
        val snackBarView = snackBar.view
        val snackBarLayout = snackBarView.layoutParams as FrameLayout.LayoutParams
        snackBarLayout.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER // ???????????? ?????? ??????
//                snackBarLayout.width = 800 // ?????? ??????
//                snackBarLayout.height = 500 // ?????? ??????
        snackBar.show()
    }
}