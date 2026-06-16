package com.example.util

object GlobalKeyManager {
    // Shared list of dynamically generated activation keys
    private val generatedKeys = mutableSetOf<String>()

    init {
        // Pre-populate with standard key(s) if we want
        generatedKeys.add("TURKI-WOLF-FREE")
    }

    fun addKey(key: String) {
        synchronized(generatedKeys) {
            generatedKeys.add(key.trim().uppercase())
        }
    }

    fun containsKey(key: String): Boolean {
        synchronized(generatedKeys) {
            return generatedKeys.contains(key.trim().uppercase())
        }
    }

    fun getAllKeys(): List<String> {
        synchronized(generatedKeys) {
            return generatedKeys.toList()
        }
    }

    fun removeKey(key: String) {
        synchronized(generatedKeys) {
            generatedKeys.remove(key.trim().uppercase())
        }
    }
}
