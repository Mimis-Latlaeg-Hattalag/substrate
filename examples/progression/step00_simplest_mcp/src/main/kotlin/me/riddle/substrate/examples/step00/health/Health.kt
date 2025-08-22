package me.riddle.substrate.examples.step00.health

import java.time.Duration
import java.time.Instant

/**
 * Simple health snapshot for Step 00.
 *
 * Two "versions" exist in our world:
 * - JSON-RPC version: always "2.0" (the envelope)
 * - MCP protocol date: negotiated at initialize (e.g., "2025-06-18")
 *
 * This Health model is transport-agnostic and can be returned by a tool.
 */
data class Health(
    val status: String,       // "ok" | "degraded" | "error"
    val version: String,      // app/library version (e.g., "0.0.1")
    val gitSha: String,       // short sha or "dev"
    val startedAt: Instant,   // server start timestamp
    val uptimeMs: Long        // millis since start
)

/**
 * Process-local health probe. Keep it tiny for now.
 * - startedAt is captured on first load.
 * - gitSha resolves from CI env if present, otherwise "dev".
 *
 * ToDo: remove supress when used.
 */
@Suppress("unused")
object HealthProbe {
    private val startedAt: Instant = Instant.now()

    private fun resolveGitSha(): String {
        // Common CI envs we may run under later
        val envSha = System.getenv("GITHUB_SHA")
            ?: System.getenv("GIT_COMMIT")
            ?: System.getenv("CI_COMMIT_SHA")
        return (envSha ?: "dev").take(12)
    }

    fun current(appVersion: String): Health {
        val now = Instant.now()
        val uptime = Duration.between(startedAt, now).toMillis()
        return Health(
            status = "ok",
            version = appVersion,
            gitSha = resolveGitSha(),
            startedAt = startedAt,
            uptimeMs = uptime
        )
    }
}