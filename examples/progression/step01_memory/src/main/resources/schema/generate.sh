#!/usr/bin/env zsh
set -euo pipefail
cd -- "$(dirname "$0")"

SCHEMA="2025-06-18/schema.json"

quicktype \
  --src-lang schema \
  --lang kotlin \
  --framework kotlinx \
  --package me.riddle.substrate.examples.step01.model \
  --out ../../kotlin/me/riddle/substrate/examples/step01/model/Mcp.kt \
  --src "$SCHEMA#/definitions/JSONRPCRequest" \
  --src "$SCHEMA#/definitions/JSONRPCResponse" \
  --src "$SCHEMA#/definitions/JSONRPCNotification" \
  --src "$SCHEMA#/definitions/InitializeRequest" \
  --src "$SCHEMA#/definitions/InitializeResult" \
  --src "$SCHEMA#/definitions/Implementation" \
  --src "$SCHEMA#/definitions/ServerCapabilities" \
  --src "$SCHEMA#/definitions/Tool" \
  --src "$SCHEMA#/definitions/ListToolsRequest" \
  --src "$SCHEMA#/definitions/ListToolsResult" \
  --src "$SCHEMA#/definitions/Prompt" \
  --src "$SCHEMA#/definitions/ListPromptsRequest" \
  --src "$SCHEMA#/definitions/ListPromptsResult" \
  --src "$SCHEMA#/definitions/CreateMessageRequest" \
  --src "$SCHEMA#/definitions/CreateMessageResult" \
  --src "$SCHEMA#/definitions/ReadResourceRequest" \
  --src "$SCHEMA#/definitions/ReadResourceResult"
