package com.example.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface TradingService {
    @POST("validate")
    suspend fun validate(
        @Body request: ValidateRequest
    ): ValidateResponse

    @GET("signals")
    suspend fun getSignals(): List<SignalResponse>

    @GET("assets")
    suspend fun getAssets(): List<AssetResponse>
}

object RetrofitClient {
    private var lastUrl: String? = null
    private var cachedService: TradingService? = null

    fun getService(baseUrl: String): TradingService {
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        
        if (sanitizedUrl == lastUrl && cachedService != null) {
            return cachedService!!
        }

        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logger)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val service = retrofit.create(TradingService::class.java)
        lastUrl = sanitizedUrl
        cachedService = service
        return service
    }
}
