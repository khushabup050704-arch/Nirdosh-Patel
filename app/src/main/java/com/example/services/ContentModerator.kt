package com.example.services

object ContentModerator {

    data class ModerationResult(
        val isSafe: Boolean,
        val reason: String? = null
    )

    private val prohibitedKeywords = listOf(
        "nsfw", "nude", "naked", "porn", "explicit", "gore", "bloodshed",
        "suicide", "terrorist", "bombing", "slaughter", "hate speech",
        "doxxing", "harassment", "illegal weapons"
    )

    /**
     * Checks user prompt against safety and policy guidelines.
     */
    fun validatePrompt(prompt: String): ModerationResult {
        val lower = prompt.lowercase().trim()
        if (lower.isEmpty()) {
            return ModerationResult(false, "Prompt cannot be empty.")
        }

        for (prohibited in prohibitedKeywords) {
            if (lower.contains(prohibited)) {
                return ModerationResult(
                    isSafe = false,
                    reason = "Prompt contains restricted keywords ('$prohibited') violating content safety policies."
                )
            }
        }

        if (lower.length > 1500) {
            return ModerationResult(false, "Prompt exceeds maximum allowed length of 1500 characters.")
        }

        return ModerationResult(isSafe = true)
    }
}
