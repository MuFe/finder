package com.camera.finder.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.jaeger.library.StatusBarUtil
import com.mufe.mvvm.library.base.BaseModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class SettingFragment() : BaseFragment() {
    private lateinit var mBinding: FragmentSettingBinding
    private val mVm: SettingViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSettingBinding.inflate(inflater)
        mBinding.lifecycleOwner = this
        mBinding.vm = mVm
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mVm.event.observe(viewLifecycleOwner, { event ->
            when (event) {
                SettingViewModel.ViewModelEvent.ShareEvent->{

                }
                is SettingViewModel.ViewModelEvent.UrlEvent -> {
                    val intent = Intent();
                    intent.setAction("android.intent.action.VIEW");
                    val content_url = Uri.parse(event.url);
                    intent.setData(content_url);
                    startActivity(intent);
                }
                SettingViewModel.ViewModelEvent.EmailEvent -> {
                    val email = arrayOf("qindundun01@gmail.com")
                    val intent = Intent(Intent.ACTION_SEND)

                    intent.type = "message/rfc822" // 设置邮件格式


                    intent.putExtra(Intent.EXTRA_EMAIL, email) // 接收人


                    startActivity(Intent.createChooser(intent, "请选择邮件类应用"))
                }
                SettingViewModel.ViewModelEvent.RateEvent -> {
                    val manager = ReviewManagerFactory.create(requireContext())
                    val request = manager.requestReviewFlow()
                    request.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // We got the ReviewInfo object
                            try {
                                val reviewInfo = task.result as ReviewInfo
                                val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
//                                if (flow.isSuccessful) {
//                                    val uri =
//                                        Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)
//                                    val intent = Intent(Intent.ACTION_VIEW, uri)
//                                    intent.setPackage("com.android.vending")
//                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                                    requireContext().startActivity(intent)
//
//                                } else {
//                                    flow.addOnCompleteListener { it ->
//                                        if (it.isSuccessful) {
//
//                                        }
//                                    }
//                                }
                                flow.addOnCompleteListener { it ->

                                }
                            } catch (e: Exception) {

                            }

                        } else {
                            // There was some problem, log or handle the error code.

                        }
                    }
//
                }
            }
        })

    }

    override fun getBaseModel(): BaseModel {
        return mVm
    }
}