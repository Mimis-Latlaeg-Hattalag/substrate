package me.riddle.substrate.examples.step01

import io.github.oshai.kotlinlogging.KotlinLogging

/*
IMPORTANT:
- MCP Schema: https://github.com/modelcontextprotocol/modelcontextprotocol/blob/main/schema/2025-06-18/schema.json
- MCP Protocol Version: 2025-06-18 - https://modelcontextprotocol.io/specification/2025-06-18
 */


private val appLogger by lazy { KotlinLogging.logger { } }


fun main() {
    appLogger.info { "Simple Memory" }
}
