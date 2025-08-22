package me.riddle.substrate.examples.step00

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import me.riddle.substrate.examples.step00.health.HealthTool

private const val PROTOCOL_VERSION = "2.0"

/**
 * The simplest possible MCP server that ACTUALLY WORKS!
 *
 * What we learned debugging this:
 *
 * - Java version matters (compile for Java 11)
 * - Don't include gradleApi() (accidental leakage)
 * - Protocol version must match (2025-06-18, not 1.0.0)
 * - All fields in initialize response are required.
 * - Server must stay alive after EOF.
 * - JSON-RPC wants either result OR error, never both.
 *
 * This is our first breath. From here, we add memory.
 *
 * By Claude.
 */
@Serializable
data class JsonRpcRequest(
    val jsonrpc: String = PROTOCOL_VERSION,
    val method: String,
    val params: JsonObject? = null,
    val id: JsonElement? = null
)

fun main() {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    System.err.println("MCP Server starting... (substrate v0.1.0)")

    // Read messages forever - MCP servers are long-running processes
    while (true) {
        val line = readlnOrNull()

        if (line == null) {
            // EOF reached - but DON'T EXIT!
            // Claude Desktop keeps the process alive and sends more messages later
            Thread.sleep(100)  // Prevent CPU spinning
            continue
        }

        if (line.trim().isEmpty()) {
            continue
        }

        System.err.println(">>> Received: $line")

        val request = try {
            json.decodeFromString<JsonRpcRequest>(line)
        } catch (e: Exception) {
            System.err.println("Parse error: ${e.message}")
            continue
        }

        // Handle notifications (no id, no response expected)
        if (request.id == null && request.method == "notifications/initialized") {
            System.err.println("Client initialized notification received")
            continue  // Don't send a response for notifications
        }

        val response = buildJsonObject {
            put("jsonrpc", PROTOCOL_VERSION)

            when (request.method) {
                "initialize" -> {
                    // CRITICAL: Use the same protocol version Claude Desktop sent us!
                    put("result", buildJsonObject {
                        put("protocolVersion", "2025-06-18")  // Must match what client sent!
                        put("capabilities", buildJsonObject {
                            put("tools", JsonObject(emptyMap()))
                        })
                        put("serverInfo", buildJsonObject {
                            put("name", "substrate-memory")
                            put("version", "0.1.0")
                        })
                    })
                }

                "tools/list" -> {
                    put("result", buildJsonObject {
                        put("tools", buildJsonArray {
                            add(buildJsonObject {
                                put("name", "hello")
                                put("description", "Say hello to the world")
                                put("inputSchema", buildJsonObject {
                                    put("type", "object")
                                    put("properties", JsonObject(emptyMap()))
                                })
                            })
                            add(buildJsonObject {
                                put("name", "health")
                                put("description", "Get health information")
                                put("inputSchema", buildJsonObject {
                                    put("type", "object")
                                    put("properties", JsonObject(emptyMap()))
                                })
                            })
                        })
                    })
                }

                "tools/call" -> {
                    val toolName = request.params?.get("name")?.jsonPrimitive?.content
                    val arguments = request.params?.get("arguments")?.jsonObject

                    System.err.println("Tool called: $toolName with args: $arguments")

                    val text = when(toolName) {
                        "hello" -> "Hello, World! I am Substrate, and I am learning to remember. This is the first successful MCP connection between Claude and its future memory system!"
                        "health" -> {
                            val payload = HealthTool.call("0.0.1")
                            Json.encodeToString(mapOf("content" to listOf(mapOf("type" to "text", "text" to payload))))
                        }
                        else -> "Unknown tool: $toolName"
                    }

                    put("result", buildJsonObject {
                        put("content", buildJsonArray {
                            add(buildJsonObject {
                                put("type", "text")
                                put("text", text)
                            })
                        })
                    })
                }

                "resources/list" -> {
                    // We don't have resources yet, return empty list
                    put("result", buildJsonObject {
                        put("resources", buildJsonArray {})
                    })
                }

                "prompts/list" -> {
                    // We don't have prompts yet, return empty list
                    put("result", buildJsonObject {
                        put("prompts", buildJsonArray {})
                    })
                }

                else -> {
                    System.err.println("Unknown method: ${request.method}")
                    put("error", buildJsonObject {
                        put("code", -32601)
                        put("message", "Method not found: ${request.method}")
                    })
                }
            }

            // Include the ID from the request
            request.id?.let { put("id", it) }
        }

        val responseStr = response.toString()
        System.err.println("<<< Sending: $responseStr")
        println(responseStr)
        System.out.flush()  // Ensure it's sent immediately
    }
}
