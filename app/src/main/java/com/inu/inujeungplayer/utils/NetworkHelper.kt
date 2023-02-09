package com.inu.inujeungplayer.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkHelper {
    @JvmStatic
    fun isInternetAvailable(pContext: Context?): Boolean {

        val connectivityManager = pContext?.getSystemService(
            Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            ?: return false

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        // If we check only for "NET_CAPABILITY_INTERNET", we get "true" if we are connected to a wifi
        // which has no access to the internet. "NET_CAPABILITY_VALIDATED" also verifies that we
        // are online
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        /*if (pContext == null) {
            return false
        }
        val cm = pContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting*/
    }
}