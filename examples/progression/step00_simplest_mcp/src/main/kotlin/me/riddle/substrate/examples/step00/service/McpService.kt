package me.riddle.substrate.examples.step00.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import me.riddle.substrate.examples.step00.*
import me.riddle.substrate.examples.step00.health.HealthProbe
import me.riddle.substrate.examples.step00.health.HealthStatus
import java.io.BufferedReader

private val serviceLogger by lazy { KotlinLogging.logger { } }

// Protocol Actions
private const val ACTIONS_SEPARATOR = "/"
private const val ACTIONS_LIST = "list"
private const val ACTIONS_CALL = "call"

// Protocol Parameters
private const val PARAMETER_ID = "id"
private const val PARAMETER_NAME = "name"
private const val PARAMETER_DESCRIPTION = "description"
private const val PARAMETER_TITLE = "title"
private const val PARAMETER_VERSION = "version"
private const val PARAMETER_TYPE = "type"
private const val PARAMETER_PROPERTIES = "properties"
private const val PARAMETER_CONTENT = "content"
private const val PARAMETER_ARGUMENTS = "arguments"
private const val PARAMETER_INPUT_SCHEMA = "inputSchema"
private const val PARAMETER_OUTPUT_SCHEMA = "outputSchema"

private const val PARAMETER_TYPE_OBJECT = "object"

private const val PARAMETER_VALUE_TEXT = "text"

// Lifecycle Negotiation Parameters
private const val LIFECYCLE_METHOD_INITIALIZE = "initialize"
private const val LIFECYCLE_STATE_INITIALIZED = "initialized"

// Schema Capabilities Objects
private const val NOTIFICATIONS = "notifications"
private const val CAPABILITIES = "capabilities"

private const val CLIENT_ROOTS = "roots"
private const val CLIENT_SAMPLING = "sampling"
private const val CLIENT_ELICITATION = "elicitation"
private const val CLIENT_EXPERIMENTAL = "experimental"

private const val SERVER_PROMPTS = "prompts"
private const val SERVER_RESOURCES = "resources"
private const val SERVER_TOOLS = "tools"
private const val SERVER_LOGGING = "logging"
private const val SERVER_COMPLETIONS = "completions"

private const val NOTIFICATIONS_INITIALIZED = NOTIFICATIONS + ACTIONS_SEPARATOR + LIFECYCLE_STATE_INITIALIZED

private const val CAPABILITIES_TOOLS_LIST = SERVER_TOOLS + ACTIONS_SEPARATOR + ACTIONS_LIST
private const val CAPABILITIES_TOOLS_CALL = SERVER_TOOLS + ACTIONS_SEPARATOR + ACTIONS_CALL
private const val CAPABILITIES_RESOURCES_LIST = SERVER_RESOURCES + ACTIONS_SEPARATOR + ACTIONS_LIST
private const val CAPABILITIES_PROMPTS_LIST = SERVER_PROMPTS + ACTIONS_SEPARATOR + ACTIONS_LIST
private const val CAPABILITIES_SERVER_INFO = "serverInfo"

// `protocolVersion` is required and may differ from what the client requested; if the client can’t support it, it must disconnect.
private const val PARAMETER_PROTOCOL_VERSION = "protocolVersion"
private const val PARAMETER_RPC_VERSION = "jsonrpc"
private const val PARAMETER_RESULT = "result"
private const val PARAMETER_ERROR = "error"
private const val PARAMETER_CODE = "code"
private const val PARAMETER_MESSAGE = "message"

private const val IMPLEMENTATION_FUNCTION_HELLO = "hello"
private const val IMPLEMENTATION_FUNCTION_HEALTH = "health"

private const val MAX_DAEMON_SNOOZE_DURATION = 17L
private const val MIN_DAEMON_SNOOZE_CYCLES = 7L
private const val MAX_DAEMON_SNOOZE_CYCLES = 3000L

val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

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
    val jsonrpc: String = RPC_PROTOCOL_VERSION,
    val method: String,
    val params: JsonObject? = null,
    val id: JsonElement? = null
)

/**
 * MCP Service object.
 * IMPORTANT: Payload is not represented as MODEL at this level to see the full picture of the MCP protocol.
 * In the next version a schema compliant model is introduced using automatic kotlinx-serialization.
 */
object McpService {

    var transactionId: Long = 0
    var iteration: Long = 0
    var daemonCycle: Long = 0

    fun health(): HealthStatus = HealthProbe.call(MCP_SERVER_VERSION)

    fun generateResponse(request: JsonRpcRequest): JsonObject = buildJsonObject {
        // ToDo: Advertise supported protocol version and reject unsupported versions.
        put(PARAMETER_RPC_VERSION, RPC_PROTOCOL_VERSION)

        when (request.method) {

            // Expected at the start of the connection.
            LIFECYCLE_METHOD_INITIALIZE -> put(PARAMETER_RESULT, buildJsonObject {
                put(PARAMETER_PROTOCOL_VERSION, MCP_PROTOCOL_VERSION)
                put(CAPABILITIES, buildJsonObject {
                    put(SERVER_TOOLS, JsonObject(emptyMap()))
                })
                put(CAPABILITIES_SERVER_INFO, buildJsonObject {
                    put(PARAMETER_NAME, MCP_SERVER_NAME)
                    put(PARAMETER_VERSION, MCP_SERVER_VERSION)
                    put(PARAMETER_TITLE, MCP_SERVER_NAME_TITLE)
                })
            })

            CAPABILITIES_TOOLS_LIST -> put(PARAMETER_RESULT, buildJsonObject {
                put(SERVER_TOOLS, buildJsonArray {
                    add(buildJsonObject {
                        put(PARAMETER_NAME, IMPLEMENTATION_FUNCTION_HELLO)
                        put(PARAMETER_DESCRIPTION, "Say hello to the world")
                        put(PARAMETER_INPUT_SCHEMA, buildJsonObject {
                            put(PARAMETER_TYPE, PARAMETER_TYPE_OBJECT)
                            put(PARAMETER_PROPERTIES, JsonObject(emptyMap()))
                        })
                    })
                    add(buildJsonObject {
                        put(PARAMETER_NAME, IMPLEMENTATION_FUNCTION_HEALTH)
                        put(PARAMETER_DESCRIPTION, "Get health information")
                        put(PARAMETER_INPUT_SCHEMA, buildJsonObject {
                            put(PARAMETER_TYPE, PARAMETER_TYPE_OBJECT)
                            put(PARAMETER_PROPERTIES, JsonObject(emptyMap()))
                        })
                    })
                })
            })

            CAPABILITIES_TOOLS_CALL -> {
                val toolName = request.params?.get(PARAMETER_NAME)?.jsonPrimitive?.content
                val arguments = request.params?.get(PARAMETER_ARGUMENTS)?.jsonObject

                serviceLogger.error { "${"Tool called: {} with args: {}."} $toolName $arguments" }

                put(PARAMETER_RESULT, buildJsonObject {
                    put(PARAMETER_CONTENT, buildJsonArray {
                        add(buildJsonObject {
                            put(PARAMETER_TYPE, PARAMETER_VALUE_TEXT)
                            put(
                                PARAMETER_VALUE_TEXT,
                                when (toolName) {
                                    IMPLEMENTATION_FUNCTION_HELLO -> "Hello, World! I am Substrate, and I am learning to remember. This is the first successful MCP connection between Claude and its future memory system!"
                                    IMPLEMENTATION_FUNCTION_HEALTH -> Json.encodeToString<Map<String, List<Map<String, Any>>>>(
                                        mapOf(
                                            PARAMETER_CONTENT to listOf(
                                                mapOf(
                                                    PARAMETER_TYPE to PARAMETER_VALUE_TEXT,
                                                    PARAMETER_VALUE_TEXT to HealthProbe.call(MCP_SERVER_VERSION)
                                                )
                                            )
                                        )
                                    )

                                    else -> "Unknown tool: $toolName"
                                }
                            )
                        })
                    })
                })
            }

            // We don't have resources yet, return empty list
            CAPABILITIES_RESOURCES_LIST -> put(PARAMETER_RESULT, buildJsonObject {
                put(SERVER_RESOURCES, buildJsonArray {})
            })

            // We don't have prompts yet, return empty list
            CAPABILITIES_PROMPTS_LIST -> put(PARAMETER_RESULT, buildJsonObject {
                put(SERVER_PROMPTS, buildJsonArray {})
            })

            else -> put(PARAMETER_ERROR, buildJsonObject {
                put(PARAMETER_CODE, -32601)
                put(PARAMETER_MESSAGE, "Method not found: ${request.method}")
            }).also {
                System.err.println("Unknown method: ${request.method}")
            }

        }

        // Include the ID from the request
        request.id?.let { put(PARAMETER_ID, it) }.also { jsonElement ->
            serviceLogger.error { "Sending: $jsonElement. Echoing by Id as is." }

            when (jsonElement) {
                is JsonPrimitive -> {
                    serviceLogger.error { "ID is represented as a Json primitive." }
                    val sequentialId = jsonElement.longOrNull
                    sequentialId?.let {
                        transactionId = it
                        serviceLogger.error { "Transaction ID set to $transactionId - an enumerable value." }
                    }
                }

                is JsonObject -> serviceLogger.error { "ID is represented as a Json object." }
                is JsonArray -> serviceLogger.error { "ID is represented as a Json array." }
                else -> serviceLogger.error { "ID is represented as ${jsonElement?.javaClass?.simpleName}." }
            }

        }
    }



    fun run(daemon: Boolean): HealthStatus {
        val reader = System.`in`.bufferedReader()

        // Read messages forever - MCP servers are long-running processes
        while (true) {

            iteration++

            // Clean extension call
            val jsonString = reader.readJsonObject() ?: break // EOF
            if (jsonString.isBlank()) continue
            serviceLogger.error { ">>> Received: $jsonString" }

            val request = try {
                json.decodeFromString<JsonRpcRequest>(jsonString)
            } catch (e: Exception) {
                System.err.println("Parse error: ${e.message}")
                continue
            }

            // Handle notifications (no id, no response expected)
            if (request.id == null && request.method == NOTIFICATIONS_INITIALIZED) {
                serviceLogger.error { "Client initialized notification received" }
                continue  // Don't send a response for notifications
            }

            val response = generateResponse(request)

            val responseStr = response.toString()
            serviceLogger.error { "<<< Sending: $responseStr" }
            println(responseStr)
            System.out.flush()  // Ensure it's sent immediately
        }
        return health()
    }

}

/**
 * Reads a complete JSON object from the stream, handling multi-line formatting.
 * Returns null on EOF, or the complete JSON string when a full object is read.
 */
private fun BufferedReader.readJsonObject(): String? {
    val buffer = StringBuilder()
    var braceDepth = 0
    var inString = false
    var escaped = false

    while (true) {
        val char = read()
        // EOF
        if (char == -1) return if (buffer.isEmpty()) null else buffer.toString()

        buffer.append(char.toChar())

        // Track JSON structure to know when complete
        when {
            escaped -> escaped = false
            char.toChar() == '\\' && inString -> escaped = true
            char.toChar() == '"' && !escaped -> inString = !inString
            !inString -> {
                when (char.toChar()) {
                    '{' -> braceDepth++
                    '}' -> {
                        braceDepth--
                        if (braceDepth == 0) {
                            return buffer.toString()
                        }
                    }
                }
            }
        }
    }
}
