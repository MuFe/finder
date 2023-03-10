package com.camera.finder.ui


import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mufe.mvvm.library.base.BaseModel
import com.mufe.mvvm.library.misc.SingleLiveEvent
import java.text.DecimalFormat


class SettingViewModel(

) : BaseModel() {
    private val mVersion: MutableLiveData<String> = MutableLiveData()
    val version: LiveData<String> = mVersion

    private val mEvent = SingleLiveEvent<ViewModelEvent>()
    val event: LiveData<ViewModelEvent> = mEvent

    val appName = MutableLiveData<String>()


    init {

    }


    fun clickAbout(){
        mEvent.postValue(ViewModelEvent.UrlEvent("https://docs.google.com/document/d/1D2YwrGfTTR1cObaXFdWQmswUybpepYoSDcgpV2kjzwQ/edit?usp=sharing"))
//        mEvent.postValue(ViewModelEvent.UrlEvent("https://ww.baidu.com"))
    }

    fun clickFeedback(){
        mEvent.postValue(ViewModelEvent.EmailEvent)
    }

    fun clickShare(){
        mEvent.postValue(ViewModelEvent.ShareEvent)
    }

    fun clickPrivacy(){
        mEvent.postValue(ViewModelEvent.UrlEvent("https://docs.google.com/document/d/1QL1rCBiFTsBhQRjdnM1ZPVKFDjEUh_IRioL0IR2TIfk/edit?usp=sharing "))
    }

    fun clickRate(){
        mEvent.postValue(ViewModelEvent.RateEvent)
    }
    sealed class ViewModelEvent {
        class UrlEvent(val url:String):ViewModelEvent()
        object EmailEvent:ViewModelEvent()
        object RateEvent:ViewModelEvent()
        object ShareEvent:ViewModelEvent()
    }


}