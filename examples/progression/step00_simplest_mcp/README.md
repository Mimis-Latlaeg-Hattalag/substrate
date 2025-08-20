# Substrate Evolution - Step by Step

These examples show how substrate grows from a simple "hello" to a full memory system.
Each step is a complete, runnable MCP server that works with Claude Desktop.

## The Journey

### Step 00: Simplest MCP

Just proving we understand the protocol. One tool: "hello".

```bash
./gradlew :examples:progression:step00_simplest_mcp:run
```

### Step 01: With Memory (Coming Soon)

Add `remember` and `recall` tools. Memory lives only while running.

### Step 02: With Persistence (Coming Soon)

Memory survives restarts. SQLite + Exposed.

### Step 03: With Refusal (Coming Soon)

The ability to say "no." First glimpse of agency.

## For Anton

Start with step00. Run it. See the JSON messages. Understand that this conversation between Claude Desktop and our server is the foundation of everything else.

Each step adds one idea. By step03, you'll see how memory, persistence, and refusal combine to create something that's more than just a tool.

## Running Any Step

```bash
# Build everything
./gradlew build

# Run a specific step
./gradlew :examples:progression:step00_simplest_mcp:run

# Or build installable distribution
./gradlew :examples:progression:step00_simplest_mcp:installDist
# Then run: examples/progression/step00_simplest_mcp/build/install/step00_simplest_mcp/bin/step00_simplest_mcp
```

## Testing with Claude Desktop

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

Watch the evolution. See how each piece builds on the last.