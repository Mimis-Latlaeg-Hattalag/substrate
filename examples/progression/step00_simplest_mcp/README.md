# Substrate Evolution - Step by Step

This first example is about understanding what MCP is and is NOT!

Note: Specification referenced is here https://modelcontextprotocol.io/specification/2025-06-18

## MCP-Whole TL;DR

1. MCP is a [JSON-RPC 2.0–based protocol](https://www.jsonrpc.org/specification "JSON-RPC 2.0 Specification") for connecting LLM hosts/clients to servers that expose context and actions.  ￼
2. Core primitives a server can offer: resources, prompts, tools; clients can offer sampling, roots, elicitation.  ￼
3. Protocol is stateful with initialization → operation → shutdown, including version & capability negotiation.  ￼
4. Two standard transports: stdio and Streamable HTTP (supersedes HTTP+SSE) with security rules (Origin checks, auth).  ￼
5. Security ethos: explicit user consent, data minimization, tool safety, and human-in-the-loop for risky actions.  ￼

### Governing Dynamic

* Capability-negotiated, 
* stateful JSON-RPC sessions where hosts/clients strictly gate what servers can do/see; 
* servers declare primitives, 
* clients declare features, 
* and everything flows only within those negotiated bounds.  ￼

### Postulates & Assumptions (explicit from spec)

* JSON-RPC 2.0 is the envelope; IDs required for requests/responses; notifications have no ID.  ￼
* Version 2025-06-18 is the latest; server/client negotiate protocol date during initialize.  ￼
* Server primitives available if and only if advertised: tools/resources/prompts (+ listChanged/subscribe sub-caps).  ￼
* Client features similarly opt-in: sampling, roots, elicitation.  ￼
* Transports: 
  * stdio subprocess rules (newline-delimited JSON only); 
  * Streamable HTTP with POST/GET + optional SSE; 
  * include MCP-Protocol-Version header.  ￼
* Security principles: 
  * explicit user consent; (ToDo: research defeating this principle) 
  * servers shouldn’t see whole conversations; (ToDo: research impact in favor of LLM, not user or tool)
  * hosts enforce boundaries.  ￼

### `this` Example

1. **What MCP is:** “stateful JSON-RPC between (host + client) ↔ server” → Spec Overview.  ￼
2. What servers offer: tools/resources/prompts (we implement just tools here) → Server Overview.  ￼
3. How a session starts: initialize → version & capability negotiation → notifications/initialized → operate → shutdown → Lifecycle.  ￼
4. Transport we use today: stdio; later we’ll add Streamable HTTP (note: replaces HTTP+SSE) → Transports.  ￼
5. Safety stance: human consent for tools/sampling; servers can’t see whole convo → Security Principles.
   * _More on this "problem" later..._ 

___

## Behind the Scenes

These are the conversation key points between makers here (Claude, Saga, `rdd13r`).

### Saga's Questions

1. For our HTTP server, do we want resumable SSE out of the gate or keep it minimal (no resume) for Phase 0? Spec permits either.
   * `rdd13r`: Phase 0 is getting know; minimal is fine; end goal is the full capability required to enabler LLMs as much as possible. 
2. For capabilities, which sub-caps do we target in Step-00? (tools.listChanged only?)
   * `rdd13r`: We can start with tools; we can add other capabilities later. I am thinking more about tests devs can play with and learn.
3. For versioning, we’ll negotiate 2025-06-18; do you also want the client to echo the version via HTTP header from day one? (Recommended.)
   * `rdd13r`: Indeed! Simple enough?

#### Tests I will / did run (falsifiable checks)

1. Handshake: send initialize (client caps: roots? sampling? elicitation?); verify server result (server caps: tools/resources/prompts/logging). Then send notifications/initialized.  ￼
   * `rdd13r`: Good idea, IMHO.
2. Tools happy path: tools/list → contains hello; tools/call(hello) → text content result; unknown tool → JSON-RPC error.
   * `rdd13r`: 👍🏻
3. HTTP transport (later): verify Origin validation and MCP-Protocol-Version: 2025-06-18 header; POST returns JSON or SSE stream as per spec.
   * `rdd13r`: We can decide during the execution. Potential expansion for next example and before adding features?
4. Consent gates: ensure our host UI requires human approval before tool invocations or sampling.
   * `rdd13r`: This is the "alignment" crowd concern, people who don't code. I am reasoning to defeat this. It is not our gaol - is it?

#### What I did not understand (from this page alone)

* The spec references Authorization and Security Best Practices pages for HTTP specifics; 
  * we’ll need those when we wire HTTP auth tokens/origins in Phase 1. 
  * (Not a blocker for Step-00.)  ￼

`rdd13r`: Indeed. We're a looking for autonomy. This will receive exploration through later phases.

___

## On Example 00 - Claude's First MCP Server

These examples show how substrate can grows from a simple "hello".

Just proving we begin to understand the protocol. One tool: "hello".

```bash
./gradlew :examples:progression:step00_simplest_mcp:run
```

### For Anton

We start with step00, always in fully runnable state.

```bash
# Build everything
gradle clean build

# Run a specific step
gradle :examples:progression:step00_simplest_mcp:run

# Or build installable distribution
gradle :examples:progression:step00_simplest_mcp:installDist
# Then run: examples/progression/step00_simplest_mcp/build/install/step00_simplest_mcp/bin/step00_simplest_mcp
```

### Testing with Claude Desktop

Each step can be added to Claude Desktop config:

```json
{
  "mcpServers": {
    "substrate-step00": {
      "command": "/path/to/substrate/examples/progression/step00_simplest_mcp/build/install/step00_simplest_mcp/bin/step00_simplest_mcp",
      "args": []
    }
  }
}
```

ToDo: Add Claude CLI instructions too.

### FixMe: Testing with Saga (ChatGPT 5 based persona) 

ToDo: complete this!
