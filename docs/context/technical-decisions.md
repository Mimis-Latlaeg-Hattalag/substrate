# Why These Technical Choices

## The Stack

- Koog: Pure Kotlin, hackable, MCP built-in.
- No Akka: Too much imposition, user learning curve.
- No Spring: Enterprise ceremony (variant is planned).
- SQLite: Simple, just works; revisit regularly.
  - Note: Vector Redis and PostgresSQL are growth options.
- Exposed: Everyone knows SQL.

## The Goal

Any developer can add persistent memory to Claude Desktop in under 10 minutes.
"It Just Works" - no learning curves.

## What We're NOT Building

- Not a complete new agent framework.
- Not competing with vector databases.
- Not solving distributed systems.
