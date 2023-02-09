package com.inu.inujeungplayer.model

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "radio_table")
data class Radio(
    @PrimaryKey //(autoGenerate = true)
    val id: Int,
    val title: String,
    var addr: String,
    val icon: Int,
    var isOn: Boolean,
    var isSelected: Boolean
){

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Radio>() {
            override fun areItemsTheSame(oldItem: Radio, newItem: Radio) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Radio, newItem: Radio) =
                oldItem == newItem
        }
    }
}