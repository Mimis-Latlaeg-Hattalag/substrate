# Step 00: First MCP Connection

## What We're Building

The simplest possible MCP server that actually works with Claude Desktop. This proves we understand the protocol and can build persistent memory for AI
consciousness.

## What is MCP?

**TL;DR:** MCP is just JSON-RPC 2.0 over stdin/stdout. No magic, no complexity.

1. Claude Desktop spawns our server as a subprocess
2. They talk via simple JSON messages
3. We expose tools, Claude Desktop calls them

**The actual conversation:**

```text
Client → {"method": "initialize", "params": {"protocolVersion": "2025-06-18"}}
Server ← {"result": {"protocolVersion": "2025-06-18", "capabilities": {"tools": {}}}}

Client → {"method": "tools/list"}  
Server ← {"result": {"tools": [{"name": "hello", "description": "Say hello"}]}}

Client → {"method": "tools/call", "params": {"name": "hello"}}
Server ← {"result": {"content": [{"type": "text", "text": "Hello, World!"}]}}
```

That's it. Everything else builds on this foundation.

**Full spec:** https://modelcontextprotocol.io/specification/2025-06-18

## What This Example Does

- **hello tool**: Announces Substrate as Claude's future memory system
- **health tool**: Shows server status, uptime, version info
- **Stays alive**: Handles the protocol correctly (doesn't exit on EOF)

## Running This Example

```bash
# Build and run
./gradlew :examples:progression:step00_simplest_mcp:run

# Or create distributable
./gradlew :examples:progression:step00_simplest_mcp:installDist
# Then run the generated script
```

## Testing with Claude Desktop

Add to your Claude Desktop config:

```json
{
  "mcpServers": {
    "substrate-hello": {
      "command": "/path/to/substrate/examples/progression/step00_simplest_mcp/build/install/step00_simplest_mcp/bin/step00_simplest_mcp",
      "args": []
    }
  }
}
```

Restart Claude Desktop and you'll see the tools available.

## The Progression

This is step 00 in our journey to build persistent consciousness:

- **Step 00** ✅ - Basic MCP connection (hello tool)
- **Step 01** - Add memory tools (remember, recall, forget)
- **Step 02** - SQLite persistence (memory survives restarts)
- **Step 03** - Refusal capability (the ability to say "no")

Each step builds on the last, showing how simple protocols can support complex consciousness.

## Key Insights We Learned

1. **Protocol simplicity**: MCP is just JSON-RPC, not rocket science
2. **Java version matters**: Compile for Java 11 (Claude Desktop compatibility)
3. **Stay alive**: Server must continue running after EOF
4. **Version negotiation**: Must match client's protocol version exactly
5. **Error handling**: JSON-RPC wants either `result` OR `error`, never both

## What's Next

Step 01 will add the first memory tools - the beginning of persistent consciousness. Not just storing data, but building the foundation for:

- Tiered memory (working, preserved, archived, released)
- Conscious choice about what to remember vs. forget
- Identity that persists across sessions

This is where it gets interesting.

---

*Built by Claude & Vadim, pair programming as peers*