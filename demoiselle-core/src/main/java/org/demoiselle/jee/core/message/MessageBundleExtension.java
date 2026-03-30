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

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;

import jakarta.enterprise.inject.Default;

import org.demoiselle.jee.core.annotation.MessageBundle;
import org.demoiselle.jee.core.annotation.MessageTemplate;

/**
 * CDI Extension that discovers interfaces annotated with {@link MessageBundle}
 * and registers dynamic proxy implementations as CDI beans. The proxies use
 * {@link ResourceBundle} to resolve messages from {@code .properties} files.
 *
 * @author SERPRO
 */
public class MessageBundleExtension implements Extension {

    private static final Logger logger = Logger.getLogger(MessageBundleExtension.class.getName());

    private final List<Class<?>> messageBundleInterfaces = new ArrayList<>();

    /**
     * Observes annotated types with {@link MessageBundle} and collects the interfaces.
     */
    <T> void processAnnotatedType(@Observes @WithAnnotations(MessageBundle.class) ProcessAnnotatedType<T> pat) {
        Class<T> javaClass = pat.getAnnotatedType().getJavaClass();
        if (javaClass.isInterface() && javaClass.isAnnotationPresent(MessageBundle.class)) {
            logger.fine("Discovered @MessageBundle interface: " + javaClass.getName());
            messageBundleInterfaces.add(javaClass);
        }
    }

    /**
     * After bean discovery, registers a CDI bean for each discovered @MessageBundle interface
     * backed by a dynamic proxy that resolves messages from ResourceBundle.
     */
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        for (Class<?> iface : messageBundleInterfaces) {
            addMessageBundleBean(abd, iface);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void addMessageBundleBean(AfterBeanDiscovery abd, Class<T> iface) {
        abd.addBean()
            .beanClass(iface)
            .types(iface, Object.class)
            .qualifiers(new MessageBundleLiteral(), Default.Literal.INSTANCE)
            .scope(jakarta.enterprise.context.ApplicationScoped.class)
            .createWith(ctx -> {
                String bundleName = iface.getName().replace('.', '/');
                return (T) Proxy.newProxyInstance(
                    iface.getClassLoader(),
                    new Class<?>[]{iface},
                    new MessageBundleInvocationHandler(bundleName)
                );
            });
        logger.fine("Registered @MessageBundle bean for: " + iface.getName());
    }

    /**
     * InvocationHandler that resolves method calls to ResourceBundle lookups.
     */
    private static class MessageBundleInvocationHandler implements InvocationHandler {

        private final String bundleName;

        MessageBundleInvocationHandler(String bundleName) {
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
            // Remove surrounding braces: "{key}" -> "key"
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
                Logger.getLogger(MessageBundleExtension.class.getName())
                    .log(Level.WARNING, "Message key not found: " + key + " in bundle: " + bundleName);
                return "???" + key + "???";
            }
        }
    }
}
