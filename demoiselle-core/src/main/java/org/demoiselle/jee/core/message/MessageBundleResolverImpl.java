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
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

import org.demoiselle.jee.core.annotation.MessageBundle;
import org.demoiselle.jee.core.annotation.MessageTemplate;
import org.demoiselle.jee.core.api.message.MessageBundleResolver;

@ApplicationScoped
public class MessageBundleResolverImpl implements MessageBundleResolver {

    private static final Logger LOG = Logger.getLogger(MessageBundleResolverImpl.class.getName());

    private final ConcurrentMap<BundleKey, Object> cache = new ConcurrentHashMap<>();

    @Override
    public <T> T resolve(Class<T> bundleType) {
        return resolve(bundleType, Locale.getDefault());
    }

    @Override
    public <T> T resolve(Class<T> bundleType, Locale locale) {
        Objects.requireNonNull(bundleType, "bundleType");
        if (!bundleType.isInterface() || !bundleType.isAnnotationPresent(MessageBundle.class)) {
            throw new IllegalArgumentException("Bundle type must be an interface annotated with @MessageBundle.");
        }

        Locale resolvedLocale = locale == null ? Locale.getDefault() : locale;
        BundleKey key = new BundleKey(bundleType, resolvedLocale);
        Object proxy = cache.computeIfAbsent(key, ignored -> createProxy(bundleType, resolvedLocale));
        return bundleType.cast(proxy);
    }

    private <T> Object createProxy(Class<T> bundleType, Locale locale) {
        String bundleName = bundleType.getName().replace('.', '/');
        return Proxy.newProxyInstance(
                bundleType.getClassLoader(),
                new Class<?>[]{bundleType},
                new MessageBundleInvocationHandler(bundleName, locale)
        );
    }

    private static final class MessageBundleInvocationHandler implements InvocationHandler {

        private final String bundleName;
        private final Locale locale;

        private MessageBundleInvocationHandler(String bundleName, Locale locale) {
            this.bundleName = bundleName;
            this.locale = locale;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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
                return String.format(locale, message, args);
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
                ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
                return bundle.getString(key);
            } catch (MissingResourceException ex) {
                LOG.log(Level.WARNING, "Message key not found: {0} in bundle: {1}", new Object[]{key, bundleName});
                return "???" + key + "???";
            }
        }
    }

    private static final class BundleKey {

        private final Class<?> bundleType;
        private final Locale locale;

        private BundleKey(Class<?> bundleType, Locale locale) {
            this.bundleType = bundleType;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof BundleKey that)) {
                return false;
            }
            return bundleType.equals(that.bundleType) && locale.equals(that.locale);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bundleType, locale);
        }
    }
}
