package com.inu.inujeungplayer.adapter

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.inu.inujeungplayer.R
import com.inu.inujeungplayer.constant.MusicConstants
import com.inu.inujeungplayer.constant.MusicDevice
import com.inu.inujeungplayer.constant.MusicDevice.isRadioOn
import com.inu.inujeungplayer.constant.MusicDevice.oldMusic
import com.inu.inujeungplayer.constant.MusicDevice.titleDetail
import com.inu.inujeungplayer.constant.MusicDevice.titleMain
import com.inu.inujeungplayer.constant.PendinIntent.intent
import com.inu.inujeungplayer.constant.PendinIntent.lPauseIntent
import com.inu.inujeungplayer.constant.PendinIntent.lPlayIntent
import com.inu.inujeungplayer.model.Music
import com.inu.inujeungplayer.service.ForegroundService
import com.inu.inujeungplayer.utils.MyApplication
import com.inu.inujeungplayer.utils.goldLingSetting
import java.io.File
import java.text.SimpleDateFormat


class MusicAdapter(val menuClick: (Music, View) -> Unit,
                   val itemClick: (Music) -> Unit,
                   val artistLongClick: (Music) -> Unit) :
    ListAdapter<Music, MusicAdapter.MusicViewHolder>(Music.DiffCallback){

    companion object {
        val resId = R.drawable.outline_music_note_24
//        var oldPosition = 1
//        var currentSongID = 0
        var index: Int = -1 // 초기 라디오실행 및 검색목록에 없을 때 -1
//        lateinit var songAtAdaptr: Music // music어댑터에서 선택한곡
//        var searching = false
//        var lyric= ""
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_layout, parent, false)
        return MusicViewHolder(view)
    }

//    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = getItem(position)
//        holder.rootView.tag = Music

        holder.setMusic(music)

        // 동영상은 currentPostion=1 로 지정
        if (music.currentPosition == 1 ) {
            Glide.with(MyApplication.applicationContext())
                .load(Uri.fromFile(File(music.path)))
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .priority(Priority.HIGH)
                .skipMemoryCache(false)
//                    .placeholder(resId)
                .error(resId)
                .centerInside()
                .into(holder.imageAlbum)
        } else {
            Glide.with(holder.imageAlbum)
                .load(music.albumUri)
//            .thumbnail(0.33f)
                .placeholder(resId)
                .error(resId)
                .centerCrop()
                .into(holder.imageAlbum)
        }

        Glide.with(MyApplication.applicationContext())
            .load(R.raw.bounc)
            .into(holder.imageIsPlay)

        if (music.isSelected == true) {
            holder.imageIsPlay.visibility = View.VISIBLE
        } else holder.imageIsPlay.visibility = View.INVISIBLE

        holder.imageMenu.setOnClickListener {
            music.currentPosition = position
            menuClick(music, holder.imageMenu)
        }

        holder.textAtist.setOnLongClickListener {
            artistLongClick(music)
            return@setOnLongClickListener false
        }

        // mp3 최초 실행 시점 *******************************************
        holder.rootView.setOnClickListener { v ->
            if (isRadioOn && ForegroundService.state != MusicConstants.STATE_SERVICE.PAUSE) goldLingSetting()
            isRadioOn = false
            MusicDevice.songID = music.id
//        Log.d("itemClick","filterdIsSelected= ${music.isSelected}, ${musicList.size}" )
            val filterdIsSelected = MusicDevice.musicList.groupBy { it.isSelected }
            oldMusic = filterdIsSelected[true]?.get(0)

            if (music.isSelected == true) {
                holder.imageIsPlay.visibility = View.VISIBLE
            } else holder.imageIsPlay.visibility = View.INVISIBLE
//            isScreen = 0 // play 화면 변경 필요시
            index = position

            itemClick(music) // FragmentList에서 isSelected  트리거 //:${music.artist}, ")
            when(ForegroundService.state) {
                MusicConstants.STATE_SERVICE.NOT_INIT -> {
//                    MusicDevice.isPlaying = true
                    intent = Intent(v.context, ForegroundService::class.java)
                    intent?.action = MusicConstants.ACTION.START_ACTION
                    titleMain = "Music"
                    titleDetail = music.title
                    ContextCompat.startForegroundService(v.context, intent!!)
                }

                MusicConstants.STATE_SERVICE.PREPARE, MusicConstants.STATE_SERVICE.PLAY -> {
                    titleMain = "Music"
                    titleDetail = music.title
//                    Log.d("kkkkkinservice","song=${song?.title}, new= ${musicList[index].title}")
                    if (oldMusic?.id != music.id || isRadioOn) { // 다른 곡 선택 플레이
//                        MusicDevice.isPlaying = true

                        lPlayIntent.action = MusicConstants.ACTION.PLAY_ACTION
                        val lPendingPlayIntent = PendingIntent.getService(v.context, 0, lPlayIntent, PendingIntent.FLAG_IMMUTABLE)
                        try {
                            lPendingPlayIntent.send()
                        } catch (e: PendingIntent.CanceledException) {
                            e.printStackTrace()
                        }

                    } else { // 같은 곡 멈춤
                        lPauseIntent.action = MusicConstants.ACTION.PAUSE_ACTION
//                        MusicDevice.isPlaying = false
                        val lPendingPauseIntent = PendingIntent.getService(v.context,0, lPauseIntent,PendingIntent.FLAG_IMMUTABLE)
                        try {
                            lPendingPauseIntent.send()
                        } catch (e: PendingIntent.CanceledException) {
                            e.printStackTrace()
                        }
                    }
                }

                MusicConstants.STATE_SERVICE.PAUSE -> {
//                    musicList[index].isSelected = true
                    titleMain = "Music"
//                    titleDetail = music.title
//                    isRadioOn = false

//                    Log.d("screenSet(adapterPause)","index: $index, $isRadioOn, ${MusicDevice.isPlaying}")
//                    MusicDevice.isPlaying = true
                    if (oldMusic?.id == music.id) {
                        lPlayIntent.action = MusicConstants.ACTION.REPLAY_ACTION
                        val lPendingPlayIntent = PendingIntent.getService(v.context,0,lPlayIntent,PendingIntent.FLAG_IMMUTABLE)
                        try {
                            lPendingPlayIntent.send()
                        } catch (e: PendingIntent.CanceledException) {
                            e.printStackTrace()
                        }
                    } else {
                        lPlayIntent.action = MusicConstants.ACTION.PLAY_ACTION
                        val lPendingPlayIntent = PendingIntent.getService(
                            v.context,
                            0,
                            lPlayIntent,
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
        }
    }

    inner class MusicViewHolder( view: View ):  RecyclerView.ViewHolder(view) {
        val rootView = view
//        val imageView: ImageView = view.findViewById(R.id.image)
        val textTitle: TextView = view.findViewById(R.id.textTitle)
        val textAtist: TextView = view.findViewById(R.id.textAtist)
        val textDuration: TextView = view.findViewById(R.id.textDuration)
        val textViewGenre: TextView = view.findViewById(R.id.textView_genre)
        val imageAlbum: ImageView = view.findViewById(R.id.imageAlbum)
        val imageIsPlay: ImageView = view.findViewById(R.id.imageView_isplay)
        val imageMenu: ImageView = view.findViewById(R.id.imageView_menu)

        fun setMusic(music: Music) {
            textTitle.text = music.title
            textAtist.text = music.artist
            textViewGenre.text = music.genre
            val sdf = SimpleDateFormat("mm:ss")
            textDuration.text = sdf.format(music.duration)

//                1. 로드할 대상 Uri    2. 입력될 이미지뷰
//                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                .placeholder(resId)
//                .error(resId)
//                .thumbnail(GlideApp.with(binding.root.context).load(resId).override(100, 100))

        }
    }

    // 유저 리스트 갱신
    /*fun setData(user : List<Music>){
        userList = user
        notifyDataSetChanged()
    }*/
    fun notifyChanged(i: Int) {
//        notifyItemChanged(indexOfSong)
        notifyItemChanged(i)

//        Log.d("notifyChanged","${song?.title}, ${musicList[index].title}")
//        Log.d("notifyChanged","${song?.isSelected}, ${musicList[index].isSelected}")
    }

/*
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        menu?.add(Menu.NONE, R.id.imageAlbum,
            Menu.NONE, R.string.info);
        menu?.add(Menu.NONE, R.id.imageView_isplay,
            Menu.NONE, R.string.delete);
    }*/

}


/*
class MusicAdapter : RecyclerView.Adapter<MusicAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Holder(view)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val music = musicList[position]
        holder.setMusic(music)
    }



    inner class Holder(val binding: ItemLayoutBinding): RecyclerView.ViewHolder(binding.root){

        val resId = R.drawable.outline_music_note_24
        fun setMusic(music: Music) {
            with(binding) {
                textTitle.text = music.title
                textAtist.text = music.artist
                textViewGenre.text = music.genre
                val sdf = SimpleDateFormat("mm:ss")
                textDuration.text = sdf.format(music.duration)
            }

//                1. 로드할 대상 Uri    2. 입력될 이미지뷰
            Glide.with(binding.root.context)
                .load(music.albumUri)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(resId)
                .error(resId)
//                .thumbnail(GlideApp.with(binding.root.context).load(resId).override(100, 100))
                .into(binding.imageAlbum)


            */
/* GlideApp.with(binding.root.context)
                 .load(R.raw.bounc)
                 .into(binding.imageViewIsplay)

             binding.imageViewIsplay.visibility = View.INVISIBLE
             if (music.isSelected) {
                 binding.imageViewIsplay.visibility = View.VISIBLE
             }*//*

        }
    }
}*/
