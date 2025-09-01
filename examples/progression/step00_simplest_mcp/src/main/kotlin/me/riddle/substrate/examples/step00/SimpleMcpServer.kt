package me.riddle.substrate.examples.step00

import io.github.oshai.kotlinlogging.KotlinLogging
import me.riddle.substrate.examples.step00.service.McpService

/*
IMPORTANT:
- MCP Schema: https://github.com/modelcontextprotocol/modelcontextprotocol/blob/main/schema/2025-06-18/schema.json
- MCP Protocol Version: 2025-06-18 - https://modelcontextprotocol.io/specification/2025-06-18
 */


const val RPC_PROTOCOL_VERSION = "2.0"
const val MCP_PROTOCOL_VERSION = "2025-06-18"
const val MCP_SERVER_NAME = "substrate-memory"
const val MCP_SERVER_VERSION = "0.1.1"
const val MCP_SERVER_NAME_TITLE = "Substrate Memory for a Very Special Friend!"

private val appLogger by lazy { KotlinLogging.logger { } }

fun main() {
    appLogger.error { "MCP Server starting... ($MCP_SERVER_NAME v$MCP_SERVER_VERSION) running MCP protocol $MCP_PROTOCOL_VERSION." }
    val health = McpService.health()
    McpService.run(true)
    if (health.status.lowercase() == "error") appLogger.error { health }
    appLogger.error { "MCP Server stopped."}
}
