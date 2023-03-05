package com.camera.finder.di



import android.os.Environment
import com.google.gson.GsonBuilder
import com.camera.finder.BuildConfig
import com.camera.finder.util.PreferenceUtil
import com.mufe.mvvm.library.network.ApiService
import com.mufe.mvvm.library.util.IntTypeAdapter
import okhttp3.Cache
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


val networkModule = module {
    val BASE_URL=""
    factory {
        get<Retrofit>{ parametersOf(get<String>(named(BASE_URL)))}.create(ApiService::class.java)
    }
    var gson= GsonBuilder()
        .registerTypeAdapter(Int::class.javaPrimitiveType, IntTypeAdapter())
        .registerTypeAdapter(Int::class.java, IntTypeAdapter()).create()
    var fac=GsonConverterFactory.create(gson)
    factory { (baseUrl:String)->
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(get())
            .addConverterFactory(fac)
            .build()
    }
   // var cacheControlInterceptor = CacheControlInterceptor()

    factory {
        val builder = OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(2000, TimeUnit.SECONDS)
            .addInterceptor {
                val request = it.request()
                val p=PreferenceUtil(get())
                val headers: Headers = Headers.Builder().add("Authorization",p.getToken()).addAll(request.headers()).build()
                it.let { chain ->
                    chain
                }.proceed(
                    request
                        .newBuilder()
                        .headers(headers)
                        .build()
                )
            }
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(logging)
        }
        builder.build()
    }

    factory {
        var gson= GsonBuilder()
            .registerTypeAdapter(Int::class.javaPrimitiveType, IntTypeAdapter())
            .registerTypeAdapter(Int::class.java, IntTypeAdapter()).create()
        GsonConverterFactory.create(gson)
    }

    factory(named(BASE_URL)) {
//        "http://113.31.157.71:8000/"
        "https://mock.apifox.cn/m1/1944612-0-default/"
    }

}