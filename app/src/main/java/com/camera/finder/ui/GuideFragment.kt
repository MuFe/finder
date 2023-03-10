package com.camera.finder.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.camera.finder.databinding.FragmentGuideBinding
import com.jaeger.library.StatusBarUtil


class GuideFragment() : BaseFragment() {
    private lateinit var mBinding: FragmentGuideBinding
    val first= MutableLiveData<Boolean>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentGuideBinding.inflate(inflater)
        mBinding.lifecycleOwner = this
        mBinding.vm = this
        first.value=true
        StatusBarUtil.setTranslucentForImageViewInFragment(requireActivity(),0,mBinding.top)
        return mBinding.root
    }
    fun enter(){
        first.value=false
    }

    fun enter1(){
        mPreferenceUtil.setFirst()
        (requireActivity() as MainHost).resetNavToHome()
    }




    fun goBack() {
        navigateUp()
    }
}