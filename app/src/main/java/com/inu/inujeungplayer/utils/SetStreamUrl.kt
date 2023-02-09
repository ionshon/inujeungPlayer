package com.inu.inujeungplayer.utils

import android.util.Log
import com.inu.inujeungplayer.constant.MusicConstants.allRadio
import com.inu.inujeungplayer.constant.MusicConstants.allRadio2
import com.inu.inujeungplayer.constant.MusicConstants.allRadioIcon
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread

class SetStreamUrl {
    private val LOGTAG = "GetStreamingUrl"
    //        val viewModel: RadioViewModel by viewModels { RadioViewModelFactory(MyApplication.radioRepo!!) }
    fun setStreamUrl(i: Int, url: String?) { //: LinkedList<String?>? {
        var br: BufferedReader
        var murl: String = ""
        var mTitle: String = ""
//        var murls: LinkedList<String?>? = null
//        Log.i(LOGTAG, "cachedir, url=> ${url}")

        thread {
            try {
                val mUrl = URL(url).openConnection()
//                Log.d("openConnection","$murl")

                br = BufferedReader(
                    InputStreamReader(mUrl.getInputStream())
                )

                mUrl.connectTimeout = 5000
//                Log.i(LOGTAG, "cachedir, murl => ${murl}")
                while (true) {
                    try {
                        val line = br.readLine() ?: break
                        murl = parseLine(line)
//                        mTitle = parseLineTitle(line)
//                        Log.i(LOGTAG, "parseLine, br => ${br}")
//                        Log.i(LOGTAG, "parseLine2, line => ${line}")
//                        Log.i(LOGTAG, "parseLine3, murl => ${murl}")
                        if (murl != "") {
                            Log.d(LOGTAG, "라인1: $murl")
//                            allRadio[i][1] = murl
                            allRadio2[i].addr = murl
                            val radio = MyApplication.radioRepo.getRadio(i)
                            radio.addr = murl

                            MyApplication.radioRepo.radioChanged(radio)
                        }
                    } catch (e: java.lang.Exception) {
//                        Toast.makeText(MyApplication.applicationContext(), "No Internet!!", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }finally{}
                } // while
                br.close()
            } catch (e: Exception) {
//                Toast.makeText(MyApplication.applicationContext(), "No Internet!!", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally{}
        } // thread
    } // fun

    private fun parseLine(line: String): String {
        val trimmed = line.trim { it <= ' ' }
        return if (trimmed.indexOf("http") >= 0) {
            trimmed.substring(trimmed.indexOf("http"))
        } else ""
    }
    /*private fun parseLineTitle(line: String): String {
        val trimmed = line.trim { it <= ' ' }
        return if (trimmed.indexOf("ti") >= 0) {
            trimmed.substring(trimmed.indexOf("http"))
        } else ""
    }
    private fun parseRaw(raw: String): HashMap<String, String> {
        val results = HashMap<String, String>()
        for (line in raw.split("\r\n").toTypedArray()) {
            val colon = line.indexOf(":")
            if (colon != -1) {
                val key = line.substring(0, colon).trim { it <= ' ' }.lowercase(Locale.getDefault())
                val value = line.substring(colon + 1).trim { it <= ' ' }
                results["upnp_$key"] = value
            }
        }
        return results
    }*/
}