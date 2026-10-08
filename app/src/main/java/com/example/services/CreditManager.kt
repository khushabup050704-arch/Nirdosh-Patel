package com.example.services

import com.example.models.DurationOption

object CreditManager {

    data class CreditPackage(
        val id: String,
        val credits: Int,
        val priceUsd: String,
        val tag: String? = null,
        val isPopular: Boolean = false
    )

    val packages = listOf(
        CreditPackage("starter", 50, "$4.99", "Starter"),
        CreditPackage("pro", 200, "$14.99", "Most Popular", isPopular = true),
        CreditPackage("studio", 600, "$34.99", "Best Value"),
        CreditPackage("enterprise", 1500, "$79.99", "Ultimate")
    )

    fun calculateRequiredCredits(duration: DurationOption): Int {
        return duration.creditsRequired
    }
}
