package com.camera.finder.util

import android.util.Log
import com.camera.finder.data.WifiData
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class ScanningUtil(val mRateControl:RateControl) {
    private val DPORTS = intArrayOf(80)

    fun startScanning(address: String): WifiData? {
       try{
           val host = WifiData()
           val h = InetAddress.getByName(address)
           host.ipAddress = address
           host.hardwareAddress = HardwareAddress.getHardwareAddress(address)
           host.name=h.hostName
           if (!NetInfo.NOMAC.equals(host.hardwareAddress)) {
               return host
           }

           // Native InetAddress check

           if (h.isReachable(getRate())) {
               Log.e("TAG", "found using InetAddress ping " + address + "+" + getRate()+"+"+h.hostName+"+"+h.canonicalHostName)
               if (mRateControl.indicator == null) {
                   mRateControl.indicator = address
                   mRateControl.adaptRate()
               }
               return host
           }

           host.hardwareAddress = HardwareAddress.getHardwareAddress(address)
           if (!NetInfo.NOMAC.equals(host.hardwareAddress)) {
               return host
           }

           val s = Socket()
           for (value in DPORTS) {
               try {
                   s.bind(null)
                   s.connect(
                       InetSocketAddress(
                           address,
                           value
                       ), getRate()
                   )
               } catch (e: IOException) {

               } catch (e: IllegalArgumentException) {
               } finally {
                   try {
                       s.close()
                   } catch (e: Exception) {
                   }
               }
           }
           host.hardwareAddress = HardwareAddress.getHardwareAddress(address)
           if (!NetInfo.NOMAC.equals(host.hardwareAddress)) {
               return host
           }

       }catch(e:Exception){
            Log.e("TAG",e.toString())
       }
        return null
    }


    private fun getRate(): Int {
        return mRateControl.rate
    }
}