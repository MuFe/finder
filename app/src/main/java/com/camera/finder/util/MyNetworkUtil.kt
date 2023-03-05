package com.camera.finder.util


import com.mufe.mvvm.library.network.ApiService
import com.mufe.mvvm.library.util.NetworkUtil


class MyNetworkUtil(val service: ApiService, val preferenceUtil: PreferenceUtil):NetworkUtil() {

}
