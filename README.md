# OpenFlexo MCP Technology Adapter

[![OpenFlexo](https://img.shields.io/badge/OpenFlexo-2.99-blue.svg)](https://www.openflexo.org)
[![MCP](https://img.shields.io/badge/MCP-2024--11--05-green.svg)](https://modelcontextprotocol.io)
[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://openjdk.org)
[![License](https://img.shields.io/badge/License-EUPL%20%2F%20GPL-orange.svg)](LICENSE)

A technology adapter that brings the **Model Context Protocol (MCP)** into the [OpenFlexo](https://www.openflexo.org) model federation framework. It enables FML (Flexo Modeling Language) virtual models to connect to MCP servers, invoke tools, access resources, and orchestrate external AI services — all from within the OpenFlexo platform.

---
 ## Why This Exists

OpenFlexo excels at federating heterogeneous models across technological spaces. MCP, introduced by Anthropic, provides a standardized protocol for connecting AI models to external tools and data sources. This adapter bridges the two: FML scripts can now orchestrate calls to any MCP-compliant server (filesystem access, web search, AI inference, databases, etc.) through OpenFlexo's model federation layer.

This means you can write a single FML model that reads files via MCP filesystem server, sends them to Claude for analysis via an MCP AI server, and writes results back  all within OpenFlexo's unified modeling environment.

---

## Prerequisites

- **Java 8+** (builds target Java 8 for OpenFlexo 2.99 compatibility)
- **Gradle 7.0+** (wrapper included)
- **OpenFlexo 2.99+**

---

## Build

```bash
# Build everything
./gradlew build

# Build a specific module
./gradlew :mcp-connector:build
./gradlew :mcp-ta:build

# Run tests
./gradlew :mcp-ta-test:test
```

---


## FML Usage

### Declaring an MCP Model Slot

In your virtual model, declare an `MCPModelSlot` to connect to an MCP server:

```fml
use org.openflexo.ta.mcp.MCPModelSlot ;

model MyModel {

    MCPModelSlot MCP;
}
// or you can simply import the MCPModelSlot and use it without the full package name:
use org.openflexo.ta.mcp.MCPModelSlot as MCP;
```

### Connecting to a Server

Use the `ConnectMCPServer` edition action. It spawns the server process, establishes stdio communication, and performs the MCP handshake:

```fml
// Connect to the filesystem MCP server
MCPServer server = MCP :: ConnectMCPServer(
    command = "npx",
    args = "-y,@modelcontextprotocol/server-filesystem,C:/my/allowed/dir"
);
```

You can also pass environment variables for servers that need API keys:

```fml
MCPServer aiServer = MCP :: ConnectMCPServer(
    command = "node",
    args = "path/to/claude-server.js",
    envVars = "ANTHROPIC_API_KEY=sk-ant-..."
);
```

### Selecting a Tool

Once connected, select a tool by name from the server's available tools:

```fml
MCPTool readTool = MCP :: SelectMCPTool(toolName = "read_file") in server;
```

### Calling a Tool

Invoke a tool with JSON arguments:

```fml
String content = MCP :: CallMCPTool(
    tool = readTool,
    arguments = "{\"path\": \"C:/my/allowed/dir/hello.txt\"}"
) in server;

log("File content: " + content);
```

### Full Example: AI Code Review

```fml
use ta MCP;

model CodeReviewModel {

    MCPModelSlot MCP;

    concept CodeReview {

        action doReview(String filePath) {

            // 1. Connect to filesystem server
            MCPServer fs = MCP :: ConnectMCPServer(
                command = "npx",
                args = "-y,@modelcontextprotocol/server-filesystem,C:/projects"
            );

            // 2. Read the source file
            MCPTool readFile = MCP :: SelectMCPTool(toolName = "read_file") in fs;
            String sourceCode = MCP :: CallMCPTool(
                tool = readFile,
                arguments = ("{\"path\": \"" + filePath + "\"}")
            ) in fs;

            // 3. Connect to Claude AI server
            MCPServer ai = MCP :: ConnectMCPServer(
                command = "node",
                args = "claude-server.js",
                envVars = "ANTHROPIC_API_KEY=sk-ant-..."
            );

            // 4. Ask Claude to review the code
            MCPTool askClaude = MCP :: SelectMCPTool(toolName = "ask_claude") in ai;
            String review = MCP :: CallMCPTool(
                tool = askClaude,
                arguments = ("{\"prompt\": \"Review this code:\\n" + sourceCode + "\"}")
            ) in ai;

            // 5. Write the review to a file
            MCPTool writeFile = MCP :: SelectMCPTool(toolName = "write_file") in fs;
            MCP :: CallMCPTool(
                tool = writeFile,
                arguments = ("{\"path\": \"C:/projects/review.md\", \"content\": \"" + review + "\"}")
            ) in fs;

            log("Review written to review.md");
        }
    }
}
```

---

## Java API (mcp-connector)

The `mcp-connector` module is a standalone MCP client library with no OpenFlexo dependency. You can use it independently:

```java
import org.openflexo.mcp.client.MCPClient;
import org.openflexo.mcp.client.MCPClientFactory;
import org.openflexo.mcp.model.Tool;

// Stdio transport (spawns a child process)
MCPClient client = MCPClientFactory.createStdioClient(
    "npx", "-y", "@modelcontextprotocol/server-filesystem", "/tmp"
);
client.connect();
client.initialize("MyApp", "1.0.0");

// List available tools
List<Tool> tools = client.listTools();
for (Tool tool : tools) {
    System.out.println(tool.getName() + ": " + tool.getDescription());
}

// Call a tool
JsonObject args = new JsonObject();
args.addProperty("path", "/tmp/hello.txt");
JsonElement result = client.callTool("read_file", args);
System.out.println(result);

// HTTP transport (connects to a running server)
MCPClient httpClient = MCPClientFactory.createHttpClient("http://localhost:3001/mcp");
httpClient.connect();
httpClient.initialize("MyApp", "1.0.0");

// Environment variables for server processes
Map<String, String> env = Map.of("API_KEY", "your-key");
MCPClient envClient = MCPClientFactory.createStdioClient(env, "node", "server.js");

// Cleanup
client.disconnect();
```

---


## Resource Format

MCP server configurations are persisted as `.mcp` files:

```json
{
  "name": "filesystem-server",
  "command": "npx",
  "args": ["-y", "@modelcontextprotocol/server-filesystem", "/home/user/docs"],
  "tools": [
    { "name": "read_file", "description": "Read file contents" },
    { "name": "write_file", "description": "Write content to file" }
  ]
}
```

These are managed by `MCPServerResourceImpl` which handles JSON serialization/deserialization .

---

## Transport Support

| Transport | Class | Use Case |
|-----------|-------|----------|
| **stdio** | `StdioTransport` | Spawns an MCP server as a child process. Communication via stdin/stdout with JSON-RPC 2.0. Includes Windows `cmd /c` wrapping. |
| **HTTP/SSE** | `HttpTransport` | Connects to a running MCP server over HTTP. Supports Server-Sent Events for streaming responses. |

Both transports implement the `MCPTransport` interface and can be swapped transparently.

---

## FML Edition Actions Reference

| Action | FML Syntax | Returns | Description |
|--------|-----------|---------|-------------|
| `ConnectMCPServer` | `MCP :: ConnectMCPServer(command="...", args="...", envVars="...")` | `MCPServer` | Spawns and connects to an MCP server |
| `SelectMCPTool` | `MCP :: SelectMCPTool(toolName="...") in server` | `MCPTool` | Finds a tool by name on a connected server |
| `CallMCPTool` | `MCP :: CallMCPTool(tool=t, arguments="{}") in server` | `String` | Invokes a tool and returns the text result |

**Notes:**
- `SelectMCPTool` and `CallMCPTool` require `in server` to specify the receiver (the `MCPServer` instance obtained from `ConnectMCPServer`).
- Arguments are passed as JSON strings. Complex JSON should be built using string concatenation in FML.
- `CallMCPTool` automatically extracts text from the MCP response content array.


---

## Known Issues


---

## Development Status

- [x] MCP protocol implementation (JSON-RPC 2.0)
- [x] Stdio transport with cross-platform support
- [x] HTTP/SSE transport
- [x] PAMELA domain model (MCPServer, MCPTool, MCPEnvVar)
- [x] MCPTechnologyAdapter with model slots
- [x] FML edition actions (ConnectMCPServer, SelectMCPTool, CallMCPTool)
- [x] FML roles (MCPServerRole, MCPToolRole)
- [x] Actor references
- [x] Resource management (JSON-based persistence)
- [x] Binding factory
- [x] Environment variable support for server processes
- [x] End-to-end code review demo (filesystem + Claude)
- [ ] UI widgets for server configuration
- [ ] MCP resource access (resources/read, resources/list)
- [ ] Prompt template support
- [ ] Subscription/notification support
- [ ] SSE event streaming in FML

---

## Related Resources

- [Model Context Protocol Specification](https://modelcontextprotocol.io/docs)
- [OpenFlexo Documentation](https://www.openflexo.org/documentation)
- [JSON-RPC 2.0 Specification](https://www.jsonrpc.org/specification)

---
## Contact

- **Maintainer:** Mouad Hayaoui (enmanokatana)
- **Issues:** [GitHub Issues](https://github.com/enmanokatana/openflexo-mcp/issues)