/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration;

/**
 * Defines the types of sources that can be consumed.
 * 
 * @author SERPRO
 *
 */
public enum ConfigurationType {
    /**
     * Loaded settings {@link System#getProperties()} or
     * {@link System#getenv()}.
     */
    SYSTEM,

    /**
     * Settings loaded from an XML file.
     */
    XML,

    /**
     * Settings loaded from a properties file.
     */
    PROPERTIES
}
