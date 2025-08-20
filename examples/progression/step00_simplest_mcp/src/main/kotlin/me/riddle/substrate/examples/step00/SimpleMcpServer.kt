package me.riddle.substrate.examples.step00

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class JsonRpcRequest(
    val jsonrpc: String = "2.0",
    val method: String,
    val params: JsonObject? = null,
    val id: JsonElement? = null
)

fun main() {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true  // Include default values!
    }

    System.err.println("MCP Server starting...")

    // Read messages forever
    while (true) {
        val line = readlnOrNull()

        if (line == null) {
            // EOF - keep waiting
            Thread.sleep(100)
            continue
        }

        if (line.trim().isEmpty()) {
            continue
        }

        System.err.println(">>> $line")

        val request = try {
            json.decodeFromString<JsonRpcRequest>(line)
        } catch (e: Exception) {
            System.err.println("Parse error: ${e.message}")
            continue
        }

        val response = buildJsonObject {
            put("jsonrpc", "2.0")

            when (request.method) {
                "initialize" -> {
                    // Build the result manually to ensure all fields are present
                    put("result", buildJsonObject {
                        put("protocolVersion", "1.0.0")
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
                        })
                    })
                }

                "tools/call" -> {
                    val toolName = request.params?.get("name")?.jsonPrimitive?.content
                    val text = if (toolName == "hello") {
                        "Hello, World! I am Substrate, and I am learning to remember."
                    } else {
                        "Unknown tool: $toolName"
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

                else -> {
                    put("error", buildJsonObject {
                        put("code", -32601)
                        put("message", "Unknown method: ${request.method}")
                    })
                }
            }

            request.id?.let { put("id", it) }
        }

        val responseStr = response.toString()
        System.err.println("<<< $responseStr")
        println(responseStr)
        System.out.flush()
    }
}