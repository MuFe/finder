package com.camera.finder.ui


import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.camera.finder.R
import com.camera.finder.databinding.DialogPermissonBinding
import com.mufe.mvvm.library.base.BaseDialogFragment


class PermissionDialogFragment() : BaseDialogFragment() {
    companion object {
        const val REQUEST_KEY = "permissionDialog"
        const val RESULT_KEY = "Success"

        fun getInstance(): PermissionDialogFragment {
            return PermissionDialogFragment()
        }
    }

    private lateinit var mBinding: DialogPermissonBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = DialogPermissonBinding.inflate(inflater)
        mBinding.lifecycleOwner = this
        mBinding.vm = this
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val window = this.dialog!!.window
        val lp = window!!.attributes
        val dm = resources.displayMetrics
        lp.width = dm.widthPixels - 58
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.LoginDialog);
    }

   fun enter(){
       setFragmentResult(REQUEST_KEY, bundleOf())
       dismissAllowingStateLoss()
   }

}