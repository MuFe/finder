package com.camera.finder.adapter


import com.camera.finder.BR
import com.camera.finder.R
import com.camera.finder.data.WifiData
import com.camera.finder.databinding.AdapterScannerDetailBinding
import com.mufe.mvvm.library.base.BaseBindingAdapter
import com.mufe.mvvm.library.base.BaseViewHolder

class ScannerDetailAdapter(val mOnClickListener:()->Unit):BaseBindingAdapter<WifiData,AdapterScannerDetailBinding>() {
    override fun getLayoutResId(viewType: Int): Int {
       return R.layout.adapter_scanner_detail
    }

    fun click(){
        mOnClickListener()
    }

    override fun onBindItem(
        binding: AdapterScannerDetailBinding?,
        item: WifiData,
        holder: BaseViewHolder
    ) {
        if(binding!=null){
            binding.setVariable(BR.data,item)
            binding.setVariable(BR.vm,this)
            binding.executePendingBindings()
        }
    }
}