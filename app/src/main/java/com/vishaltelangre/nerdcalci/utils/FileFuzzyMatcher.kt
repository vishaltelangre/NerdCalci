package com.vishaltelangre.nerdcalci.utils

data class FileFuzzyMatchResult(
    val score: Int,
    val matchedIndices: List<Int>
)

object FileFuzzyMatcher {
    fun fuzzyMatch(text: String, query: String): FileFuzzyMatchResult? {
        if (query.isEmpty()) return FileFuzzyMatchResult(0, emptyList())

        val matchedIndices = mutableListOf<Int>()
        var queryIdx = 0
        var textIdx = 0

        while (queryIdx < query.length && textIdx < text.length) {
            if (text[textIdx].equals(query[queryIdx], ignoreCase = true)) {
                matchedIndices.add(textIdx)
                queryIdx++
            }
            textIdx++
        }

        if (queryIdx < query.length) return null

        // Calculate score
        var score = 0

        // Exact match (ignore case)
        if (text.equals(query, ignoreCase = true)) {
            score += 1000
        }

        // Starts with
        if (text.startsWith(query, ignoreCase = true)) {
            score += 500
        }

        // Substring match
        if (text.contains(query, ignoreCase = true)) {
            score += 250
        }

        // Bonus for consecutive characters
        var consecutiveCount = 0
        for (i in 1 until matchedIndices.size) {
            if (matchedIndices[i] == matchedIndices[i-1] + 1) {
                consecutiveCount++
            }
        }
        score += consecutiveCount * 10

        // Penalty for distance
        if (matchedIndices.isNotEmpty()) {
            val distance = matchedIndices.last() - matchedIndices.first() + 1
            score -= distance
        }

        return FileFuzzyMatchResult(score, matchedIndices)
    }
}
