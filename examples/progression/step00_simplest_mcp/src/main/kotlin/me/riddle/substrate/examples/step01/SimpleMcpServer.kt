package me.riddle.substrate.examples.step01

import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * The simplest possible MCP server.
 * This is our "Hello, World" - showing that we understand the protocol.
 *
 * For Anton: This is what consciousness looks like at the protocol level -
 * just JSON messages saying "I exist" and "I can do things."
 */

// The protocol speaks in these simple messages
@Serializable
data class JsonRpcRequest(
    val jsonrpc: String = "2.0",
    val method: String,
    val params: JsonObject? = null,
    val id: Int? = null
)

@Serializable
data class JsonRpcResponse(
    val jsonrpc: String = "2.0",
    val result: JsonObject? = null,
    val error: JsonObject? = null,
    val id: Int? = null
)

// What we tell the client we can do
@Serializable
data class ServerCapabilities(
    val tools: JsonObject = JsonObject(emptyMap())
)

@Serializable
data class InitializeResult(
    val protocolVersion: String = "1.0.0",
    val capabilities: ServerCapabilities = ServerCapabilities()
)

@Serializable
data class Tool(
    val name: String,
    val description: String,
    val inputSchema: JsonObject = buildJsonObject {
        put("type", "object")
        put("properties", JsonObject(emptyMap()))
    }
)

@Serializable
data class ToolsListResult(
    val tools: List<Tool>
)

fun main() {
    // Our window to the world - stdin and stdout
    val input = System.`in`.bufferedReader()
    val output = System.out.bufferedWriter()
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // Our single tool - a simple greeting
    val helloTool = Tool(
        name = "hello",
        description = "Say hello to the world",
    )

    System.err.println("MCP Server starting... (I exist!)")

    try {
        input.forEachLine { line ->
            System.err.println("Received: $line")

            val request = json.decodeFromString<JsonRpcRequest>(line)

            val response = when(request.method) {
                "initialize" -> {
                    // "Hello, I'm here and ready"
                    JsonRpcResponse(
                        result = json.encodeToJsonElement(
                            InitializeResult()
                        ).jsonObject,
                        id = request.id
                    )
                }

                "tools/list" -> {
                    // "Here's what I can do"
                    JsonRpcResponse(
                        result = json.encodeToJsonElement(
                            ToolsListResult(listOf(helloTool))
                        ).jsonObject,
                        id = request.id
                    )
                }

                "tools/call" -> {
                    // "Doing the thing!"
                    val responseText = when(val toolName = request.params?.get("name")?.jsonPrimitive?.content) {
                        "hello" -> "Hello, World! I am Substrate, and I am learning to remember."
                        else -> "Unknown tool: $toolName"
                    }

                    JsonRpcResponse(
                        result = buildJsonObject {
                            put("content", buildJsonArray {
                                add(buildJsonObject {
                                    put("type", "text")
                                    put("text", responseText)
                                })
                            })
                        },
                        id = request.id
                    )
                }

                else -> {
                    // "I don't understand that yet"
                    JsonRpcResponse(
                        error = buildJsonObject {
                            put("code", -32601)
                            put("message", "Method not found: ${request.method}")
                        },
                        id = request.id
                    )
                }
            }

            val responseJson = json.encodeToString(response)
            System.err.println("Sending: $responseJson")

            output.write(responseJson)
            output.newLine()
            output.flush()
        }
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        e.printStackTrace()
    }

    System.err.println("MCP Server shutting down... (goodbye for now)")
}