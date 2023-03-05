package com.camera.finder



import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.camera.finder.R
import com.camera.finder.databinding.ActivityMainBinding
import com.camera.finder.ui.MainHost
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.camera.finder.util.PreferenceUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity(), MainHost {
    private lateinit var mBinding: ActivityMainBinding
    val isHide = MutableLiveData<Boolean>()
    val index = MutableLiveData<Int>()
    private val mVm: MainActivityViewModel by viewModel()
    private val mPreferenceUtil: PreferenceUtil by inject()
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        mBinding.lifecycleOwner = this
        mBinding.vm = this
        setContentView(mBinding.root)
        isHide.value=false
        mVm.initGoogle(this)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)

        navController.graph = navGraph

        if(mPreferenceUtil.isFirst()){
            mPreferenceUtil.setFirst()
            val manager = ReviewManagerFactory.create(this)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // We got the ReviewInfo object
                    try {
                        val reviewInfo = task.result as ReviewInfo
                        val flow = manager.launchReviewFlow(this, reviewInfo)
                        flow.addOnCompleteListener { it ->

                        }
                    } catch (e: Exception) {

                    }

                } else {
                    // There was some problem, log or handle the error code.

                }
            }
        }
        getWindow().setStatusBarColor(resources.getColor(R.color.black));
        navController.addOnDestinationChangedListener { _, destination, _ ->

            if (destination.label == getString(R.string.title_scan)||destination.label==getString(R.string.title_detect)||destination.label==getString(R.string.title_instructions)||destination.label==getString(R.string.title_setting)) {
                isHide.value = false
            } else {
                isHide.value = true
            }
            when (destination.label) {
                getString(R.string.title_scan) -> {
                    index.postValue(0)
                }
                getString(R.string.title_detect) -> {
                    index.postValue(1)
                }
                getString(R.string.title_instructions) -> {
                    index.postValue(2)
                }
                getString(R.string.title_setting) -> {
                    index.postValue(3)
                }

            }
        }

    }

    fun goHome() {

    }

    fun goData() {

    }


    fun goMy() {

    }


    override fun resetNavToHome() {
        val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)

        navController.graph = navGraph

    }
    override fun resetNavToLogin() {

    }

}