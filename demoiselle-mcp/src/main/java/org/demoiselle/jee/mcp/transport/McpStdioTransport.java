/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.transport;

import org.demoiselle.jee.mcp.handler.McpJsonRpcHandler;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcError;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcMessage;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcSerializer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Transporte stdio para o protocolo MCP.
 *
 * <p>Lê mensagens JSON-RPC delimitadas por newline de {@code stdin},
 * delega ao {@link McpJsonRpcHandler} e escreve respostas em {@code stdout}.
 * Logs são direcionados a {@code stderr} para manter {@code stdout}
 * exclusivo para mensagens JSON-RPC.</p>
 *
 * <p>Pode ser executado standalone via {@link #main(String[])} com Weld SE,
 * ou programaticamente via {@link #run(InputStream, OutputStream, McpJsonRpcHandler, JsonRpcSerializer)}.</p>
 */
public class McpStdioTransport {

    private static final Logger LOGGER = Logger.getLogger(McpStdioTransport.class.getName());
    private static final String SESSION_ID = "stdio";

    /**
     * Ponto de entrada standalone. Inicializa Weld SE, obtém handler e
     * serializer via CDI e inicia o loop de leitura.
     *
     * <p>Weld SE é carregado via reflexão para evitar dependência de compilação.
     * O classpath de execução deve incluir {@code weld-se-shaded}.</p>
     *
     * @param args argumentos de linha de comando (ignorados)
     */
    public static void main(String[] args) {
        // Redirect JUL to stderr so stdout stays clean for JSON-RPC
        Logger rootLogger = Logger.getLogger("");
        for (var h : rootLogger.getHandlers()) {
            rootLogger.removeHandler(h);
        }
        var stderrHandler = new java.util.logging.StreamHandler(
                System.err, new java.util.logging.SimpleFormatter());
        stderrHandler.setLevel(Level.ALL);
        rootLogger.addHandler(stderrHandler);

        LOGGER.info("Starting MCP stdio transport...");

        try {
            // Load Weld SE via reflection to avoid compile-time dependency
            Class<?> weldClass = Class.forName("org.jboss.weld.environment.se.Weld");
            Object weld = weldClass.getDeclaredConstructor().newInstance();
            Object container = weldClass.getMethod("initialize").invoke(weld);

            // container is WeldContainer which implements SeContainer (AutoCloseable)
            try {
                var selectHandler = container.getClass().getMethod("select", Class.class, java.lang.annotation.Annotation[].class);
                Object handlerInstance = selectHandler.invoke(container, McpJsonRpcHandler.class, new java.lang.annotation.Annotation[0]);
                McpJsonRpcHandler handler = (McpJsonRpcHandler) handlerInstance.getClass().getMethod("get").invoke(handlerInstance);

                Object serializerInstance = selectHandler.invoke(container, JsonRpcSerializer.class, new java.lang.annotation.Annotation[0]);
                JsonRpcSerializer serializer = (JsonRpcSerializer) serializerInstance.getClass().getMethod("get").invoke(serializerInstance);

                McpStdioTransport transport = new McpStdioTransport();
                transport.run(System.in, System.out, handler, serializer);

                LOGGER.info("MCP stdio transport shutting down.");
            } finally {
                // Close the container (SeContainer extends AutoCloseable)
                container.getClass().getMethod("close").invoke(container);
            }
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE,
                    "Weld SE not found on classpath. Add weld-se-shaded to run stdio transport.", e);
            System.exit(1);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal error in MCP stdio transport", e);
            System.exit(1);
        }
    }

    /**
     * Loop principal de leitura de stdin, processamento e escrita em stdout.
     *
     * <p>Cada linha lida de {@code in} é tratada como uma mensagem JSON-RPC
     * completa. A resposta (se houver) é escrita como uma única linha em
     * {@code out}. O loop termina quando EOF é alcançado.</p>
     *
     * @param in         stream de entrada (normalmente {@code System.in})
     * @param out        stream de saída (normalmente {@code System.out})
     * @param handler    o handler JSON-RPC MCP
     * @param serializer o serializador JSON-RPC
     */
    public void run(InputStream in, OutputStream out,
                    McpJsonRpcHandler handler, JsonRpcSerializer serializer) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        PrintWriter writer = new PrintWriter(out, true);

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                LOGGER.log(Level.FINE, "Received: {0}", line);

                try {
                    JsonRpcMessage request = serializer.deserialize(line);
                    JsonRpcMessage response = handler.handle(SESSION_ID, request);

                    // Notifications produce no response (JSON-RPC 2.0 spec)
                    if (response != null) {
                        String responseJson = serializer.serialize(response);
                        writer.println(responseJson);
                        LOGGER.log(Level.FINE, "Sent: {0}", responseJson);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error processing message", e);
                    // Send parse error response
                    JsonRpcMessage errorResponse = JsonRpcMessage.error(null,
                            new JsonRpcError(JsonRpcError.PARSE_ERROR,
                                    "Parse error: " + e.getMessage(), null));
                    writer.println(serializer.serialize(errorResponse));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error reading from stdin", e);
        }

        LOGGER.info("EOF reached on stdin, stopping stdio transport.");
    }
}
