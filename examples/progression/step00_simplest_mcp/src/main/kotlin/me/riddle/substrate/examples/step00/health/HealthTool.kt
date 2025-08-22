package me.riddle.substrate.examples.step00.health

import java.time.Duration
import java.time.Instant

/**
 * Minimal "health" tool for Step 00.
 * Returns a compact JSON string as text content (no extra deps).
 */
object HealthTool {

//    FixMe: review this!
    @Suppress("unused")
    const val NAME: String = "health"

    private val startedAt: Instant = Instant.now()

    private fun gitSha(): String =
        (System.getenv("GITHUB_SHA")
            ?: System.getenv("GIT_COMMIT")
            ?: System.getenv("CI_COMMIT_SHA")
            ?: "dev").take(12)

    fun call(version: String = "0.0.1"): String {
        val uptimeMs = Duration.between(startedAt, Instant.now()).toMillis()
        // Keep the shape trivial for Step 00
        return """
               {"status":"ok","version":"$version","git_sha":"${gitSha()}","started_at":"$startedAt","uptime_ms":$uptimeMs}
               """.trimIndent()
    }
}