package com.camera.finder.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.DiscoveryListener
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
import androidx.lifecycle.viewModelScope
import com.camera.finder.R
import com.camera.finder.databinding.FragmentScanBinding
import com.camera.finder.util.NetInfo
import com.mufe.mvvm.library.base.BaseModel
import com.mufe.mvvm.library.extension.checkPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.DatagramPacket
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import javax.jmdns.ServiceTypeListener
import javax.jmdns.impl.DNSOutgoing
import javax.jmdns.impl.DNSQuestion

import javax.jmdns.impl.JmDNSImpl
import javax.jmdns.impl.ServiceInfoImpl
import javax.jmdns.impl.constants.DNSRecordClass
import javax.jmdns.impl.constants.DNSRecordType
import kotlin.random.Random


class ScanFragment() : BaseFragment() {
    private lateinit var mBinding: FragmentScanBinding
    private val mVm: ScanViewModel by viewModel()
    private lateinit var connMgr: ConnectivityManager
    private lateinit var net: NetInfo
    private lateinit var nsdManager: NsdManager
    private val SERVICE_TYPE = "_http._tcp"
    private var mDiscoveryListener: DiscoveryListener? = null
    private var mResolverListener: NsdManager.ResolveListener? = null
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
        val wifi = requireActivity().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val lock = wifi.createMulticastLock(javaClass.simpleName)
        lock.setReferenceCounted(false)
        lock.acquire();
        nsdManager =requireContext().getSystemService(Context.NSD_SERVICE) as NsdManager
        createDiscoverListener();
        return mBinding.root
    }

    private fun createDiscoverListener() {

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
                    Log.e("TAG",v.name.orEmpty())
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
//        mVm.viewModelScope.launch(Dispatchers.IO){
//            val jmdns = JmDNS.create() as JmDNSImpl
//            jmdns.addServiceTypeListener(object :ServiceTypeListener{
//                override fun serviceTypeAdded(event: ServiceEvent?) {
//                    jmdns.addServiceListener(event!!.type,object:ServiceListener{
//                        override fun serviceAdded(event: ServiceEvent?) {
//                            Log.e("TAG1","111"+event!!.info.name)
//                        }
//
//                        override fun serviceRemoved(event: ServiceEvent?) {
//                            Log.e("TAG2","111"+event!!.type.orEmpty())
//                        }
//
//                        override fun serviceResolved(event: ServiceEvent?) {
//                            Log.e("TAG13","111"+event!!.type.orEmpty())
//                        }
//                    })
//
//                }
//
//                override fun subTypeForServiceTypeAdded(event: ServiceEvent?) {
//
//                }
//            })
////            val d=DNSOutgoing(0)
////            d.addQuestion(DNSQuestion.newQuestion("_adb._tcp.local",DNSRecordType.TYPE_PTR,DNSRecordClass.CLASS_IN,false))
////            d.addQuestion(DNSQuestion.newQuestion("_airplay._tcp.local",DNSRecordType.TYPE_PTR,DNSRecordClass.CLASS_IN,false))
////            d.addQuestion(DNSQuestion.newQuestion("_raop._tcp.local",DNSRecordType.TYPE_PTR,DNSRecordClass.CLASS_IN,false))
////            d.addQuestion(DNSQuestion.newQuestion("_services._dns-sd._udp.local",DNSRecordType.TYPE_PTR,DNSRecordClass.CLASS_IN,false))
////            jmdns.send(d)
////            val mBuffer = ByteArray(1024)
////            val dp = DatagramPacket(mBuffer, mBuffer.size)
////
////            jmdns.socket.receive(dp)
////            Log.e("TAG", "net bios receive = " + String(dp.data))
//            mVm.viewModelScope.launch {
//                mVm.isScan.value=false
//            }
//        }
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