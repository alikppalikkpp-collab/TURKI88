package com.example.util

import kotlin.math.abs

data class Candle(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

data class StrategyResult(
    val pair: String,
    val direction: String, // "BUY" or "SELL"
    val entryPrice: Double,
    val supportLevel: Double,
    val resistanceLevel: Double,
    val confidence: Int,
    val validationMessage: String,
    val breakoutVolume: Double,
    val averageVolume: Double
)

object BreakAndRetestStrategy {

    /**
     * Identifies dynamic support and resistance levels based on last X candles.
     */
    fun calculateSupportAndResistance(candles: List<Candle>): Pair<Double, Double> {
        if (candles.isEmpty()) return Pair(0.0, 0.0)
        var highest = Double.MIN_VALUE
        var lowest = Double.MAX_VALUE
        for (candle in candles) {
            if (candle.high > highest) highest = candle.high
            if (candle.low < lowest) lowest = candle.low
        }
        return Pair(lowest, highest)
    }

    /**
     * Core Break & Retest technical strategy analyzer.
     * Checks for volume spike confirmation & minimum price displacement to minimize false anomalies.
     */
    fun analyze(
        candles: List<Candle>,
        pairName: String,
        candleWindow: Int = 14,
        minBreakoutPercent: Double = 0.04, // 0.04% displacement
        minVolumeFactor: Double = 1.25 // volume >= 1.25x average
    ): StrategyResult? {
        val totalNeeded = candleWindow + 3
        if (candles.size < totalNeeded) return null

        // 1. Calculate dynamic levels over the historical window
        val historicalWindow = candles.subList(0, candleWindow)
        val (support, resistance) = calculateSupportAndResistance(historicalWindow)
        if (support <= 0.0 || resistance <= 0.0) return null

        // Calculate baseline daily average volume
        val averageVolume = historicalWindow.map { it.volume }.average()

        // Three key operational candles:
        // C1: Breakout candle
        // C2: Retest candle
        // C3: Confirmation candle
        val c1Index = candles.size - 3
        val c2Index = candles.size - 2
        val c3Index = candles.size - 1

        val breakoutCandle = candles[c1Index]
        val retestCandle = candles[c2Index]
        val confirmationCandle = candles[c3Index]

        val breakoutVolumeOk = breakoutCandle.volume >= (averageVolume * minVolumeFactor)

        // A. Bullish (BUY / Call) Break and Retest Setup
        val isBullishBreakout = breakoutCandle.close > resistance &&
                ((breakoutCandle.close - resistance) / resistance * 100.0) >= minBreakoutPercent

        if (isBullishBreakout && breakoutVolumeOk) {
            // Retest verification:
            // Lowest point touches/dips to former resistance (now acting as support)
            val touchTolerancePercent = 0.02
            val retestDippedToResistance = retestCandle.low <= (resistance * (1.0 + touchTolerancePercent / 100.0))
            
            // Critical rule: Must NOT close inside the original level (protect from fakeouts)
            val closedAboveResistance = retestCandle.close >= resistance

            if (retestDippedToResistance && closedAboveResistance) {
                // Bullish confirmation: closing price increases from open and validates support
                val isBullishConfirmation = confirmationCandle.close > confirmationCandle.open &&
                        confirmationCandle.close >= retestCandle.close

                if (isBullishConfirmation) {
                    // Safe mapped confidence logic <90 and >=70
                    val computedConfidence = 74 + ((breakoutCandle.volume / averageVolume) * 3).toInt().coerceIn(0, 10)
                    return StrategyResult(
                        pair = pairName,
                        direction = "BUY",
                        entryPrice = confirmationCandle.close,
                        supportLevel = support,
                        resistanceLevel = resistance,
                        confidence = computedConfidence.coerceIn(72, 86),
                        breakoutVolume = breakoutCandle.volume,
                        averageVolume = averageVolume,
                        validationMessage = "🚀 كسر مقاومة صاعد مؤكد بنجاح وإعادة اختبار للقاع مع استقرار السعر"
                    )
                }
            }
        }

        // B. Bearish (SELL / Put) Break and Retest Setup
        val isBearishBreakout = breakoutCandle.close < support &&
                ((support - breakoutCandle.close) / support * 100.0) >= minBreakoutPercent

        if (isBearishBreakout && breakoutVolumeOk) {
            // Retest verification:
            // High point touches/rallies to former support (now acting as resistance)
            val touchTolerancePercent = 0.02
            val retestRalliedToSupport = retestCandle.high >= (support * (1.0 - touchTolerancePercent / 100.0))
            
            // Critical rule: Must NOT close inside the original level (re-entry)
            val closedBelowSupport = retestCandle.close <= support

            if (retestRalliedToSupport && closedBelowSupport) {
                // Bearish confirmation: closing price decreases from open and validates resistance
                val isBearishConfirmation = confirmationCandle.close < confirmationCandle.open &&
                        confirmationCandle.close <= retestCandle.close

                if (isBearishConfirmation) {
                    val computedConfidence = 74 + ((breakoutCandle.volume / averageVolume) * 3).toInt().coerceIn(0, 10)
                    return StrategyResult(
                        pair = pairName,
                        direction = "SELL",
                        entryPrice = confirmationCandle.close,
                        supportLevel = support,
                        resistanceLevel = resistance,
                        confidence = computedConfidence.coerceIn(72, 86),
                        breakoutVolume = breakoutCandle.volume,
                        averageVolume = averageVolume,
                        validationMessage = "📉 كسر دعم هابط مؤكد بنجاح وإعادة اختبار للقمة مع استقرار السعر"
                    )
                }
            }
        }

        return null
    }

    /**
     * Generates simulated high-fidelity candles to feed into the algorithm for any currency pair.
     */
    fun createSimulatedScenario(pairName: String, action: String): List<Candle> {
        val candlesList = mutableListOf<Candle>()
        val startPrice = com.example.util.PriceSyncHelper.getPrice(pairName)

        val random = java.util.Random()

        // Step 1: Generate dynamic background buffer representing channel consolidation (14 candles)
        var currentRef = startPrice
        for (i in 1..14) {
            val open = currentRef
            val close = currentRef + (random.nextGaussian() * (startPrice * 0.0006))
            val high = maxOf(open, close) + (random.nextDouble() * (startPrice * 0.0004))
            val low = minOf(open, close) - (random.nextDouble() * (startPrice * 0.0004))
            val volume = random.nextDouble(120.0, 240.0)
            candlesList.add(Candle(open, high, low, close, volume))
            currentRef = close
        }

        // Detect levels across these 14 candles
        val (support, resistance) = calculateSupportAndResistance(candlesList)

        if (action.uppercase() == "BUY") {
            // Generate bullish breakout
            val c1Open = currentRef
            val c1Close = resistance + (startPrice * 0.0015) // Clean break
            val c1High = c1Close + (startPrice * 0.0002)
            val c1Low = c1Open - (startPrice * 0.0001)
            val c1Volume = 320.0 // True breakout volume
            candlesList.add(Candle(c1Open, c1High, c1Low, c1Close, c1Volume))

            // Retest candle: falls exactly to former resistance, but closes above it
            val c2Open = c1Close
            val c2Close = resistance + (startPrice * 0.0004)
            val c2Low = resistance - (startPrice * 0.00002) // Touched/Dipped slightly below Resistance
            val c2High = c2Open + (startPrice * 0.0002)
            val c2Volume = 140.0
            candlesList.add(Candle(c2Open, c2High, c2Low, c2Close, c2Volume))

            // Confirmation candle: Bullish engulfing/marubozu style
            val c3Open = c2Close
            val c3Close = c2Close + (startPrice * 0.0011)
            val c3High = c3Close + (startPrice * 0.0002)
            val c3Low = c3Open - (startPrice * 0.0001)
            val c3Volume = 210.0
            candlesList.add(Candle(c3Open, c3High, c3Low, c3Close, c3Volume))
        } else {
            // Generate bearish breakout
            val c1Open = currentRef
            val c1Close = support - (startPrice * 0.0015) // Clean breakdown
            val c1High = c1Open + (startPrice * 0.0001)
            val c1Low = c1Close - (startPrice * 0.0002)
            val c1Volume = 340.0 // True volume breakout
            candlesList.add(Candle(c1Open, c1High, c1Low, c1Close, c1Volume))

            // Retest candle: rises exactly to former support, but closes below it
            val c2Open = c1Close
            val c2Close = support - (startPrice * 0.0004)
            val c2High = support + (startPrice * 0.00002) // Touched/Dipped slightly above Support
            val c2Low = c2Close - (startPrice * 0.0002)
            val c2Volume = 135.0
            candlesList.add(Candle(c2Open, c2High, c2Low, c2Close, c2Volume))

            // Confirmation candle: Bearish movement
            val c3Open = c2Close
            val c3Close = c2Close - (startPrice * 0.0011)
            val c3High = c3Open + (startPrice * 0.0001)
            val c3Low = c3Close - (startPrice * 0.0002)
            val c3Volume = 230.0
            candlesList.add(Candle(c3Open, c3High, c3Low, c3Close, c3Volume))
        }

        return candlesList
    }
}
