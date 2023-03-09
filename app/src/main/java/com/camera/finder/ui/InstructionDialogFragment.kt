package com.camera.finder.ui


import android.Manifest
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.MutableLiveData
import com.camera.finder.R
import com.camera.finder.databinding.DialogInstructionBinding
import com.camera.finder.databinding.DialogPermissonBinding
import com.mufe.mvvm.library.base.BaseDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class InstructionDialogFragment(val content:String,val icon:Drawable) : BaseDialogFragment() {
    companion object {
        const val REQUEST_KEY = "InstructionDialog"


        fun getInstance(content:String, icon:Drawable): InstructionDialogFragment {
            return InstructionDialogFragment(content,icon)
        }
    }

    private lateinit var mBinding: DialogInstructionBinding

    val title= MutableLiveData<String>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = DialogInstructionBinding.inflate(inflater)
        mBinding.lifecycleOwner = this
        mBinding.vm = this
        mBinding.icon.setBackgroundDrawable(icon)
        title.value=content
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val window = this.dialog!!.window
        val lp = window!!.attributes
        val dm = resources.displayMetrics
        lp.width = dm.widthPixels - dpToPx(requireContext(),40)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.LoginDialog);
    }

   fun enter(){
       dismissAllowingStateLoss()
   }

    fun dpToPx(context: Context, dp:Int):Int{
        return (dp*context.resources.displayMetrics.density-0.5f).toInt()
    }


}