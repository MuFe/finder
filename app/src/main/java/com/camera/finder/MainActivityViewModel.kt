package com.camera.finder


import android.content.Context
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.camera.finder.util.GoogleUtil
import com.mufe.mvvm.library.base.BaseModel


class MainActivityViewModel(
  private val googleUtil: GoogleUtil,
) : BaseModel() {

    fun initGoogle(activity: MainActivity){
        viewModelScope.launch {
            delay(100)
            googleUtil.initGoogle(activity){
               // mBaseEvent.postValue(BaseViewModelEvent.NavigateEvent(R.id.navigation_sub, bundleOf()))
            }
        }
    }






    sealed class ViewModelEvent {

    }



}