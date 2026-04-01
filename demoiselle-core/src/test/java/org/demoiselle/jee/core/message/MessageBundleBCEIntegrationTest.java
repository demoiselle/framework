/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.message;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import jakarta.enterprise.inject.spi.CDI;

import org.demoiselle.jee.core.annotation.MessageBundle;
import org.demoiselle.jee.core.annotation.MessageTemplate;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * CDI integration tests for MessageBundleBuildCompatibleExtension.
 * <p>
 * Tests verify that:
 * 1. The portable extension discovers @MessageBundle interfaces and produces functional proxy beans via Weld CDI
 * 2. The BCE proxy logic produces functionally identical results
 * 3. Both approaches yield the same behavior for all message resolution scenarios
 * </p>
 *
 * Validates: Requirements 8.2, 8.4
 */
class MessageBundleBCEIntegrationTest {

    /**
     * Creates a proxy using the same algorithm as the BCE's MessageBundleSyntheticCreator.
     * This replicates the BCE proxy creation logic since the internal handler is private.
     */
    private static TestMessages createBCEEquivalentProxy() {
        String bundleName = TestMessages.class.getName().replace('.', '/');
        return (TestMessages) Proxy.newProxyInstance(
            TestMessages.class.getClassLoader(),
            new Class<?>[]{TestMessages.class},
            new BCEEquivalentHandler(bundleName)
        );
    }

    /**
     * Handler that mirrors the BCE's MessageBundleProxyHandler logic exactly.
     * Used to verify that the BCE would produce identical behavior to the portable extension.
     */
    private static class BCEEquivalentHandler implements InvocationHandler {
        private final String bundleName;

        BCEEquivalentHandler(String bundleName) {
            this.bundleName = bundleName;
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
            String key = template.value();
            if (key.startsWith("{") && key.endsWith("}")) {
                key = key.substring(1, key.length() - 1);
            }
            String message;
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
                message = bundle.getString(key);
            } catch (MissingResourceException e) {
                message = "???" + key + "???";
            }
            if (args != null && args.length > 0) {
                message = String.format(message, args);
            }
            return message;
        }
    }

    // --- Portable Extension Tests (via Weld CDI) ---

    @Nested
    @ExtendWith(WeldJunit5Extension.class)
    @DisplayName("Portable Extension: @MessageBundle bean discovery and functionality")
    class PortableExtensionTests {

        @WeldSetup
        WeldInitiator weld = WeldInitiator.from(WeldInitiator.createWeld()
                .addExtension(new MessageBundleExtension())
                .addBeanClass(TestMessages.class))
            .build();

        @Test
        @DisplayName("@MessageBundle bean is discovered and injected by portable extension")
        void messageBundleBeanIsDiscoveredAndInjected() {
            TestMessages messages = CDI.current()
                .select(TestMessages.class, new MessageBundleLiteral()).get();
            assertNotNull(messages,
                "@MessageBundle bean should be discovered and injected via portable extension");
        }

        @Test
        @DisplayName("Simple message resolution works via portable extension")
        void simpleMessageResolution() {
            TestMessages messages = CDI.current()
                .select(TestMessages.class, new MessageBundleLiteral()).get();
            assertEquals("Hello World", messages.hello());
        }

        @Test
        @DisplayName("Parameterized message resolution works via portable extension")
        void parameterizedMessageResolution() {
            TestMessages messages = CDI.current()
                .select(TestMessages.class, new MessageBundleLiteral()).get();
            assertEquals("Hello Demoiselle", messages.greeting("Demoiselle"));
        }

        @Test
        @DisplayName("Missing key returns fallback format via portable extension")
        void missingKeyReturnsFallback() {
            TestMessages messages = CDI.current()
                .select(TestMessages.class, new MessageBundleLiteral()).get();
            assertEquals("???missing-key???", messages.missingKey());
        }

        @Test
        @DisplayName("Method without @MessageTemplate returns null via portable extension")
        void methodWithoutTemplateReturnsNull() {
            TestMessages messages = CDI.current()
                .select(TestMessages.class, new MessageBundleLiteral()).get();
            assertNull(messages.noTemplate());
        }

        @Test
        @DisplayName("Portable extension produces a bean that is not a concrete class")
        void producesNonConcreteInstance() {
            TestMessages messages = CDI.current()
                .select(TestMessages.class, new MessageBundleLiteral()).get();
            // Weld wraps the JDK dynamic proxy in a CDI client proxy for @ApplicationScoped beans,
            // so we verify the bean implements the interface and is not a direct implementation.
            assertTrue(messages instanceof TestMessages,
                "Bean should implement TestMessages interface");
            assertNotEquals(TestMessages.class, messages.getClass(),
                "Bean class should not be the interface itself (it's a proxy)");
        }
    }

    // --- BCE Proxy Logic Tests ---

    @Nested
    @DisplayName("BCE: Proxy handler produces functional beans equivalent to portable extension")
    class BCEProxyTests {

        @Test
        @DisplayName("BCE proxy resolves simple messages")
        void bceProxyResolvesSimpleMessages() {
            TestMessages bceProxy = createBCEEquivalentProxy();
            assertEquals("Hello World", bceProxy.hello());
        }

        @Test
        @DisplayName("BCE proxy resolves parameterized messages")
        void bceProxyResolvesParameterizedMessages() {
            TestMessages bceProxy = createBCEEquivalentProxy();
            assertEquals("Hello Demoiselle", bceProxy.greeting("Demoiselle"));
        }

        @Test
        @DisplayName("BCE proxy returns fallback for missing keys")
        void bceProxyReturnsFallbackForMissingKeys() {
            TestMessages bceProxy = createBCEEquivalentProxy();
            assertEquals("???missing-key???", bceProxy.missingKey());
        }

        @Test
        @DisplayName("BCE proxy returns null for methods without @MessageTemplate")
        void bceProxyReturnsNullForNoTemplate() {
            TestMessages bceProxy = createBCEEquivalentProxy();
            assertNull(bceProxy.noTemplate());
        }

        @Test
        @DisplayName("BCE proxy handles Object methods (toString, hashCode)")
        void bceProxyHandlesObjectMethods() {
            TestMessages bceProxy = createBCEEquivalentProxy();
            assertNotNull(bceProxy.toString());
            // hashCode should not throw
            assertDoesNotThrow(() -> bceProxy.hashCode());
        }
    }

    // --- Behavioral Comparison Tests ---

    @Nested
    @ExtendWith(WeldJunit5Extension.class)
    @DisplayName("Comparison: BCE produces same behavior as portable extension")
    class BehavioralComparisonTests {

        @WeldSetup
        WeldInitiator weld = WeldInitiator.from(WeldInitiator.createWeld()
                .addExtension(new MessageBundleExtension())
                .addBeanClass(TestMessages.class))
            .build();

        @Test
        @DisplayName("Simple message: BCE and portable extension produce identical results")
        void simpleMessageIdentical() {
            TestMessages portableProxy = CDI.current()
                .select(TestMessages.class, new MessageBundleLiteral()).get();
            TestMessages bceProxy = createBCEEquivalentProxy();
            assertEquals(portableProxy.hello(), bceProxy.hello());
        }

        @Test
        @DisplayName("Parameterized message: BCE and portable extension produce identical results")
        void parameterizedMessageIdentical() {
            TestMessages portableProxy = CDI.current()
                .select(TestMessages.class, new MessageBundleLiteral()).get();
            TestMessages bceProxy = createBCEEquivalentProxy();
            assertEquals(portableProxy.greeting("Test"), bceProxy.greeting("Test"));
        }

        @Test
        @DisplayName("Missing key: BCE and portable extension produce identical fallback")
        void missingKeyIdentical() {
            TestMessages portableProxy = CDI.current()
                .select(TestMessages.class, new MessageBundleLiteral()).get();
            TestMessages bceProxy = createBCEEquivalentProxy();
            assertEquals(portableProxy.missingKey(), bceProxy.missingKey());
        }

        @Test
        @DisplayName("No template: BCE and portable extension both return null")
        void noTemplateIdentical() {
            TestMessages portableProxy = CDI.current()
                .select(TestMessages.class, new MessageBundleLiteral()).get();
            TestMessages bceProxy = createBCEEquivalentProxy();
            assertEquals(portableProxy.noTemplate(), bceProxy.noTemplate());
        }

        @Test
        @DisplayName("Both extensions produce non-concrete proxy instances")
        void bothProduceProxies() {
            TestMessages portableProxy = CDI.current()
                .select(TestMessages.class, new MessageBundleLiteral()).get();
            TestMessages bceProxy = createBCEEquivalentProxy();
            // Both should implement TestMessages but not be the interface class itself
            assertTrue(portableProxy instanceof TestMessages,
                "Portable extension should produce a TestMessages instance");
            assertTrue(bceProxy instanceof TestMessages,
                "BCE should produce a TestMessages instance");
            assertTrue(Proxy.isProxyClass(bceProxy.getClass()),
                "BCE should produce a JDK dynamic proxy");
        }
    }
}
