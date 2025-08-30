@file:OptIn(ExperimentalTime::class)

package me.riddle.substrate.examples.step01.health

import io.github.oshai.kotlinlogging.KotlinLogging
import me.riddle.substrate.examples.step01.transport.Info.MCP_PROTOCOL_VERSION
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val currentVersion = "0.1.0"

private val healthLogger by lazy { KotlinLogging.logger { } }

/**
 * Simple health snapshot for Step 00.
 *
 * Two "versions" exist in our world:
 * - JSON-RPC version: always "2.0" (the envelope)
 * - MCP protocol date: negotiated at initialize (e.g., "2025-06-18")
 *
 * This Health model is transport-agnostic and can be returned by a tool.
 */
data class HealthStatus(
    val status: String,             // "ok" | "degraded" | "error"
    val version: String,            // app/library version (e.g., "0.0.1")
    val protocolVersion: String = MCP_PROTOCOL_VERSION,
    val gitSha: String,             // short sha or "dev"
    val gitBranch: String,          // git branch or "N/A"
    val gitRoot: File,              // git root or "N/A"
    val startedAt: Instant,         // server start timestamp
    val upTime: Duration            // millis since start
)

/**
 * Minimal "health" tool for Step 00.
 * Returns a compact Health status object.
 */
object HealthProbe {

    const val NAME: String = "health"

    private val startedAt: Instant = Clock.System.now().also { healthLogger.info("HealthProbe started at $it") }

    private fun gitSha(): String = "git rev-parse --short HEAD".executeCommand() ?: "N/A"

    private fun gitBranch(): String = "git rev-parse --abbrev-ref HEAD".executeCommand() ?: "N/A"

    private fun gitRoot(): File = File("git rev-parse --show-toplevel".executeCommand() ?: ".").canonicalFile

    fun call(version: String = currentVersion): HealthStatus {
        return HealthStatus(
            status = "ok",  //FixMe: Implement!
            version = "$NAME-v$version",
            gitSha = gitSha(),
            gitBranch = gitBranch(),
            gitRoot = gitRoot(),
            startedAt = startedAt,
            upTime = Clock.System.now() - startedAt
        )
    }
}

/**
 * Executes a system command represented by this String, optionally in a specified working directory.
 *
 * @param workingDir The directory in which to execute the command. Defaults to the current working directory if null.
 * @param timeoutSeconds The maximum time in seconds to wait for the command to complete. Defaults to 60 seconds.
 * @return The output of the command as a String, or null if an error occurs or the command times out.
 */
fun String.executeCommand(workingDir: File? = null, timeoutSeconds: Long = 60): String? = try {
    with(ProcessBuilder(*split("\\s".toRegex()).toTypedArray())) {
        workingDir?.let { directory(it) }
        redirectOutput(ProcessBuilder.Redirect.PIPE)
        redirectError(ProcessBuilder.Redirect.PIPE)
    }.start().apply {
        waitFor(timeoutSeconds, TimeUnit.SECONDS)
    }.let {
        when (it.exitValue()) {
            0 -> it.inputStream.bufferedReader().readText().trim()
            else -> System.err.println("Command execution failed: ${it.errorStream.bufferedReader().readText()}").let { null }
        }
    }
} catch (e: Exception) {
    System.err.println("Error executing command '$this': ${e.message}").let { null }
}