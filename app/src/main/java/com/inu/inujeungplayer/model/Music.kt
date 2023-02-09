package com.inu.inujeungplayer.model

import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music_table")
data class Music(
    @PrimaryKey //(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int,
    @ColumnInfo(name = "title")
    var title:String?,
    @ColumnInfo(name = "artist")
    var artist:String?,
    @ColumnInfo
    val album:String?,
    @ColumnInfo
    val albumId:Long?,
    @ColumnInfo
    val duration: Long,
    @ColumnInfo
    val albumUri: String?,
    @ColumnInfo
    val path : String,
    @ColumnInfo
    val genre: String?,
    @ColumnInfo
    var isSelected: Boolean?, // playing song
    @ColumnInfo
    var bookmark: Boolean?,
    @ColumnInfo
    val contentUri: String,
    @ColumnInfo
    var currentPosition: Int
){

    //호출 부분
//
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Music>() {
            override fun areItemsTheSame(oldItem: Music, newItem: Music) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Music, newItem: Music) =
                oldItem == newItem
        }
    }
}
