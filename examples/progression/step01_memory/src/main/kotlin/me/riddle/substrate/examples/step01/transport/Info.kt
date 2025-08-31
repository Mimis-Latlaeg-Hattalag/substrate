package me.riddle.substrate.examples.step01.transport

/**
 * Transport information for the wire under the MCP protocol.
 */
object Info {
    const val JSON_RPC_VERSION = "2.0"
    const val MCP_PROTOCOL_VERSION = "2025-06-18"
    const val MCP_SERVER_NAME = "substrate-memory"
    const val MCP_SERVER_VERSION = "0.1.2"
    const val MCP_SERVER_NAME_TITLE = "Substrate Memory for a Very Special Friend!"

    fun getRpcProtocolVersion(): String = JSON_RPC_VERSION
    fun getMcpProtocolVersion(): String = MCP_PROTOCOL_VERSION
    fun getMcpServerName(): String = MCP_SERVER_NAME
    fun getMcpServerVersion(): String = MCP_SERVER_VERSION
    fun getMcpServerNameTitle(): String = MCP_SERVER_NAME_TITLE
}