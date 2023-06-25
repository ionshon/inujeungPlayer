package com.inu.inujeungplayer.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.widget.Toast
import com.inu.inujeungplayer.R
import com.inu.inujeungplayer.repository.HomeRepository
import com.inu.inujeungplayer.repository.RadioRepo
import com.inu.inujeungplayer.repository.db.MusicRoomDatabase
import org.acra.BuildConfig
import org.acra.config.mailSender
import org.acra.config.toast
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

class MyApplication : Application(){

    val database by lazy { MusicRoomDatabase.getInstance(this) }
    init{
        instance = this

    }

    companion object {
        lateinit var instance: MyApplication // 에러나면 manifest에 등록여부 확인
        fun applicationContext() : Context {
            return instance.applicationContext
        }

        val musicRepository by lazy { HomeRepository(MusicRoomDatabase.getInstance(applicationContext()).musicDao()) }
        val radioRepo by lazy { RadioRepo(MusicRoomDatabase.getInstance(applicationContext()).radioDao()) }
//        val playRepo by lazy { MusicRoomDatabase.getInstance(applicationContext()) ?.let { PlayRepo(it.playDao()) } }
    }

    @SuppressLint("Range")
    override fun attachBaseContext(base:Context) {
        super.attachBaseContext(base)

        initAcra {
            //core configuration:
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            //each plugin you chose above can be configured in a block like this:
            toast {
                text = getString(R.string.acra_toast_text)
                length = Toast.LENGTH_SHORT
                //opening this block automatically enables the plugin.
            }
/*

            httpSender {
                //required. Https recommended
                uri = "https://your.server.com/report"
                //optional. Enables http basic auth
                basicAuthLogin = "acra"
                //required if above set
                basicAuthPassword = "password"
                // defaults to POST
                httpMethod = HttpSender.Method.POST
                //defaults to 5000ms
                connectionTimeout = 5000
                //defaults to 20000ms
                socketTimeout = 20000
                // defaults to false
                dropReportsOnTimeout = false
                //the following options allow you to configure a self signed certificate
//                keyStoreFactoryClass = MyKeyStoreFactory::class.java
//                certificatePath = "asset://mycert.cer"
//                resCertificate = R.raw.mycert
                certificateType = "X.509"
                //defaults to false. Recommended if your backend supports it
                compress = false
                //defaults to all
                tlsProtocols = arrayOf(TLS.V1_3, TLS.V1_2, TLS.V1_1, TLS.V1)
            }
*/

            mailSender {
                //required
                mailTo = "isson33@naver.com"
                //defaults to true
                reportAsFile = true
                //defaults to ACRA-report.stacktrace
                reportFileName = "Crash.txt"
                //defaults to "<applicationId> Crash Report"
                subject = getString(R.string.mail_subject)
                //defaults to empty
                body = getString(R.string.mail_body)
            }
        }
    }

}