package com.camera.finder.util

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.camera.finder.data.WifiData


class PreferenceUtil(val context: Context) {
    companion object {
        private const val PREFERENCE_NAME = "Vest"
        private const val TOKEN_KEY = "token"
        private const val DATA_KEY = "data"
        private const val UID_KEY = "uid"
        private const val FIRST_KEY = "first"
        private const val USERDATA_KEY = "userData"
    }

    private val mSharedPreference =
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)


    fun getToken(): String {
        return mSharedPreference.getString(TOKEN_KEY,"").orEmpty()
    }



    fun getHistory(): List<WifiData>? {
        val temp=mSharedPreference.getString(USERDATA_KEY,"")
        if(!temp.equals("")){
            val t=object : TypeToken<List<WifiData>>() {}.type
            return Gson().fromJson(temp,t)
        }else{
            return mutableListOf()
        }
    }

    fun clearLogin(){
        mSharedPreference.edit {
            putString(TOKEN_KEY, "")
            putInt(UID_KEY, 0)
        }
    }

    fun setToken(token: String, token_type: String) {
        mSharedPreference.edit {
            putString(TOKEN_KEY, token_type +" "+ token)
        }
    }

    fun setListData(data: List<WifiData>){
        mSharedPreference.edit {
            putString(USERDATA_KEY,Gson().toJson(data))
        }
    }



    fun saveDetail(id:String,data:String){
        mSharedPreference.edit {
            putString(DATA_KEY+id, data)
        }
    }
    fun getDetail(id:String):String{
        return mSharedPreference.getString(DATA_KEY+id,"").toString()
    }
    fun isLogin(): Boolean {
        if (getToken().equals("")) {
            return false
        }
        return true
    }

    fun isFirst():Boolean{
        return mSharedPreference.getBoolean(FIRST_KEY,true)
    }

    fun setFirst(){
        mSharedPreference.edit {
            putBoolean(FIRST_KEY, false)
        }
    }

}
