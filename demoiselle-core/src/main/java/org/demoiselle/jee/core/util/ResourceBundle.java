/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.util;

import java.io.Serializable;
import static java.lang.Thread.currentThread;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import static java.util.regex.Matcher.quoteReplacement;

/**
 * <p>
 * The Demoiselle's ResourceBundle extends the abstraction
 * {@link java.util.ResourceBundle}, and provide the locale and the base name
 * for the bundle.</p>
 *
 * <p>
 * To select which resource properties file to load when injecting beans of this
 * class, qualify the injection point with
 * {@link  org.demoiselle.jee.core.annotation.Name}, using the resource name
 * (without the '.properties' extension) as the value. If the injection point
 * isn't qualified the default file <code>messages.properties</code> will be
 * loaded from the root of the classpath.</p>
 *
 * @author SERPRO
 */
public class ResourceBundle extends java.util.ResourceBundle implements Serializable {

    private static final long serialVersionUID = 1L;

    private String baseName;

    private transient java.util.ResourceBundle delegate;

    private final Locale locale;

    private java.util.ResourceBundle getDelegate() {
        if (delegate == null) {
            try {
                ClassLoader classLoader = currentThread().getContextClassLoader();
                delegate = getBundle(baseName, locale, classLoader);

            } catch (MissingResourceException mre) {
                delegate = getBundle(baseName, locale);
            }
        }

        return delegate;
    }

    /**
     * Constructor that set values of baseName and locale.
     *
     * @param baseName the base name to construct the complete bundle name.
     *
     * @param locale locale to define the choosen bundle.
     */
    public ResourceBundle(String baseName, Locale locale) {
        this.baseName = baseName;
        this.locale = locale;
    }

    @Override
    public boolean containsKey(String key) {
        return getDelegate().containsKey(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        return getDelegate().getKeys();
    }

    @Override
    public Locale getLocale() {
        return getDelegate().getLocale();
    }

    @Override
    public Set<String> keySet() {
        return getDelegate().keySet();
    }

    /**
     *
     * @param key Key name
     * @param params Params
     * @return String
     */
    public String getString(String key, Object... params) {
        String result = null;

        if (key != null) {
            result = key;
        }

        if (params != null && key != null) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] != null) {
                    result = result.replaceAll("\\{" + i + "\\}", quoteReplacement(params[i].toString()));
                }
            }
        }
        return result;
    }

    @Override
    protected Object handleGetObject(String key) {
        Object result;

        try {
            Method method = getDelegate().getClass().getMethod("handleGetObject", String.class);

            method.setAccessible(true);
            result = method.invoke(delegate, key);
            method.setAccessible(false);

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException cause) {
            throw new RuntimeException(cause);
        }

        return result;
    }
}
