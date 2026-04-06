/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.message;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.context.ApplicationScoped;

import org.demoiselle.jee.core.annotation.MessageBundle;
import org.demoiselle.jee.core.annotation.MessageTemplate;

/**
 * CDI 4.0 Lite Build-Compatible Extension that discovers interfaces annotated
 * with {@link MessageBundle} and registers synthetic bean implementations.
 * <p>
 * This extension replaces the portable extension {@link MessageBundleExtension}
 * for containers that support CDI Lite (build-time processing). The original
 * portable extension is kept as fallback for containers that only support CDI Full.
 * </p>
 *
 * @author SERPRO
 * @see MessageBundleExtension
 */
public class MessageBundleBuildCompatibleExtension implements BuildCompatibleExtension {

    private static final Logger logger = Logger.getLogger(MessageBundleBuildCompatibleExtension.class.getName());

    private final List<String> messageBundleClassNames = new ArrayList<>();

    /**
     * Discovery phase — ensures @MessageBundle annotated types are scanned.
     */
    @Discovery
    public void discovery(ScannedClasses scan) {
        // CDI 4.0 Lite automatically discovers types; no additional scanning needed.
        logger.fine("MessageBundleBuildCompatibleExtension: discovery phase started");
    }

    /**
     * Enhancement phase — collects interfaces annotated with {@link MessageBundle}.
     */
    @Enhancement(types = Object.class, withAnnotations = MessageBundle.class)
    public void collectMessageBundles(ClassInfo classInfo) {
        if (classInfo.isInterface()) {
            String className = classInfo.name();
            logger.fine("Discovered @MessageBundle interface via BCE: " + className);
            messageBundleClassNames.add(className);
        }
    }

    /**
     * Synthesis phase — registers synthetic beans for each discovered @MessageBundle interface.
     * Each synthetic bean produces a dynamic proxy backed by ResourceBundle lookups,
     * functionally identical to the beans registered by the portable extension.
     */
    @SuppressWarnings("unchecked")
    @Synthesis
    public void registerBeans(SyntheticComponents syn) {
        for (String className : messageBundleClassNames) {
            registerBean(syn, className);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerBean(SyntheticComponents syn, String className) {
        try {
            Class iface = Class.forName(className);
            syn.addBean(iface)
                .type(iface)
                .type(Object.class)
                .qualifier(MessageBundle.class)
                .qualifier(Default.class)
                .scope(ApplicationScoped.class)
                .createWith(MessageBundleSyntheticCreator.class)
                .withParam("interfaceName", className);
            logger.fine("Registered synthetic @MessageBundle bean via BCE for: " + className);
        } catch (ClassNotFoundException e) {
            logger.log(Level.WARNING,
                "Could not load @MessageBundle interface: " + className, e);
        }
    }

    /**
     * Synthetic bean creator that produces dynamic proxy instances for @MessageBundle interfaces.
     * This creator is invoked by the CDI container when a synthetic bean instance is needed.
     * <p>
     * Marcada como {@code @Vetoed} para impedir descoberta automática pelo Weld
     * com {@code bean-discovery-mode="all"}.
     */
    @jakarta.enterprise.inject.Vetoed
    public static class MessageBundleSyntheticCreator implements SyntheticBeanCreator<Object> {

        @Override
        public Object create(jakarta.enterprise.inject.Instance<Object> lookup, Parameters params) {
            String interfaceName = params.get("interfaceName", String.class);
            try {
                Class<?> iface = Class.forName(interfaceName);
                String bundleName = iface.getName().replace('.', '/');
                return Proxy.newProxyInstance(
                    iface.getClassLoader(),
                    new Class<?>[]{iface},
                    new MessageBundleProxyHandler(bundleName)
                );
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(
                    "Failed to create @MessageBundle proxy for: " + interfaceName, e);
            }
        }
    }

    /**
     * InvocationHandler that resolves method calls to ResourceBundle lookups.
     * Functionally identical to the handler in {@link MessageBundleExtension}.
     */
    private static class MessageBundleProxyHandler implements InvocationHandler {

        private final String bundleName;

        MessageBundleProxyHandler(String bundleName) {
            this.bundleName = bundleName;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Handle Object methods
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }

            MessageTemplate template = method.getAnnotation(MessageTemplate.class);
            if (template == null) {
                return null;
            }

            String key = extractKey(template.value());
            String message = resolveMessage(key);

            if (args != null && args.length > 0) {
                message = String.format(message, args);
            }

            return message;
        }

        private String extractKey(String templateValue) {
            if (templateValue.startsWith("{") && templateValue.endsWith("}")) {
                return templateValue.substring(1, templateValue.length() - 1);
            }
            return templateValue;
        }

        private String resolveMessage(String key) {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
                return bundle.getString(key);
            } catch (MissingResourceException e) {
                Logger.getLogger(MessageBundleBuildCompatibleExtension.class.getName())
                    .log(Level.WARNING, "Message key not found: " + key + " in bundle: " + bundleName);
                return "???" + key + "???";
            }
        }
    }
}
