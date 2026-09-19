package com.example.shiyu.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiClient {
    private var token: String? = null
    private var baseUrl: String = ApiConfig.BASE_URL.trim().trimEnd('/') + "/"

    fun setToken(t: String?) { token = t }

    fun getBaseUrl(): String = baseUrl

    fun setBaseUrl(url: String) {
        var normalized = url.trim().trimEnd('/')
        if (normalized.endsWith("/api", ignoreCase = true)) {
            normalized = normalized.substring(0, normalized.length - 4)
        }
        normalized += "/"
        if (normalized != baseUrl) {
            baseUrl = normalized
            retrofit = null
        }
    }

    private val retryInterceptor = Interceptor { chain ->
        var lastException: IOException? = null
        val delays = longArrayOf(1000L, 2000L)
        for (attempt in 0..2) {
            try {
                return@Interceptor chain.proceed(chain.request())
            } catch (e: IOException) {
                lastException = e
                if (attempt < delays.size) {
                    Thread.sleep(delays[attempt])
                }
            }
        }
        throw lastException!!
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                token?.let { request.addHeader("Authorization", "Bearer $it") }
                chain.proceed(request.build())
            }
            .addInterceptor(retryInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .connectTimeout(ApiConfig.TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Volatile
    private var retrofit: Retrofit? = null

    private fun getRetrofit(): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: Retrofit.Builder()
                .baseUrl(baseUrl + "api/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .also { retrofit = it }
        }
    }

    val authApi: AuthApi get() = getRetrofit().create(AuthApi::class.java)
    val syncApi: SyncApi get() = getRetrofit().create(SyncApi::class.java)
    val backendApi: BackendApi get() = getRetrofit().create(BackendApi::class.java)
}
