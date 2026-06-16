package com.example.util

import com.example.data.api.SignalResponse
import kotlin.random.Random

object GlobalSignalManager {
    const val TRADE_DURATION_MINUTES = 10
    const val TRADE_DURATION_SECONDS = TRADE_DURATION_MINUTES * 60 // 600s
    const val SEARCH_DURATION_SECONDS = 20 // 20s of active high-tech searching
    const val CYCLE_SECONDS = TRADE_DURATION_SECONDS + SEARCH_DURATION_SECONDS // 620s

    data class CycleState(
        val cycleIndex: Long,
        val isSearching: Boolean,
        val remainingSeconds: Int,
        val elapsedInCycle: Int
    )

    fun getCycleState(nowSeconds: Long): CycleState {
        val elapsedInCycle = (nowSeconds % CYCLE_SECONDS).toInt()
        val cycleIndex = nowSeconds / CYCLE_SECONDS
        val isSearching = elapsedInCycle < SEARCH_DURATION_SECONDS
        val remainingSeconds = if (isSearching) {
            SEARCH_DURATION_SECONDS - elapsedInCycle
        } else {
            CYCLE_SECONDS - elapsedInCycle
        }
        return CycleState(
            cycleIndex = cycleIndex,
            isSearching = isSearching,
            remainingSeconds = remainingSeconds,
            elapsedInCycle = elapsedInCycle
        )
    }
}

object TradeTimerHelper {

    data class DynamicSignalState(
        val direction: String,
        val price: Double,
        val duration: Int,
        val confidence: Int,
        val strength: String,
        val remainingSeconds: Int,
        val isExpired: Boolean,
        val isWin: Boolean,
        val formattedTime: String
    )

    fun getDynamicState(signal: SignalResponse): DynamicSignalState {
        val nowSeconds = System.currentTimeMillis() / 1000
        val state = GlobalSignalManager.getCycleState(nowSeconds)
        
        if (state.isSearching) {
            return DynamicSignalState(
                direction = signal.direction,
                price = signal.price,
                duration = signal.duration,
                confidence = signal.confidence,
                strength = signal.strength,
                remainingSeconds = 0,
                isExpired = true,
                isWin = false,
                formattedTime = "00:00"
            )
        }
        
        // Calculate remaining seconds for the active trade
        val remainingSeconds = state.remainingSeconds
        val isExpired = false
        
        // Deterministic win/loss for the cycle
        val rand = Random(state.cycleIndex)
        val isWin = rand.nextInt(0, 100) < signal.confidence

        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        val formattedTime = String.format("%02d:%02d", mins, secs)

        return DynamicSignalState(
            direction = signal.direction,
            price = signal.price,
            duration = signal.duration,
            confidence = signal.confidence,
            strength = signal.strength,
            remainingSeconds = remainingSeconds,
            isExpired = isExpired,
            isWin = isWin,
            formattedTime = formattedTime
        )
    }
}
