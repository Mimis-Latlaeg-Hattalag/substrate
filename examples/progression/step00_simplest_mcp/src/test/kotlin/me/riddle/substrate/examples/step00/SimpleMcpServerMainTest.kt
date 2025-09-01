package me.riddle.substrate.examples.step00

import io.github.oshai.kotlinlogging.KotlinLogging
import me.riddle.substrate.examples.step00.service.McpService
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.emptyArray
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test


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
    fun `Validate initialize response`() {
        System.setIn(ByteArrayInputStream(MCP_INITIALIZE_CONTENT.toByteArray()))
        McpService.run(false)
        val result = testOut.toString()

        mainTestLogger.info { result }
    }

}