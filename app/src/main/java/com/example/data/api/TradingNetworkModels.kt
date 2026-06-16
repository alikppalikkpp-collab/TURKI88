package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ValidateRequest(
    @Json(name = "key") val key: String,
    @Json(name = "device_id") val deviceId: String
)

@JsonClass(generateAdapter = true)
data class ValidateResponse(
    @Json(name = "status") val status: String,
    @Json(name = "plan") val plan: String? = null
)

@JsonClass(generateAdapter = true)
data class SignalResponse(
    @Json(name = "pair") val pair: String,
    @Json(name = "direction") val direction: String, // "BUY" or "SELL"
    @Json(name = "price") val price: Double,
    @Json(name = "duration") val duration: Int,
    @Json(name = "confidence") val confidence: Int,
    @Json(name = "strength") val strength: String // "STRONG", "MEDIUM", "WEAK"
)

@JsonClass(generateAdapter = true)
data class AssetResponse(
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String // "FOREX", "METALS", "CRYPTO", "OTC"
)
