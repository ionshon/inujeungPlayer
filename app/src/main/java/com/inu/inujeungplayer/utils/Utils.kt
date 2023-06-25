package com.inu.inujeungplayer.utils

import android.util.Log
import com.inu.inujeungplayer.constant.MusicDevice

data class ComparablePair<A: Comparable<A>, B: Comparable<B>>(
    val first: A,
    val second: B
) : Comparable<ComparablePair<A, B>> {
    override fun compareTo(other: ComparablePair<A, B>): Int {
        val firstComp = this.first.compareTo(other.first)
        if (firstComp != 0) { return firstComp }
        return this.second.compareTo(other.second)
    }
}

fun bounceIconSetting() {
    val filterdIsSelected = MusicDevice.musicList.groupBy { it.isSelected }
    MusicDevice.oldMusic = filterdIsSelected[true]?.get(0)
    MyApplication.musicRepository._songid.value = MusicDevice.songID
//    Log.d("bounceIconSetting(utils)","")
}

fun goldLingSetting() {
    val filterdIsSelected = MusicDevice.radioList.groupBy { it.isSelected }
    MusicDevice.oldRadio = filterdIsSelected[true]?.get(0)
    MyApplication.radioRepo._radioid.value = MusicDevice.radioID
}
