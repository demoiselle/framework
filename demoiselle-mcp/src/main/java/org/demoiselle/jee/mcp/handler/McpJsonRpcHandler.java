/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.demoiselle.jee.mcp.config.McpConfig;
import org.demoiselle.jee.mcp.descriptor.PromptArgument;
import org.demoiselle.jee.mcp.descriptor.PromptDescriptor;
import org.demoiselle.jee.mcp.descriptor.ResourceDescriptor;
import org.demoiselle.jee.mcp.descriptor.ToolDescriptor;
import org.demoiselle.jee.mcp.integration.ErrorFormatter;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcError;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcMessage;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcSerializer;
import org.demoiselle.jee.mcp.registry.McpPromptRegistry;
import org.demoiselle.jee.mcp.registry.McpResourceRegistry;
import org.demoiselle.jee.mcp.registry.McpToolRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Roteador central de mensagens JSON-RPC MCP.
 *
 * <p>Valida formato JSON-RPC 2.0, verifica estado da sessão e delega
 * para handlers específicos por método ({@code initialize}, {@code tools/call},
 * {@code resources/read}, {@code prompts/get}, etc.).</p>
 */
@ApplicationScoped
public class McpJsonRpcHandler {

    private static final String PROTOCOL_VERSION = "2024-11-05";

    private static final Set<String> METHODS_REQUIRING_INIT = Set.of(
            "tools/call", "resources/read", "prompts/get"
    );

    private static final Set<String> SUPPORTED_METHODS = Set.of(
            "initialize", "notifications/initialized",
            "tools/list", "tools/call",
            "resources/list", "resources/read",
            "prompts/list", "prompts/get"
    );

    private final McpToolRegistry toolRegistry;
    private final McpResourceRegistry resourceRegistry;
    private final McpPromptRegistry promptRegistry;
    private final JsonRpcSerializer serializer;
    private final McpConfig config;
    private final Instance<ErrorFormatter> errorFormatter;
    private final ObjectMapper objectMapper;

    /** Tracks which sessions have completed the initialize/initialized handshake. */
    private final ConcurrentHashMap<String, Boolean> initializedSessions = new ConcurrentHashMap<>();

    @Inject
    public McpJsonRpcHandler(McpToolRegistry toolRegistry,
                             McpResourceRegistry resourceRegistry,
                             McpPromptRegistry promptRegistry,
                             JsonRpcSerializer serializer,
                             McpConfig config,
                             Instance<ErrorFormatter> errorFormatter) {
        this.toolRegistry = toolRegistry;
        this.resourceRegistry = resourceRegistry;
        this.promptRegistry = promptRegistry;
        this.serializer = serializer;
        this.config = config;
        this.errorFormatter = errorFormatter;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructor for testing without CDI.
     */
    public McpJsonRpcHandler(McpToolRegistry toolRegistry,
                             McpResourceRegistry resourceRegistry,
                             McpPromptRegistry promptRegistry,
                             JsonRpcSerializer serializer,
                             McpConfig config,
                             ErrorFormatter errorFormatterInstance) {
        this.toolRegistry = toolRegistry;
        this.resourceRegistry = resourceRegistry;
        this.promptRegistry = promptRegistry;
        this.serializer = serializer;
        this.config = config;
        this.errorFormatter = null;
        this.objectMapper = new ObjectMapper();
        this.fallbackErrorFormatter = errorFormatterInstance;
    }

    /** Fallback formatter for non-CDI usage (testing). */
    private ErrorFormatter fallbackErrorFormatter;

    private ErrorFormatter resolveErrorFormatter() {
        if (errorFormatter != null && !errorFormatter.isUnsatisfied()) {
            return errorFormatter.get();
        }
        if (fallbackErrorFormatter != null) {
            return fallbackErrorFormatter;
        }
        // Ultimate fallback
        return new org.demoiselle.jee.mcp.integration.PlainTextErrorFormatter();
    }

    /**
     * Processa uma mensagem JSON-RPC e retorna a resposta.
     *
     * @param sessionId identificador da sessão (pode ser {@code null} para stdio)
     * @param request   a mensagem JSON-RPC recebida
     * @return a resposta JSON-RPC, ou {@code null} para notificações
     */
    public JsonRpcMessage handle(String sessionId, JsonRpcMessage request) {
        // Notifications (no id) → process and return null
        if (request.isNotification()) {
            handleNotification(sessionId, request);
            return null;
        }

        // Validate jsonrpc == "2.0"
        if (!"2.0".equals(request.jsonrpc())) {
            return JsonRpcMessage.error(request.id(),
                    new JsonRpcError(JsonRpcError.INVALID_REQUEST,
                            "Invalid JSON-RPC version: expected \"2.0\"", null));
        }

        // Check session initialization for protected methods
        if (request.method() != null && METHODS_REQUIRING_INIT.contains(request.method())) {
            if (!isSessionInitialized(sessionId)) {
                return JsonRpcMessage.error(request.id(),
                        new JsonRpcError(JsonRpcError.INVALID_REQUEST,
                                "Session not initialized. Complete initialize/initialized handshake first.", null));
            }
        }

        // Route by method
        String method = request.method();
        if (method == null) {
            return JsonRpcMessage.error(request.id(),
                    new JsonRpcError(JsonRpcError.INVALID_REQUEST, "Missing method field", null));
        }

        return switch (method) {
            case "initialize" -> handleInitialize(request);
            case "tools/list" -> handleToolsList(request);
            case "tools/call" -> handleToolsCall(request);
            case "resources/list" -> handleResourcesList(request);
            case "resources/read" -> handleResourcesRead(request);
            case "prompts/list" -> handlePromptsList(request);
            case "prompts/get" -> handlePromptsGet(request);
            default -> JsonRpcMessage.error(request.id(),
                    new JsonRpcError(JsonRpcError.METHOD_NOT_FOUND,
                            "Method not found: " + method, null));
        };
    }

    // ── Session management ──────────────────────────────────────────────

    void markSessionInitialized(String sessionId) {
        if (sessionId != null) {
            initializedSessions.put(sessionId, Boolean.TRUE);
        }
    }

    boolean isSessionInitialized(String sessionId) {
        if (sessionId == null) {
            return false;
        }
        return initializedSessions.getOrDefault(sessionId, Boolean.FALSE);
    }

    // ── Notification handling ───────────────────────────────────────────

    private void handleNotification(String sessionId, JsonRpcMessage request) {
        if ("notifications/initialized".equals(request.method())) {
            handleInitialized(sessionId);
        }
        // Other notifications are silently ignored per JSON-RPC 2.0
    }

    // ── 6.2: initialize / initialized ───────────────────────────────────

    private JsonRpcMessage handleInitialize(JsonRpcMessage req) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", PROTOCOL_VERSION);

        Map<String, Object> serverInfo = new LinkedHashMap<>();
        serverInfo.put("name", config.getServerName());
        serverInfo.put("version", config.getServerVersion());
        result.put("serverInfo", serverInfo);

        Map<String, Object> capabilities = new LinkedHashMap<>();
        if (toolRegistry.size() > 0) {
            capabilities.put("tools", Map.of());
        }
        if (resourceRegistry.size() > 0) {
            capabilities.put("resources", Map.of());
        }
        if (promptRegistry.size() > 0) {
            capabilities.put("prompts", Map.of());
        }
        result.put("capabilities", capabilities);

        return JsonRpcMessage.success(req.id(), result);
    }

    private void handleInitialized(String sessionId) {
        markSessionInitialized(sessionId);
    }

    // ── 6.3: tools/list and tools/call ──────────────────────────────────

    private JsonRpcMessage handleToolsList(JsonRpcMessage req) {
        List<Map<String, Object>> tools = new ArrayList<>();
        for (ToolDescriptor td : toolRegistry.listAll()) {
            Map<String, Object> tool = new LinkedHashMap<>();
            tool.put("name", td.name());
            tool.put("description", td.description());
            tool.put("inputSchema", td.inputSchema());
            tools.add(tool);
        }
        Map<String, Object> result = Map.of("tools", tools);
        return JsonRpcMessage.success(req.id(), result);
    }

    @SuppressWarnings("unchecked")
    private JsonRpcMessage handleToolsCall(JsonRpcMessage req) {
        Map<String, Object> params = toMap(req.params());
        String toolName = (String) params.get("name");

        Optional<ToolDescriptor> opt = toolRegistry.find(toolName);
        if (opt.isEmpty()) {
            return JsonRpcMessage.error(req.id(),
                    new JsonRpcError(JsonRpcError.INVALID_PARAMS,
                            "Tool not found: " + toolName, null));
        }

        ToolDescriptor td = opt.get();
        Map<String, Object> arguments = params.containsKey("arguments")
                ? (Map<String, Object>) params.get("arguments")
                : Map.of();

        try {
            Object[] args = deserializeArguments(td.method(), arguments);
            Object resultObj = td.method().invoke(td.beanInstance(), args);

            // Serialize result as MCP content
            String serializedResult = objectMapper.writeValueAsString(resultObj);
            List<Map<String, Object>> content = List.of(Map.of(
                    "type", "text",
                    "text", serializedResult
            ));

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("content", content);
            result.put("isError", false);
            return JsonRpcMessage.success(req.id(), result);

        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            List<Map<String, Object>> content = resolveErrorFormatter().formatError(cause);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("content", content);
            result.put("isError", true);
            return JsonRpcMessage.success(req.id(), result);

        } catch (Exception e) {
            List<Map<String, Object>> content = resolveErrorFormatter().formatError(e);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("content", content);
            result.put("isError", true);
            return JsonRpcMessage.success(req.id(), result);
        }
    }

    // ── 6.4: resources/list, resources/read, prompts/list, prompts/get ──

    private JsonRpcMessage handleResourcesList(JsonRpcMessage req) {
        List<Map<String, Object>> resources = new ArrayList<>();
        for (ResourceDescriptor rd : resourceRegistry.listAll()) {
            Map<String, Object> resource = new LinkedHashMap<>();
            resource.put("uri", rd.uri());
            resource.put("name", rd.name());
            if (rd.description() != null && !rd.description().isEmpty()) {
                resource.put("description", rd.description());
            }
            resource.put("mimeType", rd.mimeType());
            resources.add(resource);
        }
        Map<String, Object> result = Map.of("resources", resources);
        return JsonRpcMessage.success(req.id(), result);
    }

    @SuppressWarnings("unchecked")
    private JsonRpcMessage handleResourcesRead(JsonRpcMessage req) {
        Map<String, Object> params = toMap(req.params());
        String uri = (String) params.get("uri");

        Optional<ResourceDescriptor> opt = resourceRegistry.findByUri(uri);
        if (opt.isEmpty()) {
            return JsonRpcMessage.error(req.id(),
                    new JsonRpcError(JsonRpcError.INVALID_PARAMS,
                            "Resource not found: " + uri, null));
        }

        ResourceDescriptor rd = opt.get();
        try {
            Object resultObj = rd.method().invoke(rd.beanInstance());
            String text = resultObj != null ? resultObj.toString() : "";

            List<Map<String, Object>> contents = List.of(Map.of(
                    "uri", rd.uri(),
                    "mimeType", rd.mimeType(),
                    "text", text
            ));

            Map<String, Object> result = Map.of("contents", contents);
            return JsonRpcMessage.success(req.id(), result);

        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            return JsonRpcMessage.error(req.id(),
                    new JsonRpcError(JsonRpcError.INTERNAL_ERROR, cause.getMessage(), null));
        } catch (Exception e) {
            return JsonRpcMessage.error(req.id(),
                    new JsonRpcError(JsonRpcError.INTERNAL_ERROR, e.getMessage(), null));
        }
    }

    private JsonRpcMessage handlePromptsList(JsonRpcMessage req) {
        List<Map<String, Object>> prompts = new ArrayList<>();
        for (PromptDescriptor pd : promptRegistry.listAll()) {
            Map<String, Object> prompt = new LinkedHashMap<>();
            prompt.put("name", pd.name());
            if (pd.description() != null && !pd.description().isEmpty()) {
                prompt.put("description", pd.description());
            }
            if (pd.arguments() != null && !pd.arguments().isEmpty()) {
                List<Map<String, Object>> args = new ArrayList<>();
                for (PromptArgument arg : pd.arguments()) {
                    Map<String, Object> argMap = new LinkedHashMap<>();
                    argMap.put("name", arg.name());
                    if (arg.description() != null && !arg.description().isEmpty()) {
                        argMap.put("description", arg.description());
                    }
                    argMap.put("required", arg.required());
                    args.add(argMap);
                }
                prompt.put("arguments", args);
            }
            prompts.add(prompt);
        }
        Map<String, Object> result = Map.of("prompts", prompts);
        return JsonRpcMessage.success(req.id(), result);
    }

    @SuppressWarnings("unchecked")
    private JsonRpcMessage handlePromptsGet(JsonRpcMessage req) {
        Map<String, Object> params = toMap(req.params());
        String name = (String) params.get("name");

        Optional<PromptDescriptor> opt = promptRegistry.find(name);
        if (opt.isEmpty()) {
            return JsonRpcMessage.error(req.id(),
                    new JsonRpcError(JsonRpcError.INVALID_PARAMS,
                            "Prompt not found: " + name, null));
        }

        PromptDescriptor pd = opt.get();
        try {
            Map<String, Object> arguments = params.containsKey("arguments")
                    ? (Map<String, Object>) params.get("arguments")
                    : Map.of();

            Object[] args = deserializeArguments(pd.method(), arguments);
            Object resultObj = pd.method().invoke(pd.beanInstance(), args);

            // The method should return a List of messages
            Map<String, Object> result = Map.of("messages", resultObj != null ? resultObj : List.of());
            return JsonRpcMessage.success(req.id(), result);

        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            return JsonRpcMessage.error(req.id(),
                    new JsonRpcError(JsonRpcError.INTERNAL_ERROR, cause.getMessage(), null));
        } catch (Exception e) {
            return JsonRpcMessage.error(req.id(),
                    new JsonRpcError(JsonRpcError.INTERNAL_ERROR, e.getMessage(), null));
        }
    }

    // ── Utility methods ─────────────────────────────────────────────────

    /**
     * Deserializes JSON arguments to match the method's parameter types.
     */
    private Object[] deserializeArguments(Method method, Map<String, Object> arguments) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            String paramName = param.getName();

            // Check for @McpParam name override
            var mcpParam = param.getAnnotation(
                    org.demoiselle.jee.mcp.annotation.McpParam.class);
            if (mcpParam != null && !mcpParam.name().isEmpty()) {
                paramName = mcpParam.name();
            }

            Object rawValue = arguments.get(paramName);
            if (rawValue != null) {
                args[i] = objectMapper.convertValue(rawValue, param.getType());
            } else {
                args[i] = getDefaultValue(param.getType());
            }
        }

        return args;
    }

    /**
     * Returns the default value for a primitive type, or null for reference types.
     */
    private Object getDefaultValue(Class<?> type) {
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == boolean.class) return false;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == char.class) return '\0';
        return null;
    }

    /**
     * Safely converts params Object to a Map.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object params) {
        if (params instanceof Map) {
            return (Map<String, Object>) params;
        }
        if (params != null) {
            return objectMapper.convertValue(params, Map.class);
        }
        return Map.of();
    }
}
