package me.riddle.substrate.examples.step00

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import me.riddle.substrate.examples.step00.service.McpService
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.fail


const val MCP_INITIALIZE_CONTENT = """
    {
      "jsonrpc": "2.0", 
      "method": "initialize",
      "params": {
        "protocolVersion": "2025-06-18",
        "capabilities": {
          "roots": {
            "listChanged": true
          },
          "sampling": {}
        },
        "clientInfo": {
          "name": "claude-desktop",
          "version": "1.0.0"
        }
      },
      "id": 1
    }
    """

const val MCP_INITIALIZE_RESPONSE = """
    {
      "jsonrpc": "2.0",
      "result": {
        "capabilities": {
          "roots": {
            "listChanged": true
          },
          "sampling": {}
        }
      },
      "id": 1
    }
    """

class SimpleMcpServerMainTest {

    private val mainTestLogger by lazy { KotlinLogging.logger { } }
    private val json = Json { ignoreUnknownKeys = true }

    private val testCapture = ByteArrayOutputStream()
    private val testOut = ByteArrayOutputStream()
    private val testErr = ByteArrayOutputStream()
    private val systemIn = System.`in`
    private val systemOut = System.out
    private val systemErr = System.err

    @BeforeTest
    fun setUp() {
        System.setOut(PrintStream(testOut))
        System.setErr(PrintStream(testErr))
        mainTestLogger.info { "Test main streams set up." }
    }

    @AfterTest
    fun tearDown() {
        System.setOut(systemOut)
        System.setErr(systemErr)
        mainTestLogger.info { "Test main streams torn down." }
    }

    @Test
    fun `server starts and stops cleanly on EOF`() {
        // Simulate empty input (immediate EOF)
        val emptyInput = ByteArrayInputStream(ByteArray(0))

        System.setIn(emptyInput)
        System.setOut(PrintStream(testOut))
        System.setErr(PrintStream(testErr))

        // Server should exit cleanly, not hang
        val future = Executors.newSingleThreadExecutor().submit {
            McpService.run(false) // daemon=false
        }

        try {
            future.get(2, TimeUnit.MINUTES) // Should complete quickly
        } catch (e: TimeoutException) {
            fail("Server did not exit cleanly on EOF - hanging instead", e)
        }

        mainTestLogger.info { "Server exited cleanly on EOF" }
    }

}