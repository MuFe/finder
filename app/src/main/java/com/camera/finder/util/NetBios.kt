package com.camera.finder.util

import android.util.Log
import java.io.IOException

/**
 * Created by 郭攀峰 on 2015/10/23.
 */
class NetBios : UdpCommunicate {
    private var mIP: String? = null
    private var mPort = 0

    constructor(ip: String?) : super() {
        mIP = ip
        mPort = 137
    }

    constructor() : super() {}

    fun setIp(ip: String?) {
        mIP = ip
    }

    fun setPort(port: Int) {
        mPort = port
    }

    override fun getPeerIp(): String {
        return mIP!!
    }

    override fun getPort(): Int {
        return mPort
    }

    override fun getSendContent(): ByteArray {
//        val t_ns = ByteArray(50)
//        t_ns[0] = 0x00
//        t_ns[1] = 0x00
//        t_ns[2] = 0x00
//        t_ns[3] = 0x10
//        t_ns[4] = 0x00
//        t_ns[5] = 0x01
//        t_ns[6] = 0x00
//        t_ns[7] = 0x00
//        t_ns[8] = 0x00
//        t_ns[9] = 0x00
//        t_ns[10] = 0x00
//        t_ns[11] = 0x00
//        t_ns[12] = 0x20
//        t_ns[13] = 0x43
//        t_ns[14] = 0x4B
//        for (i in 15..44) {
//            t_ns[i] = 0x41
//        }
//        t_ns[45] = 0x00
//        t_ns[46] = 0x00
//        t_ns[47] = 0x21
//        t_ns[48] = 0x00
//        t_ns[49] = 0x01
        return bytes
    }

    private val bytes = byteArrayOf(171.toByte(), 205.toByte(), 1.toByte(), 32.toByte(),
        0.toByte(), 1.toByte(), 0.toByte(), 0.toByte(),
        0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(),
        9.toByte(), 95.toByte(), 101.toByte(), 118.toByte(),
        97.toByte(), 45.toByte(), 109.toByte(), 101.toByte(),
        115.toByte(), 104.toByte(), 4.toByte(), 95.toByte(),
        116.toByte(), 99.toByte(), 112.toByte(), 5.toByte(),
        108.toByte(), 111.toByte(), 99.toByte(), 97.toByte(),
        108.toByte(), 0.toByte(), 0.toByte(), 12.toByte(),
        0.toByte(), 1.toByte())


    @get:Throws(IOException::class)
    val nbName: String?
        get() {
            val data: ByteArray
            send()
            val dp = receive()
            data = dp.data
            Log.e("TAG", "net bios receive = " + String(data))
            if (data.size > 56) {
                val str = StringBuffer(15)
                for (i in 1..15) {
                    str.append((0xFF and data[56 + i].toInt()).toChar())
                }
                close()
                return str.toString().trim { it <= ' ' }
            }
            return null
        }

    companion object {
        private val tag = NetBios::class.java.simpleName
    }
}