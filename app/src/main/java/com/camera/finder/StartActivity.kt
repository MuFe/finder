package com.camera.finder



import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.camera.finder.R
import com.camera.finder.databinding.ActivityMainBinding
import com.camera.finder.databinding.ActivityStartBinding
import com.camera.finder.ui.MainHost
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.camera.finder.util.PreferenceUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class StartActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityStartBinding
    private val mVm: MainActivityViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityStartBinding.inflate(layoutInflater)
        mBinding.lifecycleOwner = this
        mBinding.vm = this
        getWindow().setStatusBarColor(resources.getColor(R.color.black));
        setContentView(mBinding.root)
        mVm.delayStart {
            startActivity(Intent(this@StartActivity,MainActivity::class.java))
            finish()
        }
    }



}