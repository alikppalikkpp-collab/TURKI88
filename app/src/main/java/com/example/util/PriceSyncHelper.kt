package com.example.util

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

object PriceSyncHelper {
    // Current prices matching Quotex
    private val prices = ConcurrentHashMap<String, Double>()
    private var isSyncingStarted = false

    init {
        // High-fidelity fallback defaults
        prices["EUR/USD"] = 1.08542
        prices["EUR/USD OTC"] = 1.08542
        prices["GBP/USD"] = 1.27425
        prices["GBP/USD OTC"] = 1.27425
        prices["USD/JPY"] = 158.324
        prices["USD/JPY OTC"] = 158.324
        prices["EUR/GBP"] = 0.85213
        prices["USD/CHF"] = 0.89422
        prices["AUD/USD"] = 0.66518
        prices["NZD/USD"] = 0.61225
        prices["XAU/USD"] = 2351.40
        prices["BTC/USD"] = 67342.0
        prices["ETH/USD"] = 3481.50
        prices["BNB/USD"] = 561.40
        prices["SOL/USD"] = 142.15
        prices["XRP/USD"] = 0.4912
        prices["ADA/USD"] = 0.3812
        prices["DOGE/USD"] = 0.1245
        prices["TRX/USD"] = 0.1154
    }

    fun getPrice(asset: String): Double {
        val uppercaseName = asset.trim().uppercase()
        // Strip OTC or other brackets to isolate pair name
        val cleanName = uppercaseName.replace(" OTC", "").replace(" (OTC)", "").trim()
        
        val basePrice = prices[cleanName] ?: prices[uppercaseName] ?: when {
            cleanName.contains("BTC") || uppercaseName.contains("BTC") -> 67420.0
            cleanName.contains("ETH") || uppercaseName.contains("ETH") -> 3480.0
            cleanName.contains("BNB") || uppercaseName.contains("BNB") -> 562.0
            cleanName.contains("SOL") || uppercaseName.contains("SOL") -> 142.15
            cleanName.contains("XRP") || uppercaseName.contains("XRP") -> 0.4912
            cleanName.contains("TRX") || uppercaseName.contains("TRX") -> 0.1154
            cleanName.contains("JPY") -> 158.324
            cleanName.contains("XAU") || cleanName.contains("GOLD") -> 2351.40
            cleanName.contains("GBP") -> 1.27
            else -> 1.0825
        }

        // Apply dynamic micro-drift fluctuation to simulate a live real-time ticking trading interface
        val driftRange = when {
            cleanName.contains("BTC") -> Random.nextDouble(-2.5, 2.5)
            cleanName.contains("ETH") -> Random.nextDouble(-0.18, 0.18)
            cleanName.contains("BNB") -> Random.nextDouble(-0.06, 0.06)
            cleanName.contains("XAU") -> Random.nextDouble(-0.12, 0.12)
            cleanName.contains("JPY") -> Random.nextDouble(-0.008, 0.008)
            else -> Random.nextDouble(-0.00005, 0.00005)
        }
        
        return basePrice + driftRange
    }

    fun startSyncLoop() {
        if (isSyncingStarted) return
        isSyncingStarted = true
        
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            while (true) {
                try {
                    // 1. Fetch live Forex rates via Exchangerate API (Tied directly to USD)
                    val fxRequest = Request.Builder()
                        .url("https://api.exchangerate-api.com/v4/latest/USD")
                        .build()
                    val fxResponse = client.newCall(fxRequest).execute()
                    if (fxResponse.isSuccessful) {
                        fxResponse.body?.string()?.let { bodyString ->
                            val ratesObj = JSONObject(bodyString).getJSONObject("rates")
                            
                            val eur = ratesObj.optDouble("EUR", 0.92)
                            val gbp = ratesObj.optDouble("GBP", 0.784)
                            val jpy = ratesObj.optDouble("JPY", 158.0)
                            val chf = ratesObj.optDouble("CHF", 0.89)
                            val aud = ratesObj.optDouble("AUD", 1.5)
                            val nzd = ratesObj.optDouble("NZD", 1.6)

                            if (eur > 0) {
                                prices["EUR/USD"] = (1.0 / eur)
                                prices["EUR/USD OTC"] = (1.0 / eur)
                            }
                            if (gbp > 0) {
                                prices["GBP/USD"] = (1.0 / gbp)
                                prices["GBP/USD OTC"] = (1.0 / gbp)
                            }
                            if (jpy > 0) {
                                prices["USD/JPY"] = jpy
                                prices["USD/JPY OTC"] = jpy
                            }
                            if (chf > 0) {
                                prices["USD/CHF"] = chf
                                prices["USD/CHF OTC"] = chf
                            }
                            if (aud > 0) {
                                prices["AUD/USD"] = (1.0 / aud)
                                prices["AUD/USD OTC"] = (1.0 / aud)
                            }
                            if (nzd > 0) {
                                prices["NZD/USD"] = (1.0 / nzd)
                                prices["NZD/USD OTC"] = (1.0 / nzd)
                            }
                            if (eur > 0 && gbp > 0) {
                                prices["EUR/GBP"] = (gbp / eur)
                                prices["EUR/GBP OTC"] = (gbp / eur)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PriceSyncHelper", "FX price download failed - utilizing fallback constants", e)
                }

                try {
                    // 2. Fetch live crypto and PAX-GOLD (tracks XAU 1:1) from CoinGecko
                    val cryptoRequest = Request.Builder()
                        .url("https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,binancecoin,ripple,solana,cardano,dogecoin,tron,pax-gold&vs_currencies=usd")
                        .build()
                    val cryptoResponse = client.newCall(cryptoRequest).execute()
                    if (cryptoResponse.isSuccessful) {
                        cryptoResponse.body?.string()?.let { bodyString ->
                            val root = JSONObject(bodyString)
                            if (root.has("bitcoin")) {
                                val btc = root.getJSONObject("bitcoin").getDouble("usd")
                                prices["BTC/USD"] = btc
                            }
                            if (root.has("ethereum")) {
                                val eth = root.getJSONObject("ethereum").getDouble("usd")
                                prices["ETH/USD"] = eth
                            }
                            if (root.has("binancecoin")) {
                                val bnb = root.getJSONObject("binancecoin").getDouble("usd")
                                prices["BNB/USD"] = bnb
                            }
                            if (root.has("solana")) {
                                val sol = root.getJSONObject("solana").getDouble("usd")
                                prices["SOL/USD"] = sol
                            }
                            if (root.has("ripple")) {
                                val xrp = root.getJSONObject("ripple").getDouble("usd")
                                prices["XRP/USD"] = xrp
                            }
                            if (root.has("cardano")) {
                                val ada = root.getJSONObject("cardano").getDouble("usd")
                                prices["ADA/USD"] = ada
                            }
                            if (root.has("dogecoin")) {
                                val doge = root.getJSONObject("dogecoin").getDouble("usd")
                                prices["DOGE/USD"] = doge
                            }
                            if (root.has("tron")) {
                                val trx = root.getJSONObject("tron").getDouble("usd")
                                prices["TRX/USD"] = trx
                            }
                            if (root.has("pax-gold")) {
                                val gold = root.getJSONObject("pax-gold").getDouble("usd")
                                prices["XAU/USD"] = gold
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PriceSyncHelper", "Crypto price download failed - utilizing cached constants", e)
                }

                // Poll every 10 seconds to align perfectly with live global market ticks
                delay(10000)
            }
        }
    }

    fun formatPrice(pairName: String, price: Double): String {
        val uppercaseName = pairName.trim().uppercase()
        val digits = when {
            uppercaseName.contains("BTC") -> 2
            uppercaseName.contains("XAU") || uppercaseName.contains("GOLD") || uppercaseName.contains("JPY") -> 3
            else -> 5
        }
        return String.format(java.util.Locale.US, "%.${digits}f", price)
    }
}
