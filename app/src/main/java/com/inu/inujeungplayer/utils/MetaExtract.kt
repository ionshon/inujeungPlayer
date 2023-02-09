package com.inu.inujeungplayer.utils

import com.mpatric.mp3agic.Mp3File

object MetaExtract {
    fun getLyric(path: String): String {

        val lyric: String? = try {
            val mp3file = Mp3File(path)
            val id3v2Tag = mp3file.id3v2Tag
            id3v2Tag.lyrics
        } catch (e: java.lang.Exception) {
            "\n\n     error!!     "
        } finally {
//           println("id3v2Tag.lyrics error!!")
        }
        return lyric?: "\n\n    No lyric!    "
    }
}