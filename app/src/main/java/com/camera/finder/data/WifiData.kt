package com.camera.finder.data

import android.os.Parcelable
import com.camera.finder.util.HardwareAddress
import com.camera.finder.util.NetInfo

class WifiData(
    var ipAddress:String?="",
    var hardwareAddress: String?=NetInfo.NOMAC,
    var name:String?="",
    var deviceType:Int?=1,
):java.io.Serializable