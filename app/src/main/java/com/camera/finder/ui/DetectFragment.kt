package com.camera.finder.ui

import android.Manifest
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.MutableLiveData
import com.camera.finder.databinding.FragmentDetectBinding
import com.camera.finder.util.ImageUtil
import com.google.common.util.concurrent.ListenableFuture
import com.mufe.mvvm.library.extension.checkPermissions
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
    val hide = MutableLiveData<Boolean>()
    var point: Point? = null
    var lastTime = 0L
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentDetectBinding.inflate(inflater)
        mBinding.lifecycleOwner = this
        mBinding.vm = this
        isBack.value = true
        hide.value = true
        return mBinding.root
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

    fun change() {
        isBack.value = !isBack.value!!
        initBind()
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
        cameraProviderFuture.addListener({  //给cameraProviderFuture添加监听
            initBind()
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    fun initBind() {
        val cameraProvider = cameraProviderFuture.get()  //获取相机信息
        val cameraSelector =
            if (isBack.value!!) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA  //默认后置摄像头
        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(mBinding.previewView.getSurfaceProvider()) }  //viewFinder设置预览画面
        cameraProvider.unbindAll()  //先解除再绑定生命周期
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
            analyze(imageProxy)
        })
        camera = cameraProvider.bindToLifecycle(
            viewLifecycleOwner,
            cameraSelector,
            imageAnalysis,
//            preview,
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

//颜色检查的上限和下限
        //颜色检查的上限和下限
        Core.inRange(hsv, Scalar(0.0, 123.0, 100.0), Scalar(5.0, 255.0, 200.0), dst)


        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(3.0, 3.0))
        Imgproc.morphologyEx(dst, dst, Imgproc.MORPH_OPEN, kernel)

        val contours: List<MatOfPoint> = ArrayList() //存储提取后的轮廓对象集合

        val hireachy = Mat() //起到一个提取过程中间转换暂存的作用。
        Imgproc.findContours(
            dst, contours, hireachy, Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        ) //执行轮廓提取

        var maxArea = 0.0
        var rect: Rect? = null
        if (contours.size < 3) {

        }
        for ((i, wrapper) in contours.withIndex()) {
            val area = Imgproc.contourArea(wrapper)
            if (area < 200 && area > 100) {
                if (area > maxArea) {
                    maxArea = area
                    rect = Imgproc.boundingRect(wrapper) //将该区域转为Rect矩形对象
                }
            }
        }

        //得到最大的对象
        //得到最大的对象
        if (rect != null) {
            lastTime = System.currentTimeMillis()
            if (point == null) {
                point = Point(rect.x.toDouble() - 5.0, rect.y.toDouble() - 5.0)
            } else {
                if (Math.abs(point!!.x - rect.x.toDouble()) > 10 || Math.abs(point!!.y - rect.y.toDouble()) > 10) {
                    point!!.x = rect.x.toDouble() - 5.0
                    point!!.y = rect.y.toDouble() - 5.0
                }
            }
            Imgproc.circle(rgb, point, 20, Scalar(125.0, 43.0, 46.0), 4)
        } else {
            if (lastTime != 0L && System.currentTimeMillis() - lastTime < 3500) {
                Imgproc.circle(rgb, point, 20, Scalar(125.0, 43.0, 46.0), 4)
            }
        }
        val bitmap5: Bitmap =
            Bitmap.createBitmap(rgb.width(), rgb.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(rgb, bitmap5)
        requireActivity().runOnUiThread {
            //根据自己的ImageView进行替换就可以了。
            mBinding.image.setImageBitmap(bitmap5)
        }
        util.release();

        imageProxy.close()
    }


}