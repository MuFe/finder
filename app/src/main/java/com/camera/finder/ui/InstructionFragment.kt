package com.camera.finder.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
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
import com.camera.finder.databinding.FragmentInstructionBinding
import com.camera.finder.databinding.FragmentSettingBinding
import com.camera.finder.util.GoogleUtil
import org.koin.android.ext.android.inject


class InstructionFragment() : BaseFragment() {
    private lateinit var mBinding: FragmentInstructionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentInstructionBinding.inflate(inflater)
        mBinding.lifecycleOwner = this
        mBinding.vm = this
        return mBinding.root
    }


    fun click(drawable:Drawable,str:String){
        InstructionDialogFragment.getInstance(str,drawable).show(parentFragmentManager,InstructionDialogFragment::class.simpleName)
    }


    fun goBack() {
        navigateUp()
    }
}