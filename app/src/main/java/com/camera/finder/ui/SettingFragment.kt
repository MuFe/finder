package com.camera.finder.ui

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Allocation.USAGE_SCRIPT
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.lifecycle.MutableLiveData
import com.camera.finder.adapter.ScannerDetailAdapter
import com.camera.finder.data.WifiData
import com.camera.finder.databinding.FragmentDetailBinding
import com.camera.finder.databinding.FragmentSettingBinding
import com.camera.finder.util.GoogleUtil
import com.jaeger.library.StatusBarUtil
import org.koin.android.ext.android.inject


class SettingFragment() : BaseFragment() {
    private lateinit var mBinding: FragmentSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSettingBinding.inflate(inflater)
        mBinding.lifecycleOwner = this
        mBinding.vm = this
        return mBinding.root
    }




    fun goBack() {
        navigateUp()
    }
}