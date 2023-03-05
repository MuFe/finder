package com.camera.finder.ui


import android.content.res.Resources
import androidx.navigation.Navigation
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.camera.finder.R
import com.camera.finder.util.PreferenceUtil
import com.mufe.mvvm.library.base.BaseModel
import com.mufe.mvvm.library.base.BaseOwner
import com.mufe.mvvm.library.extension.toast
import com.mufe.mvvm.library.network.Status
import org.koin.android.ext.android.inject


open class BaseFragment : Fragment(), BaseOwner {
    val mPreferenceUtil: PreferenceUtil by inject()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBaseModel()?.baseEvent?.observe(viewLifecycleOwner, { event ->
            when (event) {
                is BaseModel.BaseViewModelEvent.NetworkEvent->{
                    when(event.resource.status){
                        Status.LOADING->{

                        }
                        Status.ERROR->{
                           requireContext().toast(event.resource.message.orEmpty())
                        }
                        else->{

                        }
                    }
                }
                is BaseModel.BaseViewModelEvent.ToastIntEvent->{
                    requireContext().toast(getBaseResources().getString(event.id))
                }
                is BaseModel.BaseViewModelEvent.NavigateEvent->{
                    navigate(event.id,event.bundle)
                }
                is BaseModel.BaseViewModelEvent.ToastStrEvent->{
                    requireContext().toast(event.str)
                }
                BaseModel.BaseViewModelEvent.LogoutEvent->{
                    mPreferenceUtil.clearLogin()
                    (activity as? MainHost)?.resetNavToLogin()
                }
                BaseModel.BaseViewModelEvent.NavigateUpEvent->{
                    navigateUp()
                }
                else->{

                }
            }
        })
    }

    override fun navigateUp() {
        val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        navController.navigateUp()
    }

    fun navigateUp(id: Int) {
        val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        navController.popBackStack(id, false)
    }

    override fun navigate(id: Int) {
        val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        val navDestination = navController.graph.findNode(id)
        if (navDestination != null) {
            if (navDestination.id == navController.currentDestination!!.id) {
                return
            }
        }
        navController.navigate(id)
    }


    fun getBaseResources(): Resources {
        return requireContext().resources
    }




    fun navigate(id: Int, bundle: Bundle?) {
        val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        val navDestination = navController.graph.findNode(id)
        if (navDestination != null) {
            if (navDestination.id == navController.currentDestination?.id) {
                return
            }
        }
        navController.navigate(id, bundle)
    }





   override fun getBaseModel():BaseModel? {
        return null
    }



}