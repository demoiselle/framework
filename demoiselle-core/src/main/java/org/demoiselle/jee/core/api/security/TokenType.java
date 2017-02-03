/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.security;

/**
 * Types of token
 *
 * @author SERPRO
 */
public enum TokenType {
    JWT,
    BASIC,
    BEARER,
    MAC,
    TOKEN
}
