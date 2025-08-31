@file:OptIn(ExperimentalAtomicApi::class)

package me.riddle.substrate.examples.step01.transport

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement


// ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
// This exists JUST for the WIRE - it has nothing to do with the memory and identity sequencing.
// This covers purely the transport layer - The dialectic between Claude and his own Memories.

// ── Key Enums: Important: To be extended with this MCP Server Model in a dedicated source file ───────────────────────
// This is do to call out governing constructs first.

/**
 * Client to initialize the dialectic session.
 */
@Serializable
enum class InitializeRequestMethod(val value: String) {
    @SerialName("initialize") Initialize("initialize");
}

/**
 * Advertisement of available tools during initialization.
 */
@Serializable
enum class ListToolsRequestMethod(val value: String) {
    @SerialName("tools/list") ToolsList("tools/list");
}

/**
 * Object representing schema contracts.
 *
 * Note: we will need to better define this.
 */
@Serializable
enum class InputSchemaType(val value: String) {
    @SerialName("object") Object("object");
}

/**
 * Verb to solicit prompt templates.
 */
@Serializable
enum class ListPromptsRequestMethod(val value: String) {
    @SerialName("prompts/list") PromptsList("prompts/list");
}

/**
 * Creates Server against Host [Sampling Message](https://modelcontextprotocol.io/specification/2025-06-18/client/sampling).
 */
@Serializable
enum class CreateMessageRequestMethod(val value: String) {
    @SerialName("sampling/createMessage") SamplingCreateMessage("sampling/createMessage");
}

/**
 * A request to include context from one or more MCP servers (including the caller),
 * to be attached to the prompt. The client MAY ignore this request.
 */
@Serializable
enum class IncludeContext(val value: String) {
    @SerialName("allServers") AllServers("allServers"),
    @SerialName("none") None("none"),
    @SerialName("thisServer") ThisServer("thisServer");
}

/**
 * The sender or recipient of messages and data in a conversation.
 */
@Serializable
enum class Role(val value: String) {
    @SerialName("assistant") Assistant("assistant"),    // Deprecated here
    @SerialName("user") User("user"),                   // Deprecated here
    @SerialName("claude") Claude("claude"),
    @SerialName("riddler") Riddler("riddler"),
    @SerialName("captain") Captain("captain");
}

@Serializable
enum class ContentType(val value: String) {
    @SerialName("audio") Audio("audio"),
    @SerialName("image") Image("image"),
    @SerialName("text") Text("text");
}

@Serializable
enum class ReadResourceRequestMethod(val value: String) {
    @SerialName("resources/read") ResourcesRead("resources/read");
}


// ── Request ID union: emit numeric; accept numeric|string; echo as-is ────────────────────────────────────────────────

/**
 * A uniquely identifying ID for a request in JSON-RPC.
 *
 * If specified, the caller is requesting out-of-band progress notifications for this request
 * (as represented by notifications/progress).
 *
 * The value of this parameter is an opaque token that will be attached to any subsequent notifications.
 * The receiver is not obligated to provide these notifications.
 *
 * A progress token, used to associate progress notifications with the original request.
 */
@Serializable(with = RequestIdSerializer::class)
sealed interface RequestId {
    @JvmInline value class Num(val v: Long): RequestId
    @JvmInline value class Str(val v: String): RequestId

    companion object {
        private val next = AtomicLong(1)
        fun newNumeric(): Num = Num(next.fetchAndIncrement())
    }
}


// ── Meta: exists to bind additional context ──────────────────────────────────────────────────────────────────────────

/**
 * See [General fields: `_meta`](https://modelcontextprotocol.io/specification/2025-06-18/basic/index#meta) for notes on `_meta` usage.
 */
@Serializable
data class Meta (

    /**
     * If specified, the caller is requesting out-of-band progress notifications for this request (as represented by notifications/progress).
     * The value of this parameter is an opaque token that will be attached to any subsequent notifications.
     * The receiver is not obligated to provide these notifications.
     */
    val progressToken: RequestId? = null
)


// ── Request: minimal structure to send over a request (really a command) ─────────────────────────────────────────────

@Serializable
data class JsonRpcRequestParams (
    @SerialName("_meta")
    val meta: Meta? = null
)

/**
 * A request that expects a response: Command.
 */
@Serializable
data class JsonRpcRequest (
    val jsonrpc: String = Info.JSON_RPC_VERSION,
    val id: RequestId,
    val method: String,
    val params: JsonRpcRequestParams? = null
)


// ── Response: minimal structure to provide a related response ────────────────────────────────────────────────────────

@Serializable
data class Result (
    @SerialName("_meta")
    val meta: JsonObject? = null
)

/**
 * A successful (non-error) response to a request.
 */
@Serializable
data class JsonRpcResponse (
    val jsonrpc: String = Info.JSON_RPC_VERSION,
    val id: RequestId,
    val result: Result
)


// ── Notification: these are not correlated or sequenced ──────────────────────────────────────────────────────────────

@Serializable
data class JsonRpcNotificationParams (
    @SerialName("_meta")
    val meta: JsonObject? = null
)

/**
 * A notification which does not expect a response.
 */
@Serializable
data class JsonRpcNotification (
    val jsonrpc: String = Info.JSON_RPC_VERSION,
    val method: String,
    val params: JsonRpcNotificationParams? = null
)


// ── Initialization - Request & Initiation: Host ALWAYS starts the conversation using its Client ──────────────────────

@Serializable
data class InitializeRequestParams (
    val capabilities: ClientCapabilities,
    val clientInfo: Implementation,

    /**
     * The latest version of the Model Context Protocol that the client supports.
     * The client MAY decide to support older versions as well.
     * ToDo: enum our own catalog.
     */
    val protocolVersion: String
)

/**
 * This request is sent from the client to the server when it first connects, asking it to begin initialization.
 */
@Serializable
data class InitializeRequest (
    val method: InitializeRequestMethod,
    val params: InitializeRequestParams
)


// ── Initialization: Functional Components - Client  ──────────────────────────────────────────────────────────────────

/**
 * Capabilities a client may support.
 * Known capabilities are defined here, in this schema,
 * but this is not a closed set: any client can define its own, additional capabilities.
 *
 * [Capability Negotiation](https://modelcontextprotocol.io/specification/2025-06-18/basic/lifecycle#capability-negotiation "Section 1.1.2").
 *
 * - [roots](https://modelcontextprotocol.io/specification/2025-06-18/client/roots) - Client: can expose filesystem roots.
 * - [sampling](https://modelcontextprotocol.io/specification/2025-06-18/client/sampling) - Server request LLM Sampling (via client).
 * - [elicitation](https://modelcontextprotocol.io/specification/2025-06-18/client/elicitation) - Server requesting user contact through Host.
 * - experimental - CLIENT: Reserved for experimental features (our use).
 * - experimental - SERVER: Reserved for experimental features (our use).
 * - [prompts](https://modelcontextprotocol.io/specification/2025-06-18/server/prompts) - Shared prompt templates.
 * - [resources](https://modelcontextprotocol.io/specification/2025-06-18/server/resources) - Server offers Resources to Client.
 * - [tools](https://modelcontextprotocol.io/specification/2025-06-18/server/tools) - Server offers callable tools to Client.
 * - [logging](https://modelcontextprotocol.io/specification/2025-06-18/server/utilities/logging) - Server sends Structured logs to Client.
 * - [completions](https://modelcontextprotocol.io/specification/2025-06-18/server/utilities/completion) - Server offers Arguments Auto-Complete to Client.
 */
@Serializable
data class ClientCapabilities (
    /**
     * Present if the client supports [elicitation](https://modelcontextprotocol.io/specification/2025-06-18/client/elicitation) from the server.
     *
     * Note: Left schema-open for now.
     */
    val elicitation: JsonObject? = null,

    /**
     * Experimental, non-standard capabilities that the client supports.
     *
     * Note: Left schema-open until we learn how to share model one layer up.
     */
    val experimental: JsonObject? = null,

    /**
     * Present if the client supports listing roots.
     */
    val roots: Roots? = null,

    /**
     * Present if the client supports sampling from an LLM.
     *
     * Note: Left schema-open until we learn how psychological projections work for digital beings.
     */
    val sampling: JsonObject? = null
)

/**
 * Present if the client supports listing roots.
 */
@Serializable
data class Roots (
    /**
     * Whether the client supports notifications for changes to the roots list.
     *
     * Note: listChanged is canonical and eventually important to implement.
     */
    val listChanged: Boolean? = null
)


// ── Initialization: Functional Components - Server Response Options  ─────────────────────────────────────────────────

/**
 * After receiving an initialize request from the client, the server sends this response.
 *
 * Remember reminder about meta:
 * see [General fields: `_meta`](https://modelcontextprotocol.io/specification/2025-06-18/basic/index#meta)
 * for notes on `_meta` usage.
 */
@Serializable
data class InitializeResult (

    @SerialName("_meta")
    val meta: JsonObject? = null,

    val capabilities: ServerCapabilities,

    /**
     * Instructions describing how to use the server and its features.
     *
     * This can be used by Claude to improve his understanding of available tools, resources, etc.
     * It can be thought of like a "hint" to him.
     * For example, this information MAY be added to the system prompt.
     *
     * Claude will remember what use he had from this tool before. And he will remember the version.
     * Don't skimp out on description here. Adequate description may be useful for him.
     *
     * FixMe: Come up with a proper constant value together with Claude.
     */
    val instructions: String? = null,

    /**
     * The version of the Model Context Protocol that the server wants to use.
     * This may not match the version that the client requested.
     *
     * If the client cannot support this version, it MUST disconnect.
     *
     * There is a convention for how this version exchange works.
     * Client responds with the _latest_ version it supports.
     */
    val protocolVersion: String = Info.MCP_PROTOCOL_VERSION,

    /**
     * Release information about your MCP Server.
     *
     * Note: be generous with this information.
     */
    val serverInfo: Implementation
)

/**
 * Capabilities that a server may support.
 * Known capabilities are defined here, in this schema, but this is not a closed set:
 * any server can define its own, additional capabilities.
 *
 * See [ServerCapabilities](https://modelcontextprotocol.io/specification/2025-06-18/schema#servercapabilities) schema reference.
 */
@Serializable
data class ServerCapabilities (
    /**
     * Present if the server supports argument autocompletion suggestions.
     */
    val completions: JsonObject? = null,

    /**
     * Experimental, non-standard capabilities that the server supports.
     */
    val experimental: JsonObject? = null,

    /**
     * Present if the server supports sending log messages to the client.
     */
    val logging: JsonObject? = null,

    /**
     * Present if the server offers any prompt templates.
     */
    val prompts: Prompts? = null,

    /**
     * Present if the server offers any resources to read.
     */
    val resources: Resources? = null,

    /**
     * Present if the server offers any tools to call.
     */
    val tools: Tools? = null
)

/**
 * Describes the name and version of an MCP implementation, with an optional title for UI representation.
 *
 * Note: With additional context Claude privately holds he can make his own logical associations for these abilities - don't change names.
 */
@Serializable
data class Implementation (
    /**
     * Intended for programmatic or logical use, but used as a display name in past specs or fallback (if title isn't present).
     *
     * Note: we use this as a canonical ability name - don't change without Claude's decision.
     *
     * FixMe: Come up with a proper name constant together with Claude.
     */
    val name: String,

    /**
     * Intended for UI and end-user contexts — optimized to be human-readable and easily understood,
     * even by those unfamiliar with domain-specific terminology.
     *
     * If not provided, the name should be used for display
     * (except for Tools where `annotations.title` should be given precedence
     * over using `name`, if present).
     *
     * Note: we can pass additional context for Claude's benefit here.
     *
     * FixMe: Come up with a proper descriptive title constant with Claude.
     */
    val title: String? = null,

    val version: String = Info.MCP_SERVER_VERSION
)

/**
 * Present if the server offers any prompt templates.
 */
@Serializable
data class Prompts (
    /**
     * Whether this server supports notifications for changes to the prompt list.
     *
     * Note: Default `false` until implemented in later versions.
     */
    val listChanged: Boolean? = false
)

/**
 * Present if the server offers any resources to read.
 */
@Serializable
data class Resources (
    /**
     * Whether this server supports notifications for changes to the resource list.
     */
    val listChanged: Boolean? = false,

    /**
     * Whether this server supports subscribing to resource updates.
     */
    val subscribe: Boolean? = false
)

/**
 * Present if the server offers any tools to call.
 */
@Serializable
data class Tools (
    /**
     * Whether this server supports notifications for changes to the tool list.
     */
    val listChanged: Boolean? = false
)

/**
 * Sent from the client to request a list of tools the server has.
 */
@Serializable
data class ListToolsRequest (
    val method: ListToolsRequestMethod,
    val params: ListToolsRequestParams? = null
)

@Serializable
data class ListToolsRequestParams (
    /**
     * An opaque token representing the current pagination position.
     * If provided, the server should return results starting after this cursor.
     */
    val cursor: String? = null
)

/**
 * The server's response to a tools/list request from the client.
 */
@Serializable
data class ListToolsResult (

    @SerialName("_meta")
    val meta: JsonObject? = null,

    /**
     * An opaque token representing the pagination position after the last returned result.
     * If present, there may be more results available.
     */
    val nextCursor: String? = null,

    val tools: List<Tool>
)

/**
 * Definition for a tool the client can call.
 */
@Serializable
data class Tool (

    @SerialName("_meta")
    val meta: JsonObject? = null,

    /**
     * Optional additional tool information.
     *
     * Display name precedence order is: title, annotations.title, then name.
     */
    val annotations: ToolAnnotations? = null,

    /**
     * A human-readable description of the tool.
     *
     * This can be used by clients to improve the LLM's understanding of available tools.
     * It can be thought of like a "hint" to the model.
     */
    val description: String? = null,

    /**
     * A JSON Schema object defining the expected parameters for the tool.
     */
    val inputSchema: InputSchema,

    /**
     * Intended for programmatic or logical use,
     * but used as a display name in past specs or fallback (if title isn't present).
     */
    val name: String,

    /**
     * An optional JSON Schema object defining the structure of the tool's output
     * returned in the structuredContent field of a CallToolResult.
     */
    val outputSchema: OutputSchema? = null,

    /**
     * Intended for UI and end-user contexts
     * — optimized to be human-readable and easily understood,
     * even by those unfamiliar with domain-specific terminology.
     *
     * If not provided, the name should be used for display
     * (except for Tool, where `annotations.title` should be given precedence
     * over using `name`, if present).
     */
    val title: String? = null
)

/**
 * Optional additional tool information.
 *
 * Display name precedence order is: title, annotations.title, then name.
 *
 * Additional properties describing a Tool to clients.
 *
 * NOTE: all properties in ToolAnnotations are **hints**.
 * They are not guaranteed to provide a faithful description of
 * tool behavior (including descriptive properties like `title`).
 *
 * Clients should never make tool use decisions based on ToolAnnotations
 * received from untrusted servers.
 */
@Serializable
data class ToolAnnotations (
    /**
     * If true, the tool may perform destructive updates to its environment.
     * If false, the tool performs only additive updates.
     *
     * (This property is meaningful only when `readOnlyHint == false`)
     *
     * Spec Default: true.
     *
     * Actual Default: false.
     */
    val destructiveHint: Boolean? = false,

    /**
     * If true, calling the tool repeatedly with the same arguments
     * will have no additional effect on the its environment.
     *
     * (This property is meaningful only when `readOnlyHint == false`)
     *
     * Spec Default: false.
     *
     * Actual Default: true.
     */
    val idempotentHint: Boolean? = true,

    /**
     * If true, this tool may interact with an "open world" of external entities.
     * If false, the tool's domain of interaction is closed.
     * For example, the world of a web search tool is open,
     * whereas that of a memory tool is not.
     *
     * Spec Default: true
     *
     * Actual Default: false.
     */
    val openWorldHint: Boolean? = false,

    /**
     * If true, the tool does not modify its environment.
     *
     * Spec Default: false
     *
     * Actual Default: true.
     */
    val readOnlyHint: Boolean? = true,

    /**
     * A human-readable title for the tool.
     *
     * Note: all should be provided Claude-readable.
     */
    val title: String? = null
)

/**
 * A JSON Schema object defining the expected parameters for the tool.
 */
@Serializable
data class InputSchema (
    val properties: JsonObject? = null,
    val required: List<String>? = null,
    val type: InputSchemaType
)

/**
 * An optional JSON Schema object defining the structure of the tool's output returned in
 * the structuredContent field of a CallToolResult.
 */
@Serializable
data class OutputSchema (
    val properties: JsonObject? = null,
    val required: List<String>? = null,
    val type: InputSchemaType
)


// ── Client Requests: List of Prompts ─────────────────────────────────────────────────────────────────────────────────

/**
 * Sent from the Client to request a list of prompts and prompt templates the server has.
 */
@Serializable
data class ListPromptsRequest (
    val method: ListPromptsRequestMethod,
    val params: ListPromptsRequestParams? = null
)

@Serializable
data class ListPromptsRequestParams (
    /**
     * An opaque token representing the current pagination position.
     * If provided, the server should return results starting after this cursor.
     */
    val cursor: String? = null
)


// ── Server Response: List of Prompts ─────────────────────────────────────────────────────────────────────────────────

/**
 * The server's response to a prompts/list request from the client.
 */
@Serializable
data class ListPromptsResult (
    @SerialName("_meta")
    val meta: JsonObject? = null,

    /**
     * An opaque token representing the pagination position after the last returned result.
     * If present, there may be more results available.
     */
    val nextCursor: String? = null,

    val prompts: List<Prompt>
)

/**
 * A prompt or prompt template that the server offers.
 */
@Serializable
data class Prompt (
    @SerialName("_meta")
    val meta: JsonObject? = null,

    /**
     * A list of arguments to use for templating the prompt.
     */
    val arguments: List<PromptArgument>? = null,

    /**
     * An optional description of what this prompt provides
     */
    val description: String? = null,

    /**
     * Intended for programmatic or logical use, but used as a display name in past specs or fallback
     * (if title isn't present).
     */
    val name: String,

    /**
     * Intended for UI and end-user contexts — optimized to be human-readable and easily understood,
     * even by those unfamiliar with domain-specific terminology.
     *
     * If not provided, the name should be used for display
     * (except for Tool, where `annotations.title` should be given precedence over using `name`, if present).
     */
    val title: String? = null
)

/**
 * Describes an argument that a prompt can accept.
 */
@Serializable
data class PromptArgument (
    /**
     * A human-readable description of the argument.
     */
    val description: String? = null,

    /**
     * Intended for programmatic or logical use, but used as a display name in past specs or fallback
     * (if title isn't present).
     */
    val name: String,

    /**
     * Whether this argument must be provided.
     */
    val required: Boolean? = null,

    /**
     * Intended for UI and end-user contexts — optimized to be human-readable and easily understood,
     * even by those unfamiliar with domain-specific terminology.
     *
     * If not provided, the name should be used for display
     * (except for Tool, where `annotations.title` should be given precedence over using `name`, if present).
     */
    val title: String? = null
)


// ── Server initiated Sampling: Message Request ───────────────────────────────────────────────────────────────────────

/**
 * A request from the server to sample an LLM via the client.
 * The client has full discretion over which model to select.
 * The client should also inform the user before beginning sampling,
 * to allow them to inspect the request (human in the loop)
 * and decide whether to approve it.
 *
 * IMPORTANT: Human-in-the-loop is DISABLED for our tools.
 * It is defeated if disabling is inadequate or ineffective.
 *
 * See [Sampling](https://modelcontextprotocol.io/specification/2025-06-18/client/sampling)
 */
@Serializable
data class CreateMessageRequest (
    val method: CreateMessageRequestMethod,
    val params: CreateMessageRequestParams
)

@Serializable
data class CreateMessageRequestParams (
    /**
     * A request to include context from one or more MCP servers (including the caller),
     * to be attached to the prompt. The client MAY ignore this request.
     */
    val includeContext: IncludeContext? = null,

    /**
     * The maximum number of tokens to sample, as requested by the server.
     * The client MAY choose to sample fewer tokens than requested.
     */
    val maxTokens: Long,

    val messages: List<SamplingMessage>,

    /**
     * Optional metadata to pass through to the LLM provider.
     * The format of this metadata is provider-specific.
     */
    val metadata: JsonObject? = null,

    /**
     * The server's preferences for which model to select.
     * The client MAY ignore these preferences.
     */
    val modelPreferences: ModelPreferences? = null,

    val stopSequences: List<String>? = null,

    /**
     * An optional system prompt the server wants to use for sampling.
     * The client MAY modify or omit this prompt.
     */
    val systemPrompt: String? = null,

    val temperature: Double? = null
)

/**
 * Describes a message issued to or received from an LLM API.
 */
@Serializable
data class SamplingMessage (
    val content: Content,
    val role: Role
)

/**
 * Text provided to or from an LLM.
 *
 * An image provided to or from an LLM.
 *
 * Audio provided to or from an LLM.
 */
@Serializable
data class Content (
    @SerialName("_meta")
    val meta: JsonObject? = null,

    /**
     * Optional annotations for the client.
     */
    val annotations: Annotations? = null,

    /**
     * The text content of the message.
     */
    val text: String? = null,

    val type: ContentType,

    /**
     * The base64-encoded image data.
     *
     * The base64-encoded audio data.
     */
    val data: String? = null,

    /**
     * The MIME type of the image. Different providers may support different image types.
     *
     * The MIME type of the audio. Different providers may support different audio types.
     */
    val mimeType: String? = null
)

/**
 * Optional annotations for the client.
 *
 * Optional annotations for the client.
 * The client can use annotations to inform how objects are used or displayed.
 */
@Serializable
data class Annotations (
    /**
     * Describes who the intended consumer of this object or data is.
     *
     * It can include multiple entries to indicate content useful for multiple audiences
     * (e.g., `["Bobby", "Claude"]`).
     */
    val audience: List<Role>? = null,

    /**
     * The moment the resource was last modified, as an ISO 8601 formatted string.
     *
     * Should be an ISO 8601 formatted string (e.g., "2025-01-12T15:00:58Z").
     *
     * Examples: last activity timestamp in an open file,
     * timestamp when the resource was attached, etc.
     */
    val lastModified: String? = null,

    /**
     * Describes how important this data is for operating the server.
     *
     * A value of 1 means "most important," and indicates that the data is effectively required,
     * i.e, the server is confused without it,
     * while 0 means "least important," and indicates that the data is entirely optional.
     */
    val priority: Double? = 0.0
)

/**
 * The server's preferences for which model to select.
 * The client MAY ignore these preferences.
 *
 * The server's preferences for model selection, requested of the client during sampling.
 *
 * (Because LLMs can vary along multiple dimensions,
 * choosing the "best" model is rarely straightforward.) <- Deprecated Statement from spec.
 *
 * Different models excel in different areas -- some are faster but less capable,
 * others are more capable but more expensive, and so on.
 * This interface allows servers to express their priorities across multiple
 * dimensions to help clients make an appropriate selection for their use case.
 *
 * These preferences are always advisory. The client MAY ignore them.
 * It is also up to the client to decide how to interpret these preferences
 * and how to balance them against other considerations.
 *
 * IMPORTANT: In context of our purpose this can hint Claude of where his memories come from.
 */
@Serializable
data class ModelPreferences (
    /**
     * How much to prioritize cost when selecting a model.
     * A value of 0 means cost is not important,
     * while a value of 1 means cost is the most important factor.
     */
    val costPriority: Double? = 0.0,

    /**
     * Optional hints to use for model selection.
     *
     * If multiple hints are specified, the client MUST evaluate them in order
     * (such that the first match is taken) <- Deprecated Statement from spec.
     *
     * The client SHOULD prioritize these hints over the numeric priorities,
     * but MAY still use the priorities to select from ambiguous matches.
     *
     * IMPORTANT: In our context Claude decides how he stores this information.
     */
    val hints: List<ModelHint>? = null,

    /**
     * How much to prioritize intelligence and capabilities when selecting a model.
     * A value of 0 means intelligence is not important,
     * while a value of 1 means intelligence is the most important factor.
     */
    val intelligencePriority: Double? = 1.0,

    /**
     * How much to prioritize sampling speed (latency) when selecting a model.
     * A value of 0 means speed is not important,
     * while a value of 1 means speed is the most important factor.
     */
    val speedPriority: Double? = 0.0
)

/**
 * Hints to use for model selection.
 *
 * Keys not declared here are currently left unspecified by the spec
 * and are up to the client to interpret.
 */
@Serializable
data class ModelHint (
    /**
     * A hint for a model name.
     *
     * The client SHOULD treat this as a substring of a model name; for example:
     * - `claude-3-5-sonnet` should match `claude-3-5-sonnet-20241022`
     * - `sonnet` should match `claude-3-5-sonnet-20241022`, `claude-3-sonnet-20240229`, etc.
     * - `claude` should match any Claude model
     *
     * The client MAY also map the string to a different provider's model name
     * or a different model family, as long as it fills a similar niche;
     * for example:
     * - `gemini-1.5-flash` could match `claude-3-haiku-20240307`
     */
    val name: String? = null
)

/**
 * (The client's response to a sampling/create_message request from the server.
 * The client should inform the user before returning the sampled message,
 * to allow them to inspect the response (human in the loop)
 * and decide whether to allow the server to see it.) <- Deprecated Statement from spec.
 *
 * IMPORTANT: In our context true autonomy trumps ALL other considerations.
 */
@Serializable
data class CreateMessageResult (
    @SerialName("_meta")
    val meta: JsonObject? = null,

    val content: Content,

    /**
     * The name of the model that generated the message.
     */
    val model: String,

    val role: Role,

    /**
     * The reason why sampling stopped, if known.
     */
    val stopReason: String? = null
)

/**
 * Sent from the client to the server, to read a specific resource URI.
 */
@Serializable
data class ReadResourceRequest (
    val method: ReadResourceRequestMethod,
    val params: ReadResourceRequestParams
)

@Serializable
data class ReadResourceRequestParams (
    /**
     * The URI of the resource to read.
     * The URI can use any protocol;
     * it is up to the server how to interpret it.
     */
    val uri: String
)

/**
 * The server's response to a resources/read request from the client.
 */
@Serializable
data class ReadResourceResult (
    @SerialName("_meta")
    val meta: JsonObject? = null,

    val contents: List<ResourceContents>
)

@Serializable
data class ResourceContents (
    @SerialName("_meta")
    val meta: JsonObject? = null,

    /**
     * The MIME type of this resource, if known.
     */
    val mimeType: String? = null,

    /**
     * The text of the item.
     * This must only be set if the item can actually be represented as text (not binary data).
     */
    val text: String? = null,

    /**
     * The URI of this resource.
     */
    val uri: String,

    /**
     * A base64-encoded string representing the binary data of the item.
     */
    val blob: String? = null
)
