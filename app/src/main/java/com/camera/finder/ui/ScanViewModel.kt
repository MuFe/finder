package com.camera.finder.ui


import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.camera.finder.R
import com.camera.finder.data.WifiData
import com.camera.finder.util.NetBios
import com.camera.finder.util.NetInfo
import com.camera.finder.util.PreferenceUtil
import com.camera.finder.util.ScanningUtil
import com.mufe.mvvm.library.base.BaseModel
import com.mufe.mvvm.library.misc.SingleLiveEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.opencv.dnn.Net


class ScanViewModel(
    val mPreferenceUtil: PreferenceUtil,
    val scanningUtil: ScanningUtil,
) : BaseModel() {
    private val mEvent = SingleLiveEvent<ViewModelEvent>()
    val event: LiveData<ViewModelEvent> = mEvent
    val wifi = MutableLiveData<String>()
    val address = MutableLiveData<String>()
    val getway = MutableLiveData<String>()
    val isScan = MutableLiveData<Boolean>()
    val num = MutableLiveData<Int>()
    val progress = MutableLiveData<String>()
    val isFinish = MutableLiveData<Boolean>()
    val isError = MutableLiveData<Boolean>()
    private val mListData = MutableLiveData<List<WifiData>>()
    val listData: LiveData<List<WifiData>> = mListData
    var isStop = false
    var hostDone = 0

    init {
        wifi.value = "None"
        address.value = "None"
        isScan.value = false
        isError.value = false
        isFinish.value = false
        num.value = 0
    }

    fun start() {
        if(wifi.value.orEmpty().equals("None")){
            isError.value=true
            return
        }
        if(isFinish.value==true){
            mBaseEvent.postValue(BaseViewModelEvent.NavigateEvent(R.id.navigation_scan_detail, bundleOf("data" to mListData.value.orEmpty())))
            return
        }
        if (isScan.value == true) {
            return
        }
        if (isScan.value == false) {
            isScan.value = true
        }
        mEvent.postValue(ViewModelEvent.StartEvent)
    }

    fun reStart() {
        isScan.value=false
        isFinish.value=false
        start()
    }


    fun startScanning(ip: Long, allStart: Long, allEnd: Long) {
        progress.value = "0.0"
        isScan.value = true
        hostDone = 0
        //scann(ip, start, end)
        var startTime = System.currentTimeMillis()
        val total=allEnd - allStart + 1
        viewModelScope.launch(Dispatchers.IO) {
            while(isScan.value!!){
                if (System.currentTimeMillis() - startTime > 500) {
                    startTime = System.currentTimeMillis()
                    viewModelScope.launch(Dispatchers.Main) {
                        var p=100 * hostDone.toFloat() / total.toFloat()
                        if(p>100.0f){
                            p=100.00f
                        }
                        progress.value =
                            String.format("%.2f",p )
                    }
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            val deffereds = mutableListOf<Deferred<Int>>()
            val list = mutableListOf<WifiData>()
            val mutex = Mutex()
            repeat(30) {
                val deferred = async {
                    var pt_move = 2
                    val size = total / 30
                    val start = allStart + it * size
                    val end = allStart + (it + 1) * size

                    var temp: WifiData? = null
                    for (i in start..end) {
                        if (isStop) {
                            break
                        } else {
                             mutex.withLock {    //withLock替代lock()和unlock()
                                 hostDone++
                             }
                            temp = scanningUtil.startScanning(NetInfo.getIpFromLongUnsigned(i))
                            if (temp != null) {
                                    if(!temp.ipAddress.orEmpty().equals(getway.value.orEmpty())&&!temp.ipAddress.orEmpty().equals(address.value.orEmpty())){
                                        temp.deviceType=2
                                    }
                                 mutex.withLock {
                                     if(temp.deviceType==2){
                                            viewModelScope.launch(Dispatchers.Main){
                                                num.value=num.value!!+1
                                             }
                                     }
                                    list.add(temp)
                                }
                            }
                        }
                    }
//                if (ip <= end && ip >= start) {
//                    var temp=scanningUtil.startScanning(NetInfo.getIpFromLongUnsigned(start))
//                    mutex.withLock {	//withLock替代lock()和unlock()
//                        hostDone++
//                    }
//                    if(temp!=null){
//                        mutex.withLock {	//withLock替代lock()和unlock()
//                            list.add(temp!!)
//                        }
//                    }
//
//                    // hosts
//                    var pt_backward = ip
//                    var pt_forward = ip + 1
//                    val size_hosts = size - 1
//                    for (i in 0 .. size_hosts) {
//                        if(isStop){
//                            break
//                        }else{
//                            // Set pointer if of limits
//                            if (pt_backward <= start) {
//                                pt_move = 2
//                            } else if (pt_forward > end) {
//                                pt_move = 1
//                            }
//
//                            // Move back and forth
//                            if (pt_move == 1) {
//                                temp=scanningUtil.startScanning(NetInfo.getIpFromLongUnsigned(pt_backward))
//                                mutex.withLock {	//withLock替代lock()和unlock()
//                                    hostDone++
//                                }
//                                if(System.currentTimeMillis()-startTime>500){
//                                    startTime=System.currentTimeMillis()
//                                    viewModelScope.launch(Dispatchers.Main){
//                                        progress.value=String.format("%.2f",100*hostDone.toFloat()/size.toFloat())
//                                    }
//                                }
//                                if(temp!=null){
//                                    mutex.withLock {	//withLock替代lock()和unlock()
//                                        list.add(temp!!)
//                                    }
//                                }
//                                pt_backward--
//                                pt_move = 2
//                            } else {
//                                mutex.withLock {	//withLock替代lock()和unlock()
//                                    hostDone++
//                                }
//                                temp=scanningUtil.startScanning(NetInfo.getIpFromLongUnsigned(pt_forward))
//                                if(System.currentTimeMillis()-startTime>500){
//                                    startTime=System.currentTimeMillis()
//                                    viewModelScope.launch(Dispatchers.Main){
//                                        progress.value=String.format("0.2f%",100*hostDone.toFloat()/size.toFloat())
//                                    }
//                                }
//                                if(temp!=null){
//                                    mutex.withLock {	//withLock替代lock()和unlock()
//                                        list.add(temp!!)
//                                    }
//                                }
//                                pt_forward++
//                                pt_move = 1
//                            }
//                        }
//
//                    }
//                } else {
//                    var temp:WifiData?=null
//                    for (i in start..end) {
//                        if(isStop){
//                            break
//                        }else{
//                            mutex.withLock {	//withLock替代lock()和unlock()
//                                hostDone++
//                            }
//                            temp=scanningUtil.startScanning(NetInfo.getIpFromLongUnsigned(i))
//                            if(System.currentTimeMillis()-startTime>500){
//                                startTime=System.currentTimeMillis()
//                                viewModelScope.launch(Dispatchers.Main){
//                                    progress.value=String.format("0.2f%",100*hostDone.toFloat()/size.toFloat())
//                                }
//
//                            }
//                            mutex.withLock {	//withLock替代lock()和unlock()
//                                if(temp!=null){
//                                    list.add(temp)
//                                }
//                            }
//                        }
//                    }
//                }
                    return@async 1
                }
                deffereds.add(deferred)
            }

            var result = 0
            deffereds.forEach {
                result += it.await()
            }
            viewModelScope.launch(Dispatchers.Main) {
                if (!isStop) {
                    progress.value = "100.00"
                    isScan.value = false
                    val tempList= mutableListOf<WifiData>()
                    val macList= mutableListOf<String>()
                    for(v in list){
                        if(!macList.contains(v.ipAddress.orEmpty())){
                            tempList.add(v)
                            macList.add(v.ipAddress.orEmpty())
                        }
                    }
                    mListData.postValue(tempList)
                    isFinish.value = true
                }
            }
        }
    }


    sealed class ViewModelEvent {
        object StartEvent : ViewModelEvent()
    }
}