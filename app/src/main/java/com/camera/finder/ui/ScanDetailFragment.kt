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
import com.camera.finder.util.GoogleUtil
import org.koin.android.ext.android.inject


class ScanDetailFragment() : BaseFragment() {
    private lateinit var mBinding: FragmentDetailBinding
    private lateinit var adapter: ScannerDetailAdapter
    private lateinit var listener: OnGlobalLayoutListener
    private val googleUtil:GoogleUtil by inject()
    val hide=MutableLiveData<Boolean>()
    val num=MutableLiveData<Int>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentDetailBinding.inflate(inflater)
        mBinding.lifecycleOwner = this
        mBinding.vm = this
        hide.value=true
        adapter = ScannerDetailAdapter() {

        }
        val list=requireArguments().get("data") as List<WifiData>
        num.value=0
        for(v in list){
            if(v.deviceType==2){
                num.value=num.value!!+1
            }
        }
        mPreferenceUtil.setListData(list)
        adapter.items.addAll(list)
        mBinding.recycler.adapter = adapter
//        if(!googleUtil.isHaveSub()&&list.size>0){
//            hide.value=false
//            listener=object:OnGlobalLayoutListener{
//                override fun onGlobalLayout() {
//                    val bitmap = Bitmap.createBitmap(mBinding.recycler.measuredWidth,mBinding.recycler.measuredHeight, Bitmap.Config.ARGB_8888)
//                    val canvas = Canvas(bitmap)
//                    canvas.drawColor(requireContext().resources.getColor(R.color.page_bg))
//                    mBinding.recycler.draw(canvas)
//                    val tempBitmap = rsBlur(requireContext(), bitmap, 23f, 1f)
//                    mBinding.bg.setImageBitmap(tempBitmap)
//                    mBinding.recycler.visibility=View.GONE
//                    mBinding.recycler.viewTreeObserver.removeOnGlobalLayoutListener(listener)
//                }
//            }
//            mBinding.recycler.viewTreeObserver.addOnGlobalLayoutListener(listener)
//        }
        return mBinding.root
    }


    private fun rsBlur(context: Context, source: Bitmap, radius: Float, scale: Float): Bitmap {
        val scaleWidth = (source.getWidth() * scale).toInt()
        val scaleHeight = (source.getHeight() * scale).toInt()
        val scaledBitmap: Bitmap = Bitmap.createScaledBitmap(
            source, scaleWidth,
            scaleHeight, false
        )
        val inputBitmap: Bitmap = scaledBitmap


        //创建RenderScript
        val renderScript: RenderScript = RenderScript.create(context)

        //创建Allocation
        val input: Allocation = Allocation.createFromBitmap(
            renderScript,
            inputBitmap,
            Allocation.MipmapControl.MIPMAP_NONE,
            USAGE_SCRIPT
        )
        val output: Allocation = Allocation.createTyped(renderScript, input.getType())

        //创建ScriptIntrinsic
        val intrinsicBlur: ScriptIntrinsicBlur =
            ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        intrinsicBlur.setInput(input)
        intrinsicBlur.setRadius(radius)
        intrinsicBlur.forEach(output)
        output.copyTo(inputBitmap)
        renderScript.destroy()
        return inputBitmap
    }

    fun clickSub(){
//        navigate(R.id.navigation_sub)
    }

    fun goBack() {
        navigateUp()
    }
}