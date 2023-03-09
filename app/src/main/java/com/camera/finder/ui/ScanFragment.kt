package com.camera.finder.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.lifecycle.viewModelScope
import com.camera.finder.R
import com.camera.finder.databinding.FragmentScanBinding
import com.camera.finder.util.NetInfo
import com.jaeger.library.StatusBarUtil
import com.mufe.mvvm.library.base.BaseModel
import com.mufe.mvvm.library.extension.checkPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.random.Random


class ScanFragment() : BaseFragment() {
    private lateinit var mBinding: FragmentScanBinding
    private val mVm: ScanViewModel by viewModel()
    private lateinit var connMgr: ConnectivityManager
    private lateinit var net: NetInfo

    @RequiresApi(Build.VERSION_CODES.M)
    private val mSettingsResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            parseStoragePermission(false)
        }


    @RequiresApi(Build.VERSION_CODES.M)
    private val mGrantStoragePermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            parseStoragePermission(false)
        }

    @RequiresApi(23)
    private fun isPermissionsGranted(): Boolean {
        return requireContext().checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun parseStoragePermission(first: Boolean) {
        if (!isPermissionsGranted()) {
            if (first) {
                mGrantStoragePermissionResult.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                return
            } else {
                if (!isPermissionsGranted()) {
                    PermissionDialogFragment.getInstance().show(
                        parentFragmentManager,
                        PermissionDialogFragment::class.java.simpleName
                    )
                    return
                }
            }
        }
        start()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentScanBinding.inflate(inflater)
        mBinding.lifecycleOwner = this
        mBinding.vm = mVm
        net = NetInfo(requireContext())
        connMgr =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
        requireActivity().registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(mContext: Context, intent: Intent) {
                // Wifi state

                // Wifi state
                val action: String = intent.getAction().orEmpty()
                if (action != null) {
                    if (action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                        val WifiState: Int = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1)
                        when (WifiState) {
                            WifiManager.WIFI_STATE_ENABLING -> {

                            }
                            WifiManager.WIFI_STATE_ENABLED -> {

                            }
                            WifiManager.WIFI_STATE_DISABLING -> {

                            }
                            WifiManager.WIFI_STATE_DISABLED -> {

                            }
                            else -> {

                            }
                        }
                    }
                    if (action == WifiManager.SUPPLICANT_STATE_CHANGED_ACTION && net.getWifiInfo()) {
                        val sstate: SupplicantState = net.getSupplicantState()
                        //Log.d(TAG, "SupplicantState=" + sstate);
                        if (sstate == SupplicantState.SCANNING) {

                        } else if (sstate == SupplicantState.ASSOCIATING) {

                        } else if (sstate == SupplicantState.COMPLETED) {

                        }
                    }
                }
                val ni = connMgr.getActiveNetworkInfo()
                if (ni != null) {
                    if (ni.detailedState == NetworkInfo.DetailedState.CONNECTED) {
                        val type = ni.type
                        //Log.i(TAG, "NetworkType="+type);
                        if (type == ConnectivityManager.TYPE_WIFI) { // WIFI
                            net.getWifiInfo()
                            if (net.ssid != null) {
                                net.getIp()
                                mVm.address.value = net.ip
                                mVm.getway.value = net.gatewayIp
                                mVm.isError.value=false
                                mVm.wifi.value =
                                    net.ssid.replace("\"", "").replace("<", "").replace(">", "")
                            }
                        }
                    } else {

                    }
                } else {

                }
            }
        }, filter)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mVm.progress.observe(viewLifecycleOwner, { event ->
            val index = event.toFloat().toInt()
            if(index>=100){
                mBinding.lay.setBackgroundResource(R.drawable.f416ff9_16)
            }else{
                val params=mBinding.progress.layoutParams
                params.width=mBinding.lay.measuredWidth*index/100
                mBinding.progress.layoutParams=params
            }
            val count = mBinding.hotLay.childCount
            if (count * 20 <= index) {
                val view = ImageView(requireContext())
                val random = Random(System.currentTimeMillis())
                var height = mBinding.hotLay.measuredHeight - 100
                if (height < 0) {
                    height = 300
                }
                var wdith = mBinding.hotLay.measuredWidth - 100
                if (wdith < 0) {
                    wdith = 300
                }
                val top = random.nextInt(height)
                val start = random.nextInt(wdith)
                val lay = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                lay.topMargin = top
                lay.marginStart = start
                view.layoutParams = lay
                view.alpha = 0.5f
                view.setImageResource(R.drawable.scan_icon)
                mBinding.hotLay.addView(view)
            }
        })
        mVm.listData.observe(viewLifecycleOwner, { listDta ->
            mVm.viewModelScope.launch(Dispatchers.IO) {
                for (v in listDta) {
                    if (v.ipAddress.orEmpty().equals(net.gatewayIp)) {
                        v.deviceType = 0
                    }else{
                    }
                }
//                mVm.viewModelScope.launch(Dispatchers.Main){
//                    if (mVm.isFinish.value == true) {
//                        mVm.isFinish.value=false
//                        navigate(R.id.navigation_scan_detail, bundleOf("data" to listDta))
//                    }
//                }
            }


        })
        mVm.event.observe(viewLifecycleOwner, { event ->
            when (event) {
                ScanViewModel.ViewModelEvent.StartEvent -> {
                  parseStoragePermission(true)
                }
                else->{

                }
            }
        })
    }


    fun start(){
        val params=mBinding.progress.layoutParams
        params.width=0
        mBinding.progress.layoutParams=params
        mBinding.lay.setBackgroundResource(R.drawable.f9bb0f6_16)
        mBinding.hotLay.removeAllViews()
        val network_ip = NetInfo.getUnsignedLongFromIp(net.ip)
        // Detected IP
        val shift = 32 - net.cidr
        var network_start = 0L
        var network_end = 0L
        if (net.cidr < 31) {
            network_start = (network_ip shr shift shl shift) + 1
            network_end = (network_start or ((1 shl shift) - 1).toLong()) - 1
        } else {
            network_start = network_ip shr shift shl shift
            network_end = network_start or ((1 shl shift) - 1).toLong()
        }
         mVm.startScanning(network_ip, network_start, network_end)
    }


    override fun getBaseModel(): BaseModel {
        return mVm
    }
}