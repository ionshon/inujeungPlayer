package com.inu.inujeungplayer

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationBarView
import com.inu.inujeungplayer.adapter.ViewPagerAdapter
import com.inu.inujeungplayer.constant.MusicConstants.allRadio
import com.inu.inujeungplayer.constant.MusicConstants.allRadio2
import com.inu.inujeungplayer.databinding.ActivityMainBinding
import com.inu.inujeungplayer.utils.SetStreamUrl

class MainActivity : AppCompatActivity(),  NavigationBarView.OnItemSelectedListener{

    private lateinit var binding: ActivityMainBinding
//    private lateinit var wm : WindowMa
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView
// 페이저에 어댑터 연결
        binding.viewPager.adapter = ViewPagerAdapter(this)

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    navView.menu.getItem(position).isChecked = true
                }
            }
        )

        // 리스너 연결
        navView.setOnItemSelectedListener(this)
        getRadioAddress()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if( newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideNavView()
        }
        else {
            showNavView()
        }
    }
    private fun hideNavView() {
//        constraintLayoutPlayHeight = binding.constraintLayoutPlay.measuredHeight
//        binding.constraintLayoutPlay.updateLayoutParams { height = 0 }
        binding.navView.visibility = View.GONE
    }
    private fun showNavView() {
        binding.navView.visibility = View.VISIBLE
    }

    private fun getRadioAddress() {
//        fSetPlayableUrl(MusicConstants.RADIO_ADDR.kbs1Radio)
        for (i in allRadio2.indices) {
            val m =(allRadio2[i].addr)
            if (!m.contains("m3u8")) {
                SetStreamUrl().setStreamUrl(i, m)
                Log.d("fSetPlayableUrl","${m}")
            }
        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.navigation_home -> {
                binding.viewPager.currentItem = 0
//                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment_activity_main , HomeFragment()).commitAllowingStateLoss()
                return true
            }
            R.id.navigation_dashboard -> {
                binding.viewPager.currentItem = 1
//                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment_activity_main, DashboardFragment()).commitAllowingStateLoss()
                return true
            }
            R.id.navigation_radio -> {
                binding.viewPager.currentItem = 2
//                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment_activity_main, RadioFragment()).commitAllowingStateLoss()
                return true
            }
        }
        return false
    }
}