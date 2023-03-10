package com.camera.finder.ui

import android.Manifest
import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.animation.addListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.MutableLiveData
import com.camera.finder.R
import com.camera.finder.databinding.FragmentDetectBinding
import com.camera.finder.util.ImageUtil
import com.google.common.util.concurrent.ListenableFuture
import com.jaeger.library.StatusBarUtil
import com.mufe.mvvm.library.extension.checkPermissions
import com.mufe.mvvm.library.util.DpUtil
import org.koin.android.ext.android.inject
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.util.concurrent.Executors


class DetectFragment : BaseFragment() {
    private lateinit var mBinding: FragmentDetectBinding
    private lateinit var camera: Camera
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private val executorService = Executors.newSingleThreadExecutor()
    private val dpUtil:DpUtil by inject()
    companion object {
        init {
            if (!OpenCVLoader.initDebug())
                Log.d("ERROR", "Unable to load OpenCV");
            else
                Log.d("SUCCESS", "OpenCV loaded");
        }
    }

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
    val isBack = MutableLiveData<Boolean>()
    var isAn=false
    val state = MutableLiveData<Int>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentDetectBinding.inflate(inflater)
        mBinding.lifecycleOwner = this
        mBinding.vm = this
        isBack.value = true
        state.value = 0
        val lay=mBinding.scan.layoutParams
        lay.height=requireContext().resources.displayMetrics.widthPixels-dpUtil.dpToPx(requireContext(),26)
        mBinding.scan.layoutParams=lay
        val lay1=mBinding.lay.layoutParams
        lay1.height=lay.height+dpUtil.dpToPx(requireContext(),116)
        mBinding.lay.layoutParams=lay1
        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        StatusBarUtil.setTranslucentForImageViewInFragment(requireActivity(),0,mBinding.previewView)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().getWindow().getDecorView()
            .setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        requireActivity().getWindow().setStatusBarColor(resources.getColor(R.color.black));
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener(PermissionDialogFragment.REQUEST_KEY, { _, bundle ->
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                mGrantStoragePermissionResult.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().getPackageName(), null)
                intent.data = uri
                mSettingsResult.launch(intent)
            }
        })
        parseStoragePermission(true)
    }


    @RequiresApi(23)
    private fun isPermissionsGranted(): Boolean {
        return requireContext().checkPermissions(Manifest.permission.CAMERA)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun parseStoragePermission(first: Boolean) {
        if (!isPermissionsGranted()) {
            if (first) {
                mGrantStoragePermissionResult.launch(arrayOf(Manifest.permission.CAMERA))
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
        initCamera()
    }

    fun initCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({  //???cameraProviderFuture????????????
            initBind()
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    fun initBind() {
        val cameraProvider = cameraProviderFuture.get()  //??????????????????
        val cameraSelector =
            if (isBack.value!!) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA  //?????????????????????
        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(mBinding.previewView.getSurfaceProvider()) }  //viewFinder??????????????????
        cameraProvider.unbindAll()  //??????????????????????????????
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(
                android.util.Size(
                    mBinding.previewView.measuredWidth,
                    mBinding.previewView.measuredHeight
                )
            )
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(executorService, { imageProxy ->
            if(state.value==1&&isAn==false){
                isAn=true
                analyze(imageProxy)
            }else{
                imageProxy.close()
            }
        })
        camera = cameraProvider.bindToLifecycle(
            viewLifecycleOwner,
            cameraSelector,
            imageAnalysis,
            preview,
        )  //Bind use cases to camera
    }

    fun analyze(imageProxy: ImageProxy) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        val util = ImageUtil(imageProxy.getImage()!!)
        val rgb = util.rgba();
        util.rotation(rotationDegrees, rgb);
        val hsv = Mat()
        val hsvSplit = mutableListOf<Mat>()
        Imgproc.GaussianBlur(rgb, rgb, Size(7.0, 7.0), 0.0, 0.0)
        Imgproc.cvtColor(rgb, hsv, Imgproc.COLOR_RGB2HSV_FULL)
        Core.split(hsv, hsvSplit)
        Imgproc.equalizeHist(hsvSplit[2], hsvSplit[2])
        Core.merge(hsvSplit, hsv)
        val dst = Mat()

//??????????????????????????????
        //??????????????????????????????
        Core.inRange(hsv, Scalar(0.0, 123.0, 100.0), Scalar(5.0, 255.0, 200.0), dst)


        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(3.0, 3.0))
        Imgproc.morphologyEx(dst, dst, Imgproc.MORPH_OPEN, kernel)

        val contours: List<MatOfPoint> = ArrayList() //????????????????????????????????????

        val hireachy = Mat() //??????????????????????????????????????????????????????
        Imgproc.findContours(
            dst, contours, hireachy, Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        ) //??????????????????

        var maxArea = 0.0
        var isHave=false
        for ((i, wrapper) in contours.withIndex()) {
            val area = Imgproc.contourArea(wrapper)
            if (area < 200 && area > 100) {
                if (area > maxArea) {
                    maxArea = area
                    isHave=true
                }
            }
        }
        val bitmap5: Bitmap =
            Bitmap.createBitmap(rgb.width(), rgb.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(rgb, bitmap5)
        requireActivity().runOnUiThread {
            //???????????????ImageView???????????????????????????
            mBinding.image.setImageBitmap(bitmap5)
        }
        util.release();
        imageProxy.close()
        requireActivity().runOnUiThread {
           val valueAnimator = ValueAnimator.ofInt(0, 90)
            valueAnimator.addUpdateListener(AnimatorUpdateListener { animation ->
                val result = animation.animatedValue as Int
                val params=mBinding.progress.layoutParams
                params.width=mBinding.lay.measuredWidth*result/100
                mBinding.progress.layoutParams=params
                Log.e("TAG","111111"+"+"+result+"+"+params.width)
            })
            valueAnimator.addListener(object :AnimatorListener{
                override fun onAnimationStart(p0: Animator) {

                }

                override fun onAnimationEnd(p0: Animator) {
                    if(isHave){
                        state.value=3
                    }else{
                        state.value=2
                    }
                    isAn=false
                }

                override fun onAnimationCancel(p0: Animator) {

                }

                override fun onAnimationRepeat(p0: Animator) {

                }
            })
            valueAnimator.setDuration(3000)
            valueAnimator.start()


        }
    }


    fun start(){
        if(state.value!!>=2){
            state.value=0
            mBinding.image.setImageDrawable(null)
        }else{
            state.value=1
        }

    }


}