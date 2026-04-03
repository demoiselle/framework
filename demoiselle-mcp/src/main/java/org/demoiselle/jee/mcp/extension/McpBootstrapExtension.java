/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.extension;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;

import org.demoiselle.jee.mcp.annotation.McpParam;
import org.demoiselle.jee.mcp.annotation.McpPrompt;
import org.demoiselle.jee.mcp.annotation.McpResource;
import org.demoiselle.jee.mcp.annotation.McpTool;
import org.demoiselle.jee.mcp.config.McpConfig;
import org.demoiselle.jee.mcp.descriptor.PromptArgument;
import org.demoiselle.jee.mcp.descriptor.PromptDescriptor;
import org.demoiselle.jee.mcp.descriptor.ResourceDescriptor;
import org.demoiselle.jee.mcp.descriptor.ToolDescriptor;
import org.demoiselle.jee.mcp.registry.McpPromptRegistry;
import org.demoiselle.jee.mcp.registry.McpResourceRegistry;
import org.demoiselle.jee.mcp.registry.McpToolRegistry;
import org.demoiselle.jee.mcp.schema.JsonSchemaGenerator;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CDI Portable Extension that discovers {@code @McpTool}, {@code @McpResource},
 * and {@code @McpPrompt} annotations during container boot and populates the
 * corresponding registries.
 *
 * <p>Also detects optional Demoiselle modules via {@code Class.forName()} and
 * registers conditional beans when those modules are present on the classpath.</p>
 */
public class McpBootstrapExtension implements Extension {

    private static final Logger LOG = Logger.getLogger(McpBootstrapExtension.class.getName());

    private final List<AnnotatedMethodInfo> toolMethods = new ArrayList<>();
    private final List<AnnotatedMethodInfo> resourceMethods = new ArrayList<>();
    private final List<AnnotatedMethodInfo> promptMethods = new ArrayList<>();

    // Optional module class names for conditional detection
    private static final String DEMOISELLE_REST_CLASS = "org.demoiselle.jee.rest.exception.DemoiselleRestExceptionMapper";
    private static final String DEMOISELLE_SECURITY_CLASS = "org.demoiselle.jee.security.interceptor.RequiredPermissionInterceptor";
    private static final String DEMOISELLE_CRUD_CLASS = "org.demoiselle.jee.crud.pagination.PageResult";
    private static final String DEMOISELLE_OBSERVABILITY_CLASS = "org.demoiselle.jee.observability.annotation.Counted";

    /**
     * Collects methods annotated with {@code @McpTool}, {@code @McpResource},
     * or {@code @McpPrompt} during the type scanning phase.
     */
    <T> void processAnnotatedType(
            @Observes @WithAnnotations({McpTool.class, McpResource.class, McpPrompt.class})
            ProcessAnnotatedType<T> pat) {

        Class<T> beanClass = pat.getAnnotatedType().getJavaClass();

        for (AnnotatedMethod<? super T> am : pat.getAnnotatedType().getMethods()) {
            Method javaMethod = am.getJavaMember();

            if (am.isAnnotationPresent(McpTool.class)) {
                toolMethods.add(new AnnotatedMethodInfo(beanClass, javaMethod, AnnotationType.TOOL));
                LOG.fine(() -> "Discovered @McpTool: " + beanClass.getName() + "#" + javaMethod.getName());
            }
            if (am.isAnnotationPresent(McpResource.class)) {
                resourceMethods.add(new AnnotatedMethodInfo(beanClass, javaMethod, AnnotationType.RESOURCE));
                LOG.fine(() -> "Discovered @McpResource: " + beanClass.getName() + "#" + javaMethod.getName());
            }
            if (am.isAnnotationPresent(McpPrompt.class)) {
                promptMethods.add(new AnnotatedMethodInfo(beanClass, javaMethod, AnnotationType.PROMPT));
                LOG.fine(() -> "Discovered @McpPrompt: " + beanClass.getName() + "#" + javaMethod.getName());
            }
        }
    }

    /**
     * Detects optional Demoiselle modules and registers conditional beans.
     */
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        detectOptionalModule(DEMOISELLE_REST_CLASS, "demoiselle-rest");
        detectOptionalModule(DEMOISELLE_SECURITY_CLASS, "demoiselle-security");
        detectOptionalModule(DEMOISELLE_CRUD_CLASS, "demoiselle-crud");
        detectOptionalModule(DEMOISELLE_OBSERVABILITY_CLASS, "demoiselle-observability");
    }

    /**
     * After deployment validation: resolves bean instances, populates registries
     * with descriptors, and applies the disabled tools filter from {@link McpConfig}.
     */
    void afterDeploymentValidation(@Observes AfterDeploymentValidation adv, BeanManager bm) {
        McpToolRegistry toolRegistry = lookupBean(bm, McpToolRegistry.class);
        McpResourceRegistry resourceRegistry = lookupBean(bm, McpResourceRegistry.class);
        McpPromptRegistry promptRegistry = lookupBean(bm, McpPromptRegistry.class);
        JsonSchemaGenerator schemaGenerator = lookupBean(bm, JsonSchemaGenerator.class);

        // Register tools
        for (AnnotatedMethodInfo info : toolMethods) {
            try {
                Object beanInstance = resolveBeanInstance(bm, info.beanClass());
                McpTool annotation = info.method().getAnnotation(McpTool.class);

                String name = annotation.name().isEmpty() ? info.method().getName() : annotation.name();
                String description = annotation.description();
                Map<String, Object> inputSchema = schemaGenerator.generate(info.method());

                ToolDescriptor descriptor = new ToolDescriptor(name, description, inputSchema, beanInstance, info.method());
                toolRegistry.register(descriptor);
                LOG.fine(() -> "Registered MCP tool: " + name);
            } catch (Exception e) {
                adv.addDeploymentProblem(e);
            }
        }

        // Register resources
        for (AnnotatedMethodInfo info : resourceMethods) {
            try {
                Object beanInstance = resolveBeanInstance(bm, info.beanClass());
                McpResource annotation = info.method().getAnnotation(McpResource.class);

                ResourceDescriptor descriptor = new ResourceDescriptor(
                        annotation.uri(),
                        annotation.name(),
                        annotation.description(),
                        annotation.mimeType(),
                        beanInstance,
                        info.method()
                );
                resourceRegistry.register(descriptor);
                LOG.fine(() -> "Registered MCP resource: " + annotation.uri());
            } catch (Exception e) {
                adv.addDeploymentProblem(e);
            }
        }

        // Register prompts
        for (AnnotatedMethodInfo info : promptMethods) {
            try {
                Object beanInstance = resolveBeanInstance(bm, info.beanClass());
                McpPrompt annotation = info.method().getAnnotation(McpPrompt.class);

                String name = annotation.name().isEmpty() ? info.method().getName() : annotation.name();
                String description = annotation.description();
                List<PromptArgument> arguments = buildPromptArguments(info.method());

                PromptDescriptor descriptor = new PromptDescriptor(name, description, arguments, beanInstance, info.method());
                promptRegistry.register(descriptor);
                LOG.fine(() -> "Registered MCP prompt: " + name);
            } catch (Exception e) {
                adv.addDeploymentProblem(e);
            }
        }

        // Apply disabled tools filter from McpConfig
        try {
            McpConfig config = lookupBean(bm, McpConfig.class);
            if (config != null) {
                Set<String> disabled = config.getDisabledToolNames();
                if (!disabled.isEmpty()) {
                    toolRegistry.applyDisabledFilter(disabled);
                    LOG.info(() -> "Disabled MCP tools: " + disabled);
                }
            }
        } catch (Exception e) {
            // McpConfig may not be resolvable yet (e.g., no @Configuration support);
            // silently ignore — tools remain enabled
            LOG.fine(() -> "McpConfig not available for disabled filter: " + e.getMessage());
        }

        LOG.info(() -> "MCP Bootstrap complete: " + toolRegistry.size() + " tools, "
                + resourceRegistry.size() + " resources, " + promptRegistry.size() + " prompts");
    }

    /**
     * Builds the list of {@link PromptArgument} from the method parameters,
     * using {@code @McpParam} annotations when present.
     */
    private List<PromptArgument> buildPromptArguments(Method method) {
        List<PromptArgument> arguments = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            McpParam mcpParam = param.getAnnotation(McpParam.class);

            String name;
            String description;
            boolean required;

            if (mcpParam != null) {
                name = mcpParam.name().isEmpty() ? param.getName() : mcpParam.name();
                description = mcpParam.description();
                required = mcpParam.required();
            } else {
                name = param.getName();
                description = "";
                required = true;
            }

            arguments.add(new PromptArgument(name, description, required));
        }
        return arguments;
    }

    /**
     * Detects an optional module by attempting to load a marker class.
     */
    private void detectOptionalModule(String className, String moduleName) {
        try {
            Class.forName(className);
            LOG.info(() -> "Optional module detected: " + moduleName);
        } catch (ClassNotFoundException e) {
            LOG.fine(() -> "Optional module not available: " + moduleName);
        }
    }

    /**
     * Looks up a CDI bean by type from the BeanManager.
     */
    @SuppressWarnings("unchecked")
    private <T> T lookupBean(BeanManager bm, Class<T> type) {
        Bean<?> bean = bm.resolve(bm.getBeans(type));
        if (bean == null) {
            return null;
        }
        return (T) bm.getReference(bean, type, bm.createCreationalContext(bean));
    }

    /**
     * Resolves a CDI bean instance by its class.
     */
    private Object resolveBeanInstance(BeanManager bm, Class<?> beanClass) {
        Bean<?> bean = bm.resolve(bm.getBeans(beanClass));
        if (bean == null) {
            throw new IllegalStateException("No CDI bean found for class: " + beanClass.getName());
        }
        return bm.getReference(bean, beanClass, bm.createCreationalContext(bean));
    }

    // Visible for testing
    List<AnnotatedMethodInfo> getToolMethods() {
        return toolMethods;
    }

    List<AnnotatedMethodInfo> getResourceMethods() {
        return resourceMethods;
    }

    List<AnnotatedMethodInfo> getPromptMethods() {
        return promptMethods;
    }

    /**
     * Type of MCP annotation discovered on a method.
     */
    enum AnnotationType {
        TOOL, RESOURCE, PROMPT
    }

    /**
     * Helper record to store information about an annotated method discovered
     * during the CDI scanning phase.
     *
     * @param beanClass      the CDI bean class containing the method
     * @param method         the annotated Java method
     * @param annotationType the type of MCP annotation found
     */
    record AnnotatedMethodInfo(Class<?> beanClass, Method method, AnnotationType annotationType) {}
}
