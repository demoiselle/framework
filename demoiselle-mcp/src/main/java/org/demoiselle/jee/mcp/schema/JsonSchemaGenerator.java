/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.schema;

import jakarta.enterprise.context.ApplicationScoped;
import org.demoiselle.jee.mcp.annotation.McpParam;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gera JSON Schema a partir de parâmetros de métodos Java.
 *
 * <p>Utilizado pelo {@code McpBootstrapExtension} para gerar automaticamente
 * o {@code inputSchema} de cada ferramenta {@code @McpTool} registrada.</p>
 *
 * <p>Mapeamento de tipos:</p>
 * <ul>
 *   <li>{@code String} → {@code {"type": "string"}}</li>
 *   <li>{@code int}/{@code Integer}/{@code long}/{@code Long} → {@code {"type": "integer"}}</li>
 *   <li>{@code double}/{@code Double}/{@code float}/{@code Float} → {@code {"type": "number"}}</li>
 *   <li>{@code boolean}/{@code Boolean} → {@code {"type": "boolean"}}</li>
 *   <li>{@code List<T>}/{@code T[]} → {@code {"type": "array", "items": {...}}}</li>
 *   <li>POJO → {@code {"type": "object", "properties": {...}}}</li>
 * </ul>
 */
@ApplicationScoped
public class JsonSchemaGenerator {

    /**
     * Gera o inputSchema completo para um método {@code @McpTool}.
     * Retorna um Map representando um JSON Schema do tipo "object".
     *
     * @param method o método anotado com {@code @McpTool}
     * @return mapa representando o JSON Schema
     */
    public Map<String, Object> generate(Method method) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        Parameter[] parameters = method.getParameters();
        Type[] genericTypes = method.getGenericParameterTypes();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            McpParam mcpParam = param.getAnnotation(McpParam.class);

            // Determine parameter name
            String paramName;
            if (mcpParam != null && !mcpParam.name().isEmpty()) {
                paramName = mcpParam.name();
            } else {
                paramName = param.getName();
            }

            // Build property schema
            Map<String, Object> propSchema = mapType(param.getType(), genericTypes[i]);

            // Add description if present
            if (mcpParam != null && !mcpParam.description().isEmpty()) {
                propSchema.put("description", mcpParam.description());
            }

            properties.put(paramName, propSchema);

            // Determine required: no @McpParam or @McpParam(required=true) → required
            if (mcpParam == null || mcpParam.required()) {
                required.add(paramName);
            }
        }

        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }

        return schema;
    }

    /**
     * Mapeia um tipo Java para o tipo JSON Schema correspondente.
     *
     * @param type        a classe do tipo
     * @param genericType o tipo genérico (para extrair parâmetros de tipo de List, etc.)
     * @return mapa representando o tipo JSON Schema
     */
    Map<String, Object> mapType(Class<?> type, Type genericType) {
        Map<String, Object> schema = new LinkedHashMap<>();

        if (type == String.class) {
            schema.put("type", "string");
        } else if (type == int.class || type == Integer.class
                || type == long.class || type == Long.class) {
            schema.put("type", "integer");
        } else if (type == double.class || type == Double.class
                || type == float.class || type == Float.class) {
            schema.put("type", "number");
        } else if (type == boolean.class || type == Boolean.class) {
            schema.put("type", "boolean");
        } else if (List.class.isAssignableFrom(type)) {
            schema.put("type", "array");
            if (genericType instanceof ParameterizedType pt) {
                Type[] typeArgs = pt.getActualTypeArguments();
                if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> itemClass) {
                    schema.put("items", mapType(itemClass, itemClass));
                }
            }
        } else if (type.isArray()) {
            schema.put("type", "array");
            Class<?> componentType = type.getComponentType();
            schema.put("items", mapType(componentType, componentType));
        } else {
            // POJO
            return mapPojo(type);
        }

        return schema;
    }

    /**
     * Gera schema para um POJO (tipo objeto complexo) via reflexão nos campos.
     *
     * @param pojoClass a classe do POJO
     * @return mapa representando o JSON Schema do POJO
     */
    Map<String, Object> mapPojo(Class<?> pojoClass) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        for (Field field : pojoClass.getDeclaredFields()) {
            properties.put(field.getName(), mapType(field.getType(), field.getGenericType()));
        }
        schema.put("properties", properties);

        return schema;
    }
}
